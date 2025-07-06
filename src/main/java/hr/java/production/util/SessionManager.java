package hr.java.production.util;

import hr.java.production.model.User;

/**
 * Upravlja trenutnim stanjem prijavljenog korisnika unutar aplikacije.
 * Omogućuje postavljanje, dohvat, ili čišćenje trenutnog korisnika iz sesije.
 */
public final class SessionManager {
    private static User currentUser;

    private SessionManager() {}

    /** Postavi tko je trenutno prijavljen. */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /** Vrati trenutno prijavljenog korisnika ili baci IllegalStateException ako nitko nije. */
    public static User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Niti jedan korisnik nije prijavljen");
        }
        return currentUser;
    }

    /** Vrati samo korisničko ime. */
    public static String getCurrentUsername() {
        return getCurrentUser().username();
    }

    /** Očisti session (npr. pri odjavi). */
    public static void clear() {
        currentUser = null;
    }
}