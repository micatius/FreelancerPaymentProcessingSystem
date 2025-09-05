package hr.java.production.thread;

import hr.java.production.log.BinaryChangeLogger;
import hr.java.production.log.ChangeLog;
import hr.java.production.model.Entity;
import javafx.application.Platform;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class ChangeLogRefresher implements AutoCloseable {
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ChangeLogRefresher");
        t.setDaemon(true);
        return t;
    });

    private final BinaryChangeLogger logger;
    private final long periodSeconds;
    private final Consumer<List<ChangeLog<Entity>>> onLogs;

    public ChangeLogRefresher(BinaryChangeLogger logger,
                              long periodSeconds,
                              Consumer<List<ChangeLog<Entity>>> onLogs) {
        this.logger = Objects.requireNonNull(logger);
        this.periodSeconds = periodSeconds <= 0 ? 5 : periodSeconds;
        this.onLogs = Objects.requireNonNull(onLogs);
    }

    public void start() {
        exec.scheduleAtFixedRate(() -> {
            List<ChangeLog<Entity>> logs = logger.readAll();
            Platform.runLater(() -> onLogs.accept(logs));
        }, 0, periodSeconds, TimeUnit.SECONDS);
    }

    @Override public void close() {
        exec.shutdownNow();
    }
}