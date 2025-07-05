package hr.java.production.model;

import hr.java.production.exception.ValidationException;

import java.util.Objects;

public final class Freelancer extends Worker {
    private String businessName;
    private String businessIdentificationNumber;
    private String bankAccountNumber;
    private Boolean active;

    public Freelancer(String firstName, String lastName, String email, String phoneNumber, Address address, String businessName, String businessIdentificationNumber, String bankAccountNumber, Boolean active) {
        super(firstName, lastName, email, phoneNumber, address);
        this.businessName = businessName;
        this.businessIdentificationNumber = businessIdentificationNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.active = active;
    }

    public Freelancer(Long id, String firstName, String lastName, String email, String phoneNumber, Address address, String businessName, String businessIdentificationNumber, String bankAccountNumber, Boolean active) {
        super(id, firstName, lastName, email, phoneNumber, address);
        this.businessName = businessName;
        this.businessIdentificationNumber = businessIdentificationNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.active = active;
    }

    public static class Builder extends Worker.Builder<Freelancer, Builder> {
        private String businessName;
        private String businessIdentificationNumber;
        private String bankAccountNumber;
        private Boolean active;

        /**
         * Postavlja naziv poslovanja.
         *
         * @param businessName naziv poslovanja
         * @return instanca Buildera
         */
        public Builder businessName(String businessName) {
            this.businessName = businessName;
            return self();
        }

        /**
         * Postavlja identifikacijski broj (OIB ili registracijski broj).
         *
         * @param businessIdentificationNumber OIB ili registracijski broj
         * @return instanca Buildera
         */
        public Builder businessIdentificationNumber(String businessIdentificationNumber) {
            this.businessIdentificationNumber = businessIdentificationNumber;
            return self();
        }

        /**
         * Postavlja broj bankovnog računa.
         *
         * @param bankAccountNumber broj bankovnog računa
         * @return instanca Buildera
         */
        public Builder bankAccountNumber(String bankAccountNumber) {
            this.bankAccountNumber = bankAccountNumber;
            return self();
        }

        /**
         * Postavlja status aktivnosti freelancera.
         *
         * @param active true ako je aktivan, inače false
         * @return instanca Buildera
         */
        public Builder active(Boolean active) {
            this.active = active;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        // VALIDACIJE U UTIL KAO POSEBNE METODE, MOŽDA POSEBNI EXCEPTION ZA SVAKI TIP VALIDACIJE??
        @Override
        public Freelancer build() {
            validateWorkerFields();
            if (businessName == null || businessName.isBlank()) {
                throw new ValidationException("Naziv poslovanja ne smije biti prazan");
            }
            if (businessIdentificationNumber == null || businessIdentificationNumber.isBlank()) {
                throw new ValidationException("Identifikacijski broj ne smije biti prazan");
            }
            if (bankAccountNumber == null || !bankAccountNumber.matches("^HR\\d{2}[0-9A-Z]{17}$")) {
                throw new ValidationException("Broj bankovnog računa mora biti valjan IBAN");
            }
            if (active == null) {
                throw new ValidationException("Status aktivnosti mora biti postavljen");
            }
            return new Freelancer(
                    id,
                    firstName,
                    lastName,
                    email,
                    phoneNumber,
                    address,
                    businessName,
                    businessIdentificationNumber,
                    bankAccountNumber,
                    active
            );
        }
    }

    @Override
    public String toString() {
        return "Freelancer{" +
                "businessName='" + businessName + '\'' +
                ", businessIdentificationNumber='" + businessIdentificationNumber + '\'' +
                ", bankAccountNumber='" + bankAccountNumber + '\'' +
                ", active=" + active +
                '}';
    }

    public String getBusinessName() {
        return businessName;
    }

    public Freelancer setBusinessName(String businessName) {
        this.businessName = businessName;
        return this;
    }

    public String getBusinessIdentificationNumber() {
        return businessIdentificationNumber;
    }

    public Freelancer setBusinessIdentificationNumber(String businessIdentificationNumber) {
        this.businessIdentificationNumber = businessIdentificationNumber;
        return this;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public Freelancer setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    public Freelancer setActive(Boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public Role getRole() {
        return Role.FREELANCER;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Freelancer that = (Freelancer) o;
        return Objects.equals(businessName, that.businessName) && Objects.equals(businessIdentificationNumber, that.businessIdentificationNumber) && Objects.equals(bankAccountNumber, that.bankAccountNumber) && Objects.equals(active, that.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), businessName, businessIdentificationNumber, bankAccountNumber, active);
    }
}
