package hr.java.production.model;


import hr.java.production.exception.ObjectValidationException;
import hr.java.production.util.ValidationUtils;

/**
 * Apstraktna klasa Worker predstavlja osnovu za modeliranje različitih radnika s uobičajenim atributima
 * kao što su ime, prezime, email, broj telefona i adresa. Nasljeđuje klasu Entity i implementira sučelje Named.
 */
public abstract class Worker extends Entity implements Named {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Address address;

    protected Worker(Long id) {
        super(id);
    }

    protected Worker(String firstName, String lastName, String email, String phoneNumber, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    protected Worker(Long id, String firstName, String lastName, String email, String phoneNumber, Address address) {
        super(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    /**
     * Generička bazna klasa graditelja za Worker hijerarhiju.
     *
     * @param <T> tip radnika koji se gradi
     * @param <B> tip konkretne klase graditelja
     */
    public abstract static class Builder<T extends Worker, B extends Builder<T, B>> extends Entity.Builder<T, B> {
        protected String firstName;
        protected String lastName;
        protected String email;
        protected String phoneNumber;
        protected Address address;

        public B firstName(String firstName) {
            this.firstName = firstName;
            return self();
        }

        public B lastName(String lastName) {
            this.lastName = lastName;
            return self();
        }

        public B email(String email) {
            this.email = email;
            return self();
        }

        public B phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return self();
        }

        public B address(Address address) {
            this.address = address;
            return self();
        }


        /**
         * Ova metoda osigurava da su obavezna polja prije izgradnje radnika
         * validirana. Ako je bilo koje od provjeravanih polja neispravno ili
         * nedostaje, metoda će baciti {@link ObjectValidationException}.
         *
         * @throws ObjectValidationException ako bilo koji od uvjeta nije zadovoljen.
         */
        protected void validateWorkerFields() {
            ValidationUtils.validateString(firstName, "Ime");
            ValidationUtils.validateString(lastName, "Prezime");
            if (address == null)
                throw new ObjectValidationException("Adresa je obavezna");
            ValidationUtils.validateEmail(email);
            ValidationUtils.validatePhoneNumber(phoneNumber);
        }

        @Override
        protected abstract B self();

        @Override
        public abstract T build();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return String.join(" ", firstName, lastName);
    }

    public String getEmail() {
        return email;
    }

    public Worker setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Worker setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public Worker setAddress(Address address) {
        this.address = address;
        return this;
    }

    @Override
    public String toString() {
        return "Worker{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address=" + address +
                '}';
    }

    /**
     * @return poziciju zaposlenika kao Role enum tip, ovisno o tome da li je freelancer ili zaposlenik
     * i u kojem odjelu radi ako je zaposlenik
     */
    public abstract Role getRole();
}
