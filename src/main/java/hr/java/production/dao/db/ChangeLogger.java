package hr.java.production.dao.db;

import hr.java.production.model.ChangeLog;
import hr.java.production.model.Entity;
import hr.java.production.util.SessionManager;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;

/**
 * Sučelje za zapisivanje promjena nad entitetima koristeći ChangeLog zapise.
 * Sučelje omogućava definiranje osnovnog mehanizma za trajno spremanje zapisa promjena.
 */
public sealed interface ChangeLogger permits FreelancerDao, InvoiceDao, PaymentDao {
    Path LOG_FILE = Paths.get("dat/bin/changelog.bin");

    /**
     * Zapiši promjenu: kreira ChangeLog<T> i serializira ga u datoteku.
     *
     * @param type     tip entiteta koji se mijenja
     * @param oldValue stara vrijednost (null za CREATE)
     * @param newValue nova vrijednost (null za DELETE)
     * @param <T>      tip entiteta
     */
    default <T extends Entity> void logChange(
            Class<T> type,
            T oldValue,
            T newValue
    ) {
        ChangeLog<T> entry = new ChangeLog<>(
                type,
                oldValue,
                newValue,
                SessionManager.getCurrentUser().username(),
                LocalDateTime.now()
        );

        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(
                        Files.newOutputStream(LOG_FILE,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND))))
        {
            out.writeObject(entry);
        } catch (IOException e) {
            throw new UncheckedIOException("Greška u pisanju ChangeLoga", e);
        }
    }
}
