package hr.java.production.service;

import hr.java.production.exception.DatabaseException;
import hr.java.production.log.BinaryChangeLogger;
import hr.java.production.log.ChangeLogger;
import hr.java.production.model.Address;
import hr.java.production.model.Freelancer;
import hr.java.production.repo.db.AddressDao;
import hr.java.production.repo.db.FreelancerDao;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servis za upravljanje freelancerima i njihovim povezanim podacima, uključujući
 * funkcionalnosti poput kreiranja, ažuriranja, brisanja, pretraživanja freelancera i generiranja pogleda
 * namijenjenih za rad s grafičkim sučeljem. Omogućuje transakcijsku konzistenciju tijekom operacija
 * te evidentiranje promjena kroz ChangeLogger.
 */
public final class FreelancerService extends TransactionService {

    private static final String NO_FREELANCER_ID = "Freelancer ne postoji: id=";

    private final FreelancerDao freelancerDao;
    private final AddressDao addressDao;
    private final ChangeLogger changeLogger;

    public FreelancerService(FreelancerDao freelancerDao,
                             AddressDao addressDao,
                             ChangeLogger changeLogger) {
        this.freelancerDao = Objects.requireNonNull(freelancerDao);
        this.addressDao    = Objects.requireNonNull(addressDao);
        this.changeLogger  = Objects.requireNonNull(changeLogger);
    }

    public FreelancerService() {
        this(new FreelancerDao(), new AddressDao(), new BinaryChangeLogger());
    }

    public Long save(Freelancer freelancer) throws DatabaseException {
        return inTransaction(conn -> {
            freelancerDao.save(conn, freelancer);
            changeLogger.logCreate(freelancer);
            return freelancer.getId();
        }, "Greška pri kreiranju freelancera");
    }

    public void update(Freelancer updated) throws DatabaseException {
        inTransaction(conn -> {
            Long id = Objects.requireNonNull(updated.getId(), "ID ne smije biti null");
            Freelancer old = freelancerDao.findById(conn, id)
                    .orElseThrow(() -> new DatabaseException(NO_FREELANCER_ID + id));

            freelancerDao.update(conn, updated);
            changeLogger.logUpdate(old, updated);
            return null;
        }, "Greška pri ažuriranju freelancera");
    }

    public void delete(Long freelancerId) throws DatabaseException {
        inTransaction(conn -> {
            Freelancer old = freelancerDao.findById(conn, freelancerId)
                    .orElseThrow(() -> new DatabaseException(NO_FREELANCER_ID + freelancerId));

            freelancerDao.delete(conn, freelancerId);
            changeLogger.logDelete(old);
            return null;
        }, "Greška pri brisanju freelancera");
    }

    /** Returns ALL freelancers fully hydrated (Freelancer + Address) using batch address fetch. */
    public List<Freelancer> findAll() throws DatabaseException {
        return inTransaction(conn -> {
            List<Freelancer> freelancers = freelancerDao.findAll(conn);
            if (freelancers.isEmpty()) return List.of();

            // Skupi sve addressId-ove koje trebamo
            Set<Long> addressIds = freelancers.stream()
                    .map(f -> f.getAddress() != null ? f.getAddress().getId() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, Address> addresses = addressDao.findByIds(conn, addressIds);

            // Hidracija na mjestu: zamijeni ref(id) potpunim objektom
            for (Freelancer f : freelancers) {
                if (f.getAddress() != null && f.getAddress().getId() != null) {
                    Address full = addresses.get(f.getAddress().getId());
                    if (full != null) {
                        f.setAddress(full);
                    }
                }
            }
            return freelancers;
        }, "Greška pri dohvaćanju freelancera");
    }

    public Optional<Freelancer> findById(Long id) throws DatabaseException {
        return inTransaction(conn -> {
            Optional<Freelancer> base = freelancerDao.findById(conn, id);
            if (base.isEmpty()) return Optional.empty();

            Freelancer f = base.get();
            if (f.getAddress() != null && f.getAddress().getId() != null) {
                Address a = addressDao.findById(conn, f.getAddress().getId()).orElse(null);
                if (a != null) f.setAddress(a);
            }
            return Optional.of(f);
        }, "Greška pri čitanju freelancera po ID-u");
    }
}
