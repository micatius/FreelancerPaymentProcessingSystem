package hr.java.production.utils;

import hr.java.production.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Pomagalo za razne validacijske provjere.
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{10}$");

    private static final Pattern POSTAL_CODE =
            Pattern.compile("\\d{5}");

    /**
     * Provjerava je li dani tekst null ili prazan.
     *
     * @param value     tekst koji validiramo
     * @param fieldName naziv polja (npr. "Ulica", "Grad")
     * @throws ValidationException ako je tekst null ili prazan
     */
    public static void validateString(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            String niceName = fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
            throw new ValidationException(niceName + " ne smije biti prazno");
        }
    }
    /**
     * Provjerava je li email validan prema zadanoj regex logici.
     * @param email adresa za provjeru
     * @throws ValidationException ako email nije u ispravnom formatu
     */
    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Email adresa nije ispravna");
        }
    }

    /**
     * Provjerava je li telefonski broj sastavljen od točno 10 znamenki.
     * @param phoneNumber broj za provjeru
     * @throws ValidationException ako telefonski broj nema točno 10 znamenki
     */
    public static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new ValidationException("Telefonski broj mora imati 10 znamenki");
        }
    }

    /**
     * Provjerava je li pošstanski broj sastavljen od točno 5 znamenki.
     * @param postalCode broj za provjeru
     * @throws ValidationException ako pošstanski broj nema točno 5 znamenki
     */
    public static void validatePostalCode(String postalCode) {
        if (postalCode == null || !POSTAL_CODE.matcher(postalCode).matches()) {
            throw new ValidationException("Poštanski broj mora imati 5 znamenki");
        }
    }

    // Po potrebi dodaj OIB i IBAN provjere:
    // public static void validateOib(String oib) { … }
    // public static void validateIban(String iban) { … }
}
