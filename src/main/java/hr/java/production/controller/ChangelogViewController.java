package hr.java.production.controller;

import hr.java.production.log.BinaryChangeLogger;
import hr.java.production.log.ChangeLog;
import hr.java.production.model.Entity;
import hr.java.production.thread.ChangeLogRefresher;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ChangelogViewController upravlja prikazom tablice koja sadrži zapise promjena (ChangeLog)
 * nad entitetima. Tablica omogućuje pregled informacija o tipu entiteta, vrsti operacije,
 * korisničkom imenu, identifieru, te vremenskim zapisima promjene. Podaci se automatski sortiraju.
 */
public class ChangelogViewController {
    @FXML
    private TableView<ChangeLog<Entity>> changelogTable;
    @FXML
    private TableColumn<ChangeLog<Entity>, String> typeCol;
    @FXML
    private TableColumn<ChangeLog<Entity>, String> idCol;
    @FXML
    private TableColumn<ChangeLog<Entity>, String> operationCol;
    @FXML
    private TableColumn<ChangeLog<Entity>, String> dateCol;
    @FXML
    private TableColumn<ChangeLog<Entity>, String> timeCol;
    @FXML
    private TableColumn<ChangeLog<Entity>, String> userCol;
    private final ObservableList<ChangeLog<Entity>> rows = FXCollections.observableArrayList();
    private final BinaryChangeLogger changeLogger = new BinaryChangeLogger();
    private ChangeLogRefresher refresher;

    private static final Logger log = LoggerFactory.getLogger(ChangelogViewController.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private void initialize() {
        typeCol.setCellValueFactory(cd ->
                new SimpleStringProperty(simpleType(cd.getValue())));
        idCol.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().entityId())));
        operationCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().op().name()));
        dateCol.setCellValueFactory(cd ->
                new SimpleStringProperty(DATE_FMT.format(cd.getValue().timestamp().toLocalDate())));
        timeCol.setCellValueFactory(cd ->
                new SimpleStringProperty(TIME_FMT.format(cd.getValue().timestamp().toLocalTime())));
        userCol.setCellValueFactory(cd ->
                new SimpleStringProperty(nullSafe(cd.getValue().username())));

        // numerički sort za id kolonu
        idCol.setComparator((a, b) -> {
            try { return Integer.compare(Integer.parseInt(a), Integer.parseInt(b)); }
            catch (NumberFormatException _) { return a.compareTo(b); }
        });

        SortedList<ChangeLog<Entity>> sorted = new SortedList<>(rows);
        sorted.comparatorProperty().bind(changelogTable.comparatorProperty());
        changelogTable.setItems(sorted);

        // create and start refresher
        refresher = new ChangeLogRefresher(new BinaryChangeLogger(),
                3, rows::setAll);
        refresher.start();

        // ensure refresher shuts down when window closes
        Platform.runLater(() -> {
            Window w = changelogTable.getScene().getWindow();
            w.setOnHiding(e -> {
                if (refresher != null) refresher.close();
            });
        });

        // Pokušaj učitavanja odmah; ako ne uspije, ostavi prazno (može se naknadno pozvati setData).
        try {
            List<ChangeLog<Entity>> all = changeLogger.readAll();
            rows.setAll(all);
        } catch (Exception e) {
            log.error("Greška u dohvaćanju changelogova iz binarne datoteke", e);
        }
    }

    /**
     * Postavlja podatke za prikaz.
     * @param logs
     */
    public void setData(List<ChangeLog<Entity>> logs) {
        rows.setAll(logs);
    }

    /**
     * Vraća jednostavni naziv tipa povezanog s instancom ChangeLog ili "Unknown" ako tip nije definiran.
     *
     * @param cl instanca ChangeLog čiji se tip koristi za dobivanje jednostavnog naziva
     * @return jednostavni naziv tipa entiteta ili "Unknown" ako tip nije postavljen
     */
    private static String simpleType(ChangeLog<?> cl) {
        Class<?> t = cl.type();
        return (t != null) ? t.getSimpleName() : "Unknown";
    }

    /**
     * Vraća prazan string ako je predani string null, inače vraća predani string.
     *
     * @param s string koji treba provjeriti na null
     * @return prazan string ako je s null, inače predani string
     */
    private static String nullSafe(String s) {
        return (s == null) ? "" : s;
    }
}

