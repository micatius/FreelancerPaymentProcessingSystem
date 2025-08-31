package hr.java.production.service;

import hr.java.production.exception.DatabaseException;
import hr.java.production.log.BinaryChangeLogger;
import hr.java.production.log.ChangeLogger;
import hr.java.production.repo.db.AddressDao;
import hr.java.production.repo.db.FreelancerDao;
import hr.java.production.repo.db.InvoiceDao;
import hr.java.production.repo.db.PaymentDao;
import hr.java.production.repo.db.ServiceDao;
import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.model.Address;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;
import hr.java.production.model.Payment;
import hr.java.production.model.Service;
import hr.java.production.util.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servis za rad s računima (Invoice) uključujući transakcijsko spremanje stavki (Service) i uplata (Payment)
 * te detaljno čitanje s hidracijom povezanih entiteta.
 */
public final class InvoiceService {

    private final InvoiceDao invoiceDao;
    private final ServiceDao serviceDao;
    private final PaymentDao paymentDao;
    private final FreelancerDao freelancerDao;
    private final AddressDao addressDao;
    private final ChangeLogger changeLogger;

    public InvoiceService(InvoiceDao invoiceDao,
                          ServiceDao serviceDao,
                          PaymentDao paymentDao,
                          FreelancerDao freelancerDao,
                          AddressDao addressDao,
                          ChangeLogger changeLogger) {
        this.invoiceDao = Objects.requireNonNull(invoiceDao);
        this.serviceDao = Objects.requireNonNull(serviceDao);
        this.paymentDao = Objects.requireNonNull(paymentDao);
        this.freelancerDao = Objects.requireNonNull(freelancerDao);
        this.addressDao = Objects.requireNonNull(addressDao);
        this.changeLogger = Objects.requireNonNull(changeLogger);
    }

    public InvoiceService() {
        this(new InvoiceDao(),
                new ServiceDao(),
                new PaymentDao(),
                new FreelancerDao(),
                new AddressDao(),
                new BinaryChangeLogger());
    }

    /**
     * Kreira račun i sve pripadajuće stavke/ uplate u jednoj transakciji.
     *
     * @param invoice  račun koji se sprema (mora imati postavljenog freelancera kao ref(id))
     * @param services lista stavki za taj račun (svakoj se postavlja invoiceId)
     * @param payments lista uplata za taj račun (svakoj se postavlja Invoice.ref(generatedId))
     * @return spremljeni račun s generiranim ID-om
     * @throws DatabaseException u slučaju greške pri spremanju
     */
    public Invoice create(Invoice invoice, List<Service> services, List<Payment> payments) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            conn.setAutoCommit(false);
            try {
                // 1) spremi račun (dobiva ID)
                invoiceDao.save(conn, invoice);
                Long invId = invoice.getId();

                // 2) spremi stavke (postavi invoiceId)
                if (services != null) {
                    for (Service s : services) {
                        Service toSave = new Service.Builder(s).invoiceId(invId).build();
                        serviceDao.save(conn, toSave);
                    }
                }

                // 3) spremi uplate (postavi Invoice.ref(id))
                if (payments != null) {
                    for (Payment p : payments) {
                        Payment toSave = new Payment.Builder(p).invoice(Invoice.ref(invId)).build();
                        paymentDao.save(conn, toSave);
                    }
                }

                conn.commit();

                // after-commit logging
                changeLogger.logCreate(invoice);

                return invoice;
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                if (e instanceof DatabaseException dae) throw dae;
                throw new DatabaseException("Greška pri kreiranju računa", e);
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        } catch (DatabaseConnectionException | SQLException e) {
            throw new DatabaseException("Greška pri uspostavi veze prema bazi", e);
        }
    }

    /**
     * Ažurira račun te zamjenjuje sve stavke i uplate novim listama (jednostavan model).
     *
     * @param updated  novi sadržaj računa (isti ID kao postojeći)
     * @param services nove stavke
     * @param payments nove uplate
     * @return ažurirani račun
     * @throws DatabaseException u slučaju greške pri spremanju
     */
    public Invoice update(Invoice updated, List<Service> services, List<Payment> payments) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            conn.setAutoCommit(false);
            try {
                // snapshot starog stanja samo za logiranje
                Invoice old = invoiceDao.findById(conn, updated.getId())
                        .orElseThrow(() -> new DatabaseException("Račun ne postoji: id=" + updated.getId()));

                // 1) ažuriraj račun
                invoiceDao.update(conn, updated);

                Long invId = updated.getId();

                // 2) osvježi stavke: briši sve → dodaj nove
                serviceDao.deleteByInvoiceId(conn, invId);
                if (services != null) {
                    for (Service s : services) {
                        Service toSave = new Service.Builder(s).invoiceId(invId).build();
                        serviceDao.save(conn, toSave);
                    }
                }

                // 3) osvježi uplate
                paymentDao.deleteByInvoiceId(conn, invId);
                if (payments != null) {
                    for (Payment p : payments) {
                        Payment toSave = new Payment.Builder(p).invoice(Invoice.ref(invId)).build();
                        paymentDao.save(conn, toSave);
                    }
                }

                conn.commit();

                // after-commit logging
                changeLogger.logUpdate(old, updated);

                return updated;
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                if (e instanceof DatabaseException dae) throw dae;
                throw new DatabaseException("Greška pri ažuriranju računa", e);
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        } catch (DatabaseConnectionException | SQLException e) {
            throw new DatabaseException("Greška pri uspostavi veze prema bazi", e);
        }
    }

    /**
     * Briše račun i sve povezane stavke/ uplate u jednoj transakciji.
     *
     * @param invoiceId ID računa koji se briše
     * @throws DatabaseException u slučaju greške pri brisanju
     */
    public void delete(Long invoiceId) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            conn.setAutoCommit(false);
            try {
                Invoice old = invoiceDao.findById(conn, invoiceId)
                        .orElseThrow(() -> new DatabaseException("Račun ne postoji: id=" + invoiceId));

                // redoslijed: djeca pa roditelj
                serviceDao.deleteByInvoiceId(conn, invoiceId);
                paymentDao.deleteByInvoiceId(conn, invoiceId);
                invoiceDao.delete(conn, invoiceId);

                conn.commit();

                // after-commit logging
                changeLogger.logDelete(old);

            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                if (e instanceof DatabaseException dae) throw dae;
                throw new DatabaseException("Greška pri brisanju računa", e);
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        } catch (DatabaseConnectionException | SQLException e) {
            throw new DatabaseException("Greška pri uspostavi veze prema bazi", e);
        }
    }

    /**
     * Vraća detaljan prikaz računa: račun + freelancer (+adresa) + stavke + uplate.
     *
     * @param invoiceId ID računa
     * @return detaljni pogled
     * @throws DatabaseException u slučaju greške pri čitanju
     */
    public InvoiceView getDetailed(Long invoiceId) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            conn.setAutoCommit(false);
            try {
                Invoice invoice = invoiceDao.findById(conn, invoiceId)
                        .orElseThrow(() -> new DatabaseException("Račun ne postoji: id=" + invoiceId));

                // hydrate freelancer (+address)
                Freelancer freelancer = null;
                Address address = null;
                if (invoice.getFreelancer() != null && invoice.getFreelancer().getId() != null) {
                    Long freelancerId = invoice.getFreelancer().getId();
                    freelancer = freelancerDao.findById(conn, freelancerId).orElse(null);

                    if (freelancer != null && freelancer.getAddress() != null && freelancer.getAddress().getId() != null) {
                        address = addressDao.findById(conn, freelancer.getAddress().getId()).orElse(null);
                    }
                }

                // load children
                List<Service> services = serviceDao.findByInvoiceId(conn, invoiceId);
                List<Payment> payments = paymentDao.findByInvoiceId(conn, invoiceId);

                conn.commit();
                return new InvoiceView(invoice, freelancer, address, services, payments);
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                if (e instanceof DatabaseException dae) throw dae;
                throw new DatabaseException("Greška pri čitanju detalja računa", e);
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        } catch (DatabaseConnectionException | SQLException e) {
            throw new DatabaseException("Greška pri uspostavi veze prema bazi", e);
        }
    }

    /**
     * Jednostavno dohvaćanje računa po ID-u (bez hidracije).
     */
    public Optional<Invoice> findById(Long id) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            return invoiceDao.findById(conn, id);
        } catch (DatabaseConnectionException | SQLException e) {
            throw new DatabaseException("Greška pri čitanju računa po ID-u", e);
        }
    }

    /**
     * Vraća sve račune (bez hidracije).
     */
    public List<Invoice> findAll() throws DatabaseException {
        return invoiceDao.findAll(); // koristi DAO convenience metodu
    }

    /**
     * DTO pogled na račun s kompletno učitanim povezanim entitetima.
     */
    public record InvoiceView(
            Invoice invoice,
            Freelancer freelancer,
            Address address,
            List<Service> services,
            List<Payment> payments
    ) {}
}
