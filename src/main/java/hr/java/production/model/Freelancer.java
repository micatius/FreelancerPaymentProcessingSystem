package hr.java.production.model;

import hr.java.production.exception.ObjectValidationException;

/**
 * Klasa Freelancer predstavlja samostalnog radnika s dodatnim atributima
 * kao što su naziv poslovanja, identifikacijski broj poslovanja, broj bankovnog računa
 * i status aktivnosti. Nasljeđuje klasu Worker.
 */
public final class Freelancer extends Worker {
    private String businessName;
    private String businessIdentificationNumber;
    private String bankAccountNumber;
    private Boolean active;

    private Freelancer(Long id) {
        super(id);
    }


    private Freelancer(Builder builder) {
        super(builder.id, builder.firstName, builder.lastName, builder.email, builder.phoneNumber, builder.address);
        this.businessName = builder.businessName;
        this.businessIdentificationNumber = builder.businessIdentificationNumber;
        this.bankAccountNumber = builder.bankAccountNumber;
        this.active = builder.active;
    }

    public static class Builder extends Worker.Builder<Freelancer, Builder> {
        private String businessName;
        private String businessIdentificationNumber;
        private String bankAccountNumber;
        private Boolean active;

        public Builder businessName(String businessName) {
            this.businessName = businessName;
            return self();
        }

        public Builder businessIdentificationNumber(String businessIdentificationNumber) {
            this.businessIdentificationNumber = businessIdentificationNumber;
            return self();
        }

        public Builder bankAccountNumber(String bankAccountNumber) {
            this.bankAccountNumber = bankAccountNumber;
            return self();
        }

        public Builder active(Boolean active) {
            this.active = active;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Freelancer build() {
            validateWorkerFields();
            if (businessName == null || businessName.isBlank()) {
                throw new ObjectValidationException("Naziv poslovanja ne smije biti prazan");
            }
            if (businessIdentificationNumber == null || businessIdentificationNumber.isBlank()) {
                throw new ObjectValidationException("Identifikacijski broj ne smije biti prazan");
            }
            if (bankAccountNumber == null || !bankAccountNumber.matches("^HR\\d{2}[0-9A-Z]{17}$")) {
                throw new ObjectValidationException("Broj bankovnog računa mora biti valjan IBAN");
            }
            if (active == null) {
                throw new ObjectValidationException("Status aktivnosti mora biti postavljen");
            }
            return new Freelancer(this);
        }
    }

    public static Freelancer ref(Long id) {
        if (id == null) throw new ObjectValidationException("ID je obavezan za referencu freelancera.");
        return new Freelancer(id);
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

}
