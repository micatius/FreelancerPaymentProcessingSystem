package hr.java.production.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Zapis promjene za entitet tipa T.
 *
 * @param <T> tip entiteta koji se mijenja
 */
public record ChangeLog<T extends Entity>(
        Class<T> type,
        T oldValue,
        T newValue,
        String username,
        LocalDateTime timestamp
) implements Serializable {
    public ChangeLog {
        Objects.requireNonNull(type,      "Tip promjene je obavezan");
        Objects.requireNonNull(username,  "Korisnik je obavezan");
        Objects.requireNonNull(timestamp, "Timestamp je obavezan");
        // oldValue/newValue mogu biti null (za kreiranje/brisanje)
    }

    /**
     * Vraća ime klase entiteta tipa T.
     *
     * @return ime klase za tip entiteta
     */
    public String typeName() {
        return type.getSimpleName();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s promijenjen: %s -> %s od %s",
                timestamp, typeName(), oldValue, newValue, username);
    }
}
