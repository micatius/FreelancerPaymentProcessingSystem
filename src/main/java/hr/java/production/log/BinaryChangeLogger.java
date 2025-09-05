package hr.java.production.log;

import hr.java.production.exception.BinaryFileReadException;
import hr.java.production.exception.BinaryFileWriteException;
import hr.java.production.model.Entity;
import hr.java.production.util.SessionManager;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacija ChangeLogger koja koristi binarnu datoteku za spremanje zapisa.
 */
public final class BinaryChangeLogger implements ChangeLogger {

    private static final Path LOG_FILE = Paths.get("dat/bin/changelog.bin");

    @Override
    public <T extends Entity & Serializable> void logCreate(T newValue) {
        String user = SessionManager.getCurrentUser().username();
        ChangeLog<T> entry = ChangeLog.created(newValue, user);
        writeBinary(entry);
    }

    @Override
    public <T extends Entity & Serializable> void logUpdate(T oldValue, T newValue) {
        String user = SessionManager.getCurrentUser().username();
        ChangeLog<T> entry = ChangeLog.updated(oldValue, newValue, user);
        writeBinary(entry);
    }

    @Override
    public <T extends Entity & Serializable> void logDelete(T oldValue) {
        String user = SessionManager.getCurrentUser().username();
        ChangeLog<T> entry = ChangeLog.deleted(oldValue, user);
        writeBinary(entry);
    }

    public List<ChangeLog<Entity>> readAll() {
        List<ChangeLog<Entity>> out = new ArrayList<>();
        for (ChangeLog<? extends Entity> raw : readAllRaw()) {
            @SuppressWarnings("unchecked")
            ChangeLog<Entity> casted = (ChangeLog<Entity>) raw;
            out.add(casted);
        }
        return out;
    }

    @Override
    public <T extends Entity & Serializable> List<ChangeLog<T>> readAll(Class<T> type) {
        List<ChangeLog<? extends Entity>> all = readAllRaw();
        List<ChangeLog<T>> out = new ArrayList<>();
        for (ChangeLog<? extends Entity> raw : all) {
            if (raw.type().equals(type)) {
                @SuppressWarnings("unchecked")
                ChangeLog<T> casted = (ChangeLog<T>) raw;
                out.add(casted);
            }
        }
        return out;
    }


    private static void writeBinary(Object entry) {
        synchronized (BinaryChangeLogger.class) {
            try {
                Files.createDirectories(LOG_FILE.getParent());
                boolean append = Files.exists(LOG_FILE) && Files.size(LOG_FILE) > 0;

                try (OutputStream os = new BufferedOutputStream(
                        Files.newOutputStream(LOG_FILE, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
                     ObjectOutputStream oos = append
                             ? new AppendableObjectOutputStream(os)
                             : new ObjectOutputStream(os)) {
                    oos.writeObject(entry);
                    oos.flush();
                }
            } catch (IOException e) {
                throw new BinaryFileWriteException("Greška pri pisanju ChangeLog zapisa", e);
            }
        }
    }

    private static List<ChangeLog<? extends Entity>> readAllRaw() {
        synchronized (BinaryChangeLogger.class) {
            if (!Files.exists(LOG_FILE)) return List.of();

            List<ChangeLog<? extends Entity>> logs = new ArrayList<>();
            try (InputStream fis = Files.newInputStream(LOG_FILE);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {

                while (true) {
                    Object obj = ois.readObject();
                    if (obj instanceof ChangeLog<?> tmp) {
                        logs.add(tmp);
                    }
                }
            } catch (EOFException _) {
                // kraj filea
            } catch (IOException | ClassNotFoundException e) {
                throw new BinaryFileReadException("Greška pri čitanju ChangeLog zapisa", e);
            }
            return logs;
        }
    }

    /** Onemogućuje ponovno pisanje headera prilikom append-a. */
    private static final class AppendableObjectOutputStream extends ObjectOutputStream {
        AppendableObjectOutputStream(OutputStream out) throws IOException { super(out); }
        @Override protected void writeStreamHeader() throws IOException { reset(); }
    }
}
