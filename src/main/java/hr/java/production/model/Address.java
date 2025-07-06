package hr.java.production.model;

import hr.java.production.util.ValidationUtils;

import java.util.Objects;

/**
 * Klasa Address predstavlja podatke o adresi, uključujući naziv ulice, kućni broj, grad i poštanski broj.
 */
public class Address extends Entity {
    private String street;
    private String houseNumber;
    private String city;
    private String postalCode;

    /**
     * Privatni konstruktor za inicijalizaciju adrese s određenim detaljima.
     *
     * @param street      naziv ulice
     * @param houseNumber kućni broj
     * @param city        naziv grada
     * @param postalCode  poštanski broj
     */
    private Address(Long id, String street, String houseNumber, String city, String postalCode) {
        super(id);
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
    }

    /**
     * Builder klasa za kreiranje objekata Address.
     */
    public static class Builder extends Entity.Builder<Address, Builder> {
        private String street;
        private String houseNumber;
        private String city;
        private String postalCode;


        public Builder street(String street) {
            this.street = street;
            return self();
        }

        public Builder houseNumber(String houseNumber) {
            this.houseNumber = houseNumber;
            return self();
        }

        public Builder city(String city) {
            this.city = city;
            return self();
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Address build() {
            // osnovna validacija
            ValidationUtils.validateString(street, "Ulica");
            ValidationUtils.validateString(houseNumber, "Kućni broj");
            ValidationUtils.validateString(city, "Grad");
            ValidationUtils.validatePostalCode(postalCode);
            return new Address(id, street, houseNumber, city, postalCode);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) && Objects.equals(houseNumber, address.houseNumber) && Objects.equals(city, address.city) && Objects.equals(postalCode, address.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), street, houseNumber, city, postalCode);
    }

    public String getStreet() {
        return street;
    }

    public Address setStreet(String street) {
        this.street = street;
        return this;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public Address setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
        return this;
    }

    public String getCity() {
        return city;
    }

    public Address setCity(String city) {
        this.city = city;
        return this;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Address setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }
}