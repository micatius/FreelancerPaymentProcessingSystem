package hr.java.production.thread;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Role;
import hr.java.production.service.InvoiceService;
import hr.java.production.util.SessionManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.concurrent.*;

public final class FinanceOverdueRefresher implements AutoCloseable {
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "FinanceOverdueRefresher");
        t.setDaemon(true);
        return t;
    });

    private final InvoiceService invoiceService = new InvoiceService();
    private final Label banner;
    private final long periodSeconds;
    private final Logger logger = LoggerFactory.getLogger(FinanceOverdueRefresher.class);

    public FinanceOverdueRefresher(Label banner, long periodSeconds) {
        this.banner = banner;
        this.periodSeconds = periodSeconds <= 0 ? 10 : periodSeconds;
    }

    public void start() {
        if (SessionManager.getCurrentUser().role() != Role.FINANCE) return;

        exec.scheduleAtFixedRate(() -> {
            try {
                var invoices = invoiceService.findAll();
                long overdueCount = invoices.stream()
                        .filter(inv -> !inv.isPaid() && inv.invoice().getDueDate().isBefore(LocalDate.now()))
                        .count();

                Platform.runLater(() -> {
                    if (overdueCount > 0) {
                        banner.setText("Trenutno ima " + overdueCount + " neplaćenih faktura nakon dospijeća!");
                        banner.setVisible(true);
                        banner.setManaged(true);
                    } else {
                        banner.setVisible(false);
                        banner.setManaged(false);
                    }
                });
            } catch (DatabaseException e) {
                logger.error("Neuspješno dohvaćanje faktura za notificiranje financijskog tima", e);
            }
        }, 0, periodSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        exec.shutdownNow();
    }
}