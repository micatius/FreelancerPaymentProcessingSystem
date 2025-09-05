package hr.java.production.service;

import hr.java.production.exception.DatabaseException;
import hr.java.production.log.BinaryChangeLogger;
import hr.java.production.log.ChangeLogger;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;
import hr.java.production.model.Payment;
import hr.java.production.repo.db.AddressDao;
import hr.java.production.repo.db.FreelancerDao;
import hr.java.production.repo.db.InvoiceDao;
import hr.java.production.repo.db.PaymentDao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for Payment domain (1↔1 with Invoice).
 * - Commands: save/update/delete
 * - Queries: always return hydrated results (Payment + Invoice + Freelancer + Address).
 */
public final class PaymentService extends TransactionService {

    private static final String NO_PAYMENT_ID  = "Uplata ne postoji: id=";
    private static final String NO_INVOICE_ID  = "Račun ne postoji: id=";
    private static final String DUP_PAYMENT    = "Račun već ima evidentiranu uplatu: invoiceId=";

    private final PaymentDao paymentDao;
    private final InvoiceDao invoiceDao;
    private final FreelancerDao freelancerDao;
    private final AddressDao addressDao;
    private final ChangeLogger changeLogger;

    public PaymentService(PaymentDao paymentDao,
                          InvoiceDao invoiceDao,
                          FreelancerDao freelancerDao,
                          AddressDao addressDao,
                          ChangeLogger changeLogger) {
        this.paymentDao    = Objects.requireNonNull(paymentDao);
        this.invoiceDao    = Objects.requireNonNull(invoiceDao);
        this.freelancerDao = Objects.requireNonNull(freelancerDao);
        this.addressDao    = Objects.requireNonNull(addressDao);
        this.changeLogger  = Objects.requireNonNull(changeLogger);
    }

    /** Default wiring. */
    public PaymentService() {
        this(new PaymentDao(), new InvoiceDao(), new FreelancerDao(), new AddressDao(), new BinaryChangeLogger());
    }

    /* ---------------------------- write operations ---------------------------- */

    /** Creates a payment; enforces 1↔1 by checking if the invoice already has a payment. Returns new payment ID. */
    public Long save(Payment payment) throws DatabaseException {
        return inTransaction(conn -> {
            if (payment == null) throw new DatabaseException("Uplata ne smije biti null.");
            if (payment.getInvoice() == null || payment.getInvoice().getId() == null) {
                throw new DatabaseException("Uplata mora imati referencu na račun (id).");
            }
            Long invoiceId = payment.getInvoice().getId();

            if (invoiceDao.findById(conn, invoiceId).isEmpty()) {
                throw new DatabaseException(NO_INVOICE_ID + invoiceId);
            }
            if (paymentDao.findByInvoiceId(conn, invoiceId).isPresent()) {
                throw new DatabaseException(DUP_PAYMENT + invoiceId);
            }


            paymentDao.save(conn, payment);
            changeLogger.logCreate(payment);
            return payment.getId();
        }, "Greška pri kreiranju uplate");
    }

    /** Updates a payment. If invoice ref changes, still enforces 1↔1 on the new invoice. */
    public void update(Payment updated) throws DatabaseException {
        inTransaction(conn -> {
            if (updated == null) throw new DatabaseException("Uplata ne smije biti null.");
            Long id = updated.getId();
            if (id == null) throw new DatabaseException("ID uplate ne smije biti null.");

            Payment old = paymentDao.findById(conn, id)
                    .orElseThrow(() -> new DatabaseException(NO_PAYMENT_ID + id));

            if (updated.getInvoice() == null || updated.getInvoice().getId() == null) {
                throw new DatabaseException("Uplata mora imati referencu na račun (id).");
            }
            Long newInvoiceId = updated.getInvoice().getId();

            if (!old.getInvoice().getId().equals(newInvoiceId)) {
                if (invoiceDao.findById(conn, newInvoiceId).isEmpty()) {
                    throw new DatabaseException(NO_INVOICE_ID + newInvoiceId);
                }
                if (paymentDao.findByInvoiceId(conn, newInvoiceId).isPresent()) {
                    throw new DatabaseException(DUP_PAYMENT + newInvoiceId);
                }
            }


            paymentDao.update(conn, updated);
            changeLogger.logUpdate(old, updated);
            return null;
        }, "Greška pri ažuriranju uplate");
    }

    /** Deletes a payment by its ID. */
    public void delete(Long paymentId) throws DatabaseException {
        inTransaction(conn -> {
            Payment old = paymentDao.findById(conn, paymentId)
                    .orElseThrow(() -> new DatabaseException(NO_PAYMENT_ID + paymentId));
            paymentDao.delete(conn, paymentId);
            changeLogger.logDelete(old);
            return null;
        }, "Greška pri brisanju uplate");
    }

    /* ----------------------------- read operations ----------------------------- */

    /** Hydrated read by payment ID. Returns Payment + Invoice + Freelancer + Address. */
    public Optional<Payment> findById(Long id) throws DatabaseException {
        return inTransaction(conn -> {
            Optional<Payment> p = paymentDao.findById(conn, id);
            if (p.isEmpty()) return Optional.empty();
            return Optional.of(buildDetailed(conn, p.get()));
        }, "Greška pri čitanju uplate po ID-u");
    }

    /** 1↔1: returns a fully hydrated Payment by invoiceId. */
    public Optional<Payment> findByInvoiceId(Long invoiceId) throws DatabaseException {
        return inTransaction(conn -> {
            Optional<Payment> p = paymentDao.findByInvoiceId(conn, invoiceId);
            if (p.isEmpty()) return Optional.empty();
            return Optional.of(buildDetailed(conn, p.get()));
        }, "Greška pri čitanju uplate po ID-u računa");
    }

    /** Returns all payments fully hydrated (simple per-row hydration). */
    public List<Payment> findAll() throws DatabaseException {
        return inTransaction(conn -> {
            List<Payment> payments = paymentDao.findAll(conn);
            if (payments.isEmpty()) return List.of();
            List<Payment> out = new ArrayList<>(payments.size());
            for (Payment p : payments) out.add(buildDetailed(conn, p));
            return out;
        }, "Greška pri dohvaćanju svih uplata");
    }

    /* ----------------------------- tiny local helper ----------------------------- */


    private Payment buildDetailed(Connection conn, Payment payment) throws DatabaseException {
        Long invoiceId = (payment.getInvoice() != null) ? payment.getInvoice().getId() : null;
        if (invoiceId == null) return payment;

        // Load full invoice
        Invoice invoice = invoiceDao.findById(conn, invoiceId).orElse(null);
        if (invoice != null && invoice.getFreelancer() != null && invoice.getFreelancer().getId() != null) {
            Long freelancerId = invoice.getFreelancer().getId();
            Freelancer freelancer = freelancerDao.findById(conn, freelancerId).orElse(null);
            if (freelancer != null && freelancer.getAddress() != null && freelancer.getAddress().getId() != null) {
                addressDao.findById(conn, freelancer.getAddress().getId()).ifPresent(freelancer::setAddress);
            }
            if (freelancer != null) invoice.setFreelancer(freelancer);
        }

        payment.setInvoice(invoice);
        return payment;
    }

}
