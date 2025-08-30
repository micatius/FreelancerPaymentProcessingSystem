package hr.java.production.dao.log;

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

    // Compact canonical constructor - enforces invariants and fills defaults
    public ChangeLog {
        if (type == null) throw new IllegalArgumentException("type is required");
        if (op == null) throw new IllegalArgumentException("operation is required");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username is required");
        if (timestamp == null) timestamp = LocalDateTime.now();

        switch (op) {
            case CREATE -> {
                if (newValue == null) throw new IllegalArgumentException("CREATE requires newValue");
                if (oldValue != null) throw new IllegalArgumentException("CREATE must have oldValue == null");
            }
            case UPDATE -> {
                if (oldValue == null || newValue == null)
                    throw new IllegalArgumentException("UPDATE requires oldValue and newValue");
                var oldId = oldValue.getId();
                var newId = newValue.getId();
                if (oldId != null && newId != null && !oldId.equals(newId))
                    throw new IllegalArgumentException("UPDATE must keep same entity id");
            }
            case DELETE -> {
                if (oldValue == null) throw new IllegalArgumentException("DELETE requires oldValue");
                if (newValue != null) throw new IllegalArgumentException("DELETE must have newValue == null");
            }
        }

        // Derive entityId if not provided
        if (entityId == null) {
            Long derived = newValue != null ? newValue.getId() : (oldValue != null ? oldValue.getId() : null);
            entityId = derived;
        }
    }

    // Factory for CREATE
    public static <T extends Entity & Serializable> ChangeLog<T> created(T newValue, String username) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) newValue.getClass();
        return new ChangeLog<>(type, Operation.CREATE, newValue.getId(), null, newValue, username, LocalDateTime.now());
    }

    // Factory for UPDATE
    public static <T extends Entity & Serializable> ChangeLog<T> updated(T oldValue, T newValue, String username) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) (oldValue != null ? oldValue.getClass() : newValue.getClass());
        Long id = newValue != null ? newValue.getId() : (oldValue != null ? oldValue.getId() : null);
        return new ChangeLog<>(type, Operation.UPDATE, id, oldValue, newValue, username, LocalDateTime.now());
    }

    // Factory for DELETE
    public static <T extends Entity & Serializable> ChangeLog<T> deleted(T oldValue, String username) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) oldValue.getClass();
        return new ChangeLog<>(type, Operation.DELETE, oldValue.getId(), oldValue, null, username, LocalDateTime.now());
    }
}
