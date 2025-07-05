package hr.java.production.model;

import hr.java.production.exception.ValidationException;
import hr.java.production.utils.ValidationUtils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Klasa Service predstavlja pojedinu uslugu s nazivom, jediničnom cijenom i količinom.
 * Nasljeđuje klasu Entity i implementira sučelje Named.
 */
public class Service extends Entity implements Named {
    private String serviceName;
    private BigDecimal unitFee;
    private Integer quantity;

    public Service(String serviceName, BigDecimal unitFee, Integer quantity) {
        this.serviceName = serviceName;
        this.unitFee = unitFee;
        this.quantity = quantity;
    }

    public Service(Long id, String serviceName, BigDecimal unitFee, Integer quantity) {
        super(id);
        this.serviceName = serviceName;
        this.unitFee = unitFee;
        this.quantity = quantity;
    }

    public static class Builder extends Entity.Builder<Service, Builder> {
        private String serviceName;
        private BigDecimal unitFee;
        private Integer quantity;

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
            return new Service(id, serviceName, unitFee, quantity);
        }
    }

    /**
     * Izračunava ukupnu cijenu usluge na temelju jedinične cijene i količine.
     *
     * @return ukupna cijena kao instanca BigDecimal
     */
    public BigDecimal calculateFullCost() {
        return unitFee.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String getName() {
        return serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Service service = (Service) o;
        return Objects.equals(serviceName, service.serviceName) && Objects.equals(unitFee, service.unitFee) && Objects.equals(quantity, service.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serviceName, unitFee, quantity);
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceName='" + serviceName + '\'' +
                ", unitFee=" + unitFee +
                ", quantity=" + quantity +
                '}';
    }
}
