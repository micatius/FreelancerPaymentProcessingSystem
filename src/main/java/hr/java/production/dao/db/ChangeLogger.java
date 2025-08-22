//package hr.java.production.dao.db;
//
//import hr.java.production.exception.BinaryFileReadException;
//import hr.java.production.exception.BinaryFileWriteException;
//import hr.java.production.model.ChangeLog;
//import hr.java.production.model.Entity;
//import hr.java.production.util.SessionManager;
//
//import java.io.*;
//import java.nio.channels.FileChannel;
//import java.nio.file.*;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Sučelje za zapisivanje promjena nad entitetima koristeći ChangeLog zapise.
// * Sučelje omogućava definiranje osnovnog mehanizma za trajno spremanje zapisa promjena.
// */
//public sealed interface ChangeLogger permits FreelancerDao, InvoiceDao, PaymentDao {
//    Path LOG_FILE = Paths.get("dat/bin/changelog.bin");
//
//    /**
//     * Zapiši promjenu: kreira ChangeLog<T> i serializira ga u datoteku.
//     *
//     * @param type     tip entiteta koji se mijenja
//     * @param oldValue stara vrijednost (null za CREATE)
//     * @param newValue nova vrijednost (null za DELETE)
//     * @param <T>      tip entiteta
//     */
//    default <T extends Entity> void logChange(
//            Class<T> type,
//            T oldValue,
//            T newValue
//    ) {
//        ChangeLog<T> entry = new ChangeLog<>(
//                type,
//                oldValue,
//                newValue,
//                SessionManager.getCurrentUser().username(),
//                LocalDateTime.now()
//        );
//
//        writeBinary(entry);
//    }
//
//    private static void writeBinary(Object entry) {
//        synchronized (ChangeLogger.class) {
//            try {
//                Files.createDirectories(LOG_FILE.getParent());
//                boolean append = Files.exists(LOG_FILE) && Files.size(LOG_FILE) > 0;
//
//                try (OutputStream os = new BufferedOutputStream(
//                        Files.newOutputStream(LOG_FILE, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
//                     ObjectOutputStream oos = append
//                             ? new AppendableObjectOutputStream(os)
//                             : new ObjectOutputStream(os)) {
//
//                    oos.writeObject(entry);
//                    oos.flush();
//                }
//            } catch (IOException e) {
//                throw new BinaryFileWriteException("Greška u pisanju ChangeLoga", e);
//            }
//        }
//    }
//
//    static List<ChangeLog<Entity>> readAll() {
//        synchronized (ChangeLogger.class) {
//            if (!Files.exists(LOG_FILE)) return List.of();
//
//            List<ChangeLog<Entity>> logs = new ArrayList<>();
//
//            try (FileInputStream fis = new FileInputStream(LOG_FILE.toFile())) {
//                FileChannel ch  = fis.getChannel();
//                long end = ch.size();
//
//                try (var bis = new BufferedInputStream(fis);
//                     var ois = new ObjectInputStream(bis)) {
//
//                    while (ch.position() < end) {
//                        Object obj = ois.readObject();
//                        if (obj instanceof ChangeLog<?> tmp) {
//                            @SuppressWarnings("unchecked")
//                            ChangeLog<Entity> casted = (ChangeLog<Entity>) tmp;
//                            logs.add(casted);
//                        }
//                    }
//                }
//            } catch (IOException | ClassNotFoundException e) {
//                throw new BinaryFileReadException("Greška pri čitanju ChangeLoga", e);
//            }
//
//            return logs;
//        }
//    }
//
//    /**
//     * Klasa AppendableObjectOutputStream omogućava zapisivanje serijaliziranih objekata
//     * u postojeći ObjectOutputStream bez ponovno dodavanja zaglavlja.
//     */
//    final class AppendableObjectOutputStream extends ObjectOutputStream {
//        public AppendableObjectOutputStream(OutputStream out) throws IOException { super(out); }
//        @Override protected void writeStreamHeader() throws IOException { reset(); }
//    }
//}
