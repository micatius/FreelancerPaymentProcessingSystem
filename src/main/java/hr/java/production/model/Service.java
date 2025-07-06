package hr.java.production.model;

import hr.java.production.exception.ValidationException;
import hr.java.production.util.ValidationUtils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Klasa Service predstavlja pojedinu uslugu s nazivom, jediničnom cijenom i količinom.
 * Nasljeđuje klasu Entity i implementira sučelje Named.
 */
public class Service extends Entity implements Named {
    private Long invoiceId;
    private String name;
    private BigDecimal unitFee;
    private Integer quantity;


    private Service(Long id, Long invoiceId, String name, BigDecimal unitFee, Integer quantity) {
        super(id);
        this.invoiceId = invoiceId;
        this.name = name;
        this.unitFee = unitFee;
        this.quantity = quantity;
    }

    public static class Builder extends Entity.Builder<Service, Builder> {
        private Long invoiceId;
        private String serviceName;
        private BigDecimal unitFee;
        private Integer quantity;

        public Builder invoiceId(Long invoiceId) {
            this.invoiceId = invoiceId;
            return self();
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return self();
        }

        public Builder unitFee(BigDecimal unitFee) {
            this.unitFee = unitFee;
            return self();
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Service build() {
            ValidationUtils.validateString(serviceName, "Naziv usluge");
            if (unitFee == null || unitFee.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Jedinična cijena usluge mora biti broj veći od 0.");
            }
            if (quantity == null || quantity < 1) {
                throw new ValidationException("Količina usluge mora biti cijeli broj veći od 0");
            }
            return new Service(id, invoiceId, serviceName, unitFee, quantity);
        }
    }

    /**
     * Izračunava ukupnu cijenu usluge na temelju jedinične cijene i količine.
     *
     * @return ukupna cijena kao instanca BigDecimal
     */
    public BigDecimal calculateTotalCost() {
        return unitFee.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getUnitFee() {
        return unitFee;
    }

    public void setUnitFee(BigDecimal unitFee) {
        this.unitFee = unitFee;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Service service = (Service) o;
        return Objects.equals(invoiceId, service.invoiceId) &&
                Objects.equals(name, service.name) &&
                Objects.equals(unitFee, service.unitFee) &&
                Objects.equals(quantity, service.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), invoiceId, name, unitFee, quantity);
    }

    @Override
    public String toString() {
        return "Service{" +
                "invoiceId=" + invoiceId +
                ", serviceName='" + name + '\'' +
                ", unitFee=" + unitFee +
                ", quantity=" + quantity +
                '}';
    }
}
