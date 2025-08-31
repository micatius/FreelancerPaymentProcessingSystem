package hr.java.production.log;

import hr.java.production.exception.ObjectValidationException;
import hr.java.production.model.Entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ChangeLog predstavlja logirane promjene nad entitetima.
 * Sadrži informacije o tipu entiteta, vrsti operacije, identifikatoru entiteta,
 * staroj i novoj verziji podatka, korisničkom imenu izvršitelja te vremenskom zapisu.
 *
 * @param <T> tip entiteta koji mora implementirati sučelja Entity i Serializable
 */
public record ChangeLog<T extends Entity & Serializable>(
        Class<T> type,
        Operation op,
        Long entityId,
        T oldValue,
        T newValue,
        String username,
        LocalDateTime timestamp
) implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    public enum Operation { CREATE, UPDATE, DELETE }

    public ChangeLog {
        if (type == null) throw new ObjectValidationException("type is required");
        if (op == null) throw new ObjectValidationException("operation is required");
        if (username == null || username.isBlank()) throw new ObjectValidationException("username is required");
        if (timestamp == null) timestamp = LocalDateTime.now();

        switch (op) {
            case CREATE -> {
                if (newValue == null) throw new ObjectValidationException("CREATE requires newValue");
                if (oldValue != null) throw new ObjectValidationException("CREATE must have oldValue == null");
            }
            case UPDATE -> {
                if (oldValue == null || newValue == null)
                    throw new ObjectValidationException("UPDATE requires oldValue and newValue");
                var oldId = oldValue.getId();
                var newId = newValue.getId();
                if (oldId != null && newId != null && !oldId.equals(newId))
                    throw new ObjectValidationException("UPDATE must keep same entity id");
            }
            case DELETE -> {
                if (oldValue == null) throw new ObjectValidationException("DELETE requires oldValue");
                if (newValue != null) throw new ObjectValidationException("DELETE must have newValue == null");
            }
        }

        if (entityId == null) {
            Long derived = newValue != null ? newValue.getId() : (oldValue != null ? oldValue.getId() : null);
            entityId = derived;
        }
    }

    /**
     * Kreira i vraća instancu ChangeLog za operaciju stvaranja entiteta.
     *
     * @*/
    public static <T extends Entity & Serializable> ChangeLog<T> created(T newValue, String username) {
        Class<T> type = (Class<T>) newValue.getClass();
        return new ChangeLog<>(type, Operation.CREATE, newValue.getId(), null, newValue, username, LocalDateTime.now());
    }

    /**
     * Kreira i vraća instancu ChangeLog za operaciju ažuriranja entiteta.
     *
     * @param oldValue prethodna vrijednost entiteta ili null
     * @param newValue nova vrijednost entiteta ili null
     * @param username korisničko ime osobe koja je izvršila promjenu
     * @param <T>      tip entiteta koji implementira Entity i Serializable
     * @return instanca ChangeLog koja predstavlja zapis promjene
     */
    public static <T extends Entity & Serializable> ChangeLog<T> updated(T oldValue, T newValue, String username) {
        Class<T> type = (Class<T>) (oldValue != null ? oldValue.getClass() : newValue.getClass());
        Long id = newValue != null ? newValue.getId() : (oldValue != null ? oldValue.getId() : null);
        return new ChangeLog<>(type, Operation.UPDATE, id, oldValue, newValue, username, LocalDateTime.now());
    }

    /**
     * Kreira i vraća instancu ChangeLog za operaciju brisanja entiteta.
     *
     * @param oldValue prethodna vrijednost entiteta koji se briše
     * @param username korisničko ime osobe koja je izvršila brisanje
     * @param <T>      tip entiteta koji implementira Entity i Serializable
     * @return instanca ChangeLog koja predstavlja zapis brisanja
     */
    public static <T extends Entity & Serializable> ChangeLog<T> deleted(T oldValue, String username) {
        Class<T> type = (Class<T>) oldValue.getClass();
        return new ChangeLog<>(type, Operation.DELETE, oldValue.getId(), oldValue, null, username, LocalDateTime.now());
    }
}
