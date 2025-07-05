package hr.java.production.model;

import hr.java.production.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Klasa koja predstavlja uplatu za određeni račun.
 */
public class Payment extends Entity {
    private Invoice invoice;
    private BigDecimal amount;
    private LocalDateTime paidOn;
    private String transactionId;

    /**
     * Konstruktor za kreiranje uplate bez ID-ja.
     *
     * @param invoice       račun koji se plaća
     * @param amount        iznos uplate
     * @param paidOn        datum i vrijeme uplate
     * @param transactionId opcionalni ID transakcije
     */
    protected Payment(Invoice invoice,
                      BigDecimal amount,
                      LocalDateTime paidOn,
                      String transactionId) {
        this(null, invoice, amount, paidOn, transactionId);
    }

    /**
     * Konstruktor za kreiranje uplate s ID-jem.
     *
     * @param id            jedinstveni identifikator uplate
     * @param invoice       račun koji se plaća
     * @param amount        iznos uplate
     * @param paidOn        datum i vrijeme uplate
     * @param transactionId opcionalni ID transakcije
     */
    protected Payment(Long id,
                      Invoice invoice,
                      BigDecimal amount,
                      LocalDateTime paidOn,
                      String transactionId) {
        super(id);
        this.invoice = invoice;
        this.amount = amount;
        this.paidOn = paidOn;
        this.transactionId = transactionId;
    }

    /**
     * Fluent Builder za Payment.
     */
    public static class Builder extends Entity.Builder<Payment, Builder> {
        private Invoice invoice;
        private BigDecimal amount;
        private LocalDateTime paidOn;
        private String transactionId;

        /**
         * Postavlja račun za uplatu.
         *
         * @param invoice objekt računa
         * @return instanca Buildera
         */
        public Builder invoice(Invoice invoice) {
            this.invoice = invoice;
            return self();
        }

        /**
         * Postavlja iznos uplate.
         *
         * @param amount iznos
         * @return instanca Buildera
         */
        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return self();
        }

        /**
         * Postavlja datum i vrijeme uplate.
         *
         * @param paidOn datum i vrijeme
         * @return instanca Buildera
         */
        public Builder paidOn(LocalDateTime paidOn) {
            this.paidOn = paidOn;
            return self();
        }

        /**
         * Postavlja ID transakcije (opcionalno).
         *
         * @param transactionId ID transakcije
         * @return instanca Buildera
         */
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Payment build() {
            if (invoice == null) {
                throw new ValidationException("Račun je obavezan");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Iznos uplate mora biti pozitivan");
            }
            if (paidOn == null) {
                throw new ValidationException("Datum plaćanja je obavezan");
            }
            if (transactionId == null) {
                throw new ValidationException("ID transakcije je obavezan.");
            }
            return new Payment(id, invoice, amount, paidOn, transactionId);
        }
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(LocalDateTime paidOn) {
        this.paidOn = paidOn;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Payment payment = (Payment) o;
        return Objects.equals(invoice, payment.invoice) && Objects.equals(amount, payment.amount) && Objects.equals(paidOn, payment.paidOn) && Objects.equals(transactionId, payment.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), invoice, amount, paidOn, transactionId);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "invoice=" + invoice +
                ", amount=" + amount +
                ", paidOn=" + paidOn +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}