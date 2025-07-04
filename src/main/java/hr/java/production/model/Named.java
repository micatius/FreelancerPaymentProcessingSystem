package hr.java.production.model;

/**
 * Predstavlje entite koji imaju ime i prezime, koja se koriste kako bi se saznalo puno ime.
 * Klase koje implementiraju ovo sučelje moraju implementirati metode za dohvat imena i prezimena.
 */
public interface Named {
    abstract String getName();
}
