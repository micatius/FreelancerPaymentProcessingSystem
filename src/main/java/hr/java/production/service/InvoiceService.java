package hr.java.production.service;

import hr.java.production.exception.DatabaseException;
import hr.java.production.log.BinaryChangeLogger;
import hr.java.production.log.ChangeLogger;
import hr.java.production.model.Address;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;
import hr.java.production.model.Payment;
import hr.java.production.model.Service;
import hr.java.production.repo.db.AddressDao;
import hr.java.production.repo.db.FreelancerDao;
import hr.java.production.repo.db.InvoiceDao;
import hr.java.production.repo.db.PaymentDao;
import hr.java.production.repo.db.ServiceDao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public final class InvoiceService extends TransactionService {
    private static final String NO_INVOICE_ID = "Račun ne postoji: id=";

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
        this(new InvoiceDao(), new ServiceDao(), new PaymentDao(), new FreelancerDao(), new AddressDao(), new BinaryChangeLogger());
    }

    /* ---------------------------- write operations ---------------------------- */


    /** Kreira fakturu (+stavke) i vraća potpuno hidrirani pogled. */
    public Long save(Invoice invoice) throws DatabaseException {
        return inTransaction(conn -> {
            invoiceDao.save(conn, invoice);
            Long invId = invoice.getId();

            // persist services if present
            List<Service> services = invoice.getServices();
            if (services != null) {
                for (Service s : services) {
                    s.setInvoiceId(invId);
                    serviceDao.save(conn, s);
                }
            }

            changeLogger.logCreate(invoice);
            return invId;
        }, "Greška pri kreiranju računa");
    }

    /** Ažurira fakturu, zamjenjuje stavke i vraća potpuno hidrirani pogled. */
    public void update(Invoice updated) throws DatabaseException {
        inTransaction(conn -> {
            Long invId = Objects.requireNonNull(updated.getId(), "ID ne smije biti null");
            Invoice old = invoiceDao.findById(conn, invId)
                    .orElseThrow(() -> new DatabaseException(NO_INVOICE_ID + invId));

            invoiceDao.update(conn, updated);

            // replace services
            serviceDao.deleteByInvoiceId(conn, invId);
            List<Service> services = updated.getServices();
            if (services != null) {
                for (Service s : services) {
                    s.setInvoiceId(invId);
                    serviceDao.save(conn, s);
                }
            }

            changeLogger.logUpdate(old, updated);
            return null;
        }, "Greška pri ažuriranju računa");
    }

    /** Briše fakturu i sve povezane entitete. */
    public void delete(Long invoiceId) throws DatabaseException {
        inTransaction(conn -> {
            Invoice old = invoiceDao.findById(conn, invoiceId)
                    .orElseThrow(() -> new DatabaseException(NO_INVOICE_ID + invoiceId));

            serviceDao.deleteByInvoiceId(conn, invoiceId);
            paymentDao.deleteByInvoiceId(conn, invoiceId);
            invoiceDao.delete(conn, invoiceId);

            changeLogger.logDelete(old);
            return null;
        }, "Greška pri brisanju računa");
    }

    /* ----------------------------- read operations ----------------------------- */

    /** Vraća potpuno hidriran Optional prikaz jednog računa. */
    public Optional<InvoiceView> findById(Long id) throws DatabaseException {
        return inTransaction(conn -> {
            Optional<Invoice> base = invoiceDao.findById(conn, id);
            if (base.isEmpty()) return Optional.empty();
            return Optional.of(toView(conn, base.get()));   // single-invoice toView
        }, "Greška pri čitanju računa po ID-u");
    }

    /** Vraća SVE račune potpuno hidrirane — koristi batch toView za nisku složenost i visoku izvedbu. */
    public List<InvoiceView> findAll() throws DatabaseException {
        return inTransaction(conn -> {
            List<Invoice> invoices = invoiceDao.findAll(conn);
            if (invoices.isEmpty()) return List.of();
            return toView(conn, invoices);
        }, "Greška pri čitanju svih računa");
    }

    /* ----------------------------- toView helpers ----------------------------- */

    /** Single-invoice toView: loads Freelancer (+Address), Services, Payment. */
    private InvoiceView toView(Connection conn, Invoice invoice) throws DatabaseException {
        try {
            // freelancer (+address)
            if (invoice.getFreelancer() != null && invoice.getFreelancer().getId() != null) {
                Long fId = invoice.getFreelancer().getId();
                Freelancer f = freelancerDao.findById(conn, fId).orElse(null);
                if (f != null && f.getAddress() != null && f.getAddress().getId() != null) {
                    Address a = addressDao.findById(conn, f.getAddress().getId()).orElse(null);
                    if (a != null) f.setAddress(a);
                }
                if (f != null) invoice.setFreelancer(f);
            }

            List<Service> services = serviceDao.findByInvoiceId(conn, invoice.getId());
            invoice.setServices(services);

            Payment payment = paymentDao.findByInvoiceId(conn, invoice.getId()).orElse(null);

            return new InvoiceView(invoice, payment);
        } catch (Exception e) {
            throw (e instanceof DatabaseException de) ? de
                    : new DatabaseException("Greška pri konverziji računa u pogled (id=" + invoice.getId() + ")", e);
        }
    }

    /**
     * Batch toView: performs O(1) queries per table (no N+1) and stitches views.
     */
    private List<InvoiceView> toView(Connection conn, List<Invoice> invoices) throws DatabaseException {
        try {
            Set<Long> invoiceIds = invoices.stream().map(Invoice::getId).collect(Collectors.toSet());
            Set<Long> freelancerIds = invoices.stream()
                    .map(inv -> inv.getFreelancer() != null ? inv.getFreelancer().getId() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());


            Map<Long, Freelancer> freelancers = freelancerDao.findByIds(conn, freelancerIds);

            Set<Long> addressIds = freelancers.values().stream()
                    .map(f -> f.getAddress() != null ? f.getAddress().getId() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, Address> addresses = addressDao.findByIds(conn, addressIds);

            Map<Long, List<Service>> servicesByInv = serviceDao.findByInvoiceIds(conn, invoiceIds);

            Map<Long, Payment> paymentByInv = paymentDao.findByInvoiceIds(conn, invoiceIds);

            for (Invoice inv : invoices) {
                if (inv.getFreelancer() != null && inv.getFreelancer().getId() != null) {
                    Freelancer f = freelancers.get(inv.getFreelancer().getId());
                    if (f != null && f.getAddress() != null && f.getAddress().getId() != null) {
                        Address a = addresses.get(f.getAddress().getId());
                        if (a != null) f.setAddress(a);
                    }
                    if (f != null) inv.setFreelancer(f);
                }
                // services
                List<Service> svcs = servicesByInv.get(inv.getId());
                if (svcs != null) inv.setServices(svcs);
            }

            List<InvoiceView> out = new ArrayList<>(invoices.size());
            for (Invoice inv : invoices) {
                out.add(new InvoiceView(inv, paymentByInv.get(inv.getId())));
            }
            return out;

        } catch (Exception e) {
            throw (e instanceof DatabaseException de) ? de
                    : new DatabaseException("Greška pri batch konverziji računa u poglede", e);
        }
    }

    /**
     * Predstavlja prikaz fakture sa svim relevantnim podacima, uključujući informacije o uplati.
     * Omogućuje provjeru statusa plaćenosti na temelju vezane uplate.
     */
    public record InvoiceView(
            Invoice invoice,
            Payment payment
    ) {

        /**
         * Provjerava je li faktura plaćena na temelju postojanja uplate.
         *
         * @return true ako postoji povezana uplata, inače false
         */
        public boolean isPaid() {
            return payment != null;
        }
    }
}
