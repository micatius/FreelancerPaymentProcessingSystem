package hr.java.production.util;

import hr.java.production.model.User;

/**
 * Klasa koja upravlja trenutnom sesijom korisnika.
 * Omogućuje postavljanje, dohvaćanje i brisanje trenutnog korisnika,
 * kao i dohvaćanje korisničkog imena trenutnog korisnika.
 */
public final class SessionManager {
    private static User currentUser;

    private SessionManager() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Niti jedan korisnik nije prijavljen");
        }
        return currentUser;
    }

    public static String getCurrentUsername() {
        return getCurrentUser().username();
    }

    public static void clear() {
        currentUser = null;
    }
}