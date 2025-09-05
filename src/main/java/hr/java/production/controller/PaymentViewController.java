package hr.java.production.controller;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Payment;
import hr.java.production.model.Role;
import hr.java.production.model.User;
import hr.java.production.service.PaymentService;
import hr.java.production.ui.Alerts;
import hr.java.production.ui.ScreenMode;
import hr.java.production.ui.UiUtils;
import hr.java.production.ui.Windows;
import hr.java.production.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PaymentViewController {

    @FXML private TableView<Payment> paymentTable;

    @FXML private TableColumn<Payment, String> idCol;
    @FXML private TableColumn<Payment, String> receiverCol;
    @FXML private TableColumn<Payment, String> invoiceCol;
    @FXML private TableColumn<Payment, String> datePaidCol;
    @FXML private TableColumn<Payment, String> amountCol;
    @FXML private TableColumn<Payment, String> transactionIdCol;

    @FXML private DatePicker paidFrom;
    @FXML private DatePicker paidTo;

    @FXML private TextField filterField;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button viewButton;
    @FXML private Button deleteButton;

    private final PaymentService paymentService = new PaymentService();
    private final ObservableList<Payment> currentList = FXCollections.observableArrayList();
    private FilteredList<Payment> filtered;
    private Long userFreelancerId = null;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    @FXML
    private void initialize() {
        idCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        receiverCol.setCellValueFactory(data -> {
            Freelancer f = data.getValue().getInvoice().getFreelancer();
            String full = (f.getFirstName() + " " + f.getLastName()).trim();
            return new SimpleStringProperty(full);
        });

        invoiceCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getInvoice().getId())));

        datePaidCol.setCellValueFactory(data ->
                new SimpleStringProperty(DATE_FORMAT.format(data.getValue().getPaidOn().toLocalDate())));

        amountCol.setCellValueFactory(data -> {
            BigDecimal amt = data.getValue().getAmount();
            return new SimpleStringProperty(amt.toPlainString());
        });

        transactionIdCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTransactionId()));

        // sortiranje id kolone numerički
        idCol.setComparator((s1, s2) -> {
            try { return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)); }
            catch (NumberFormatException _) { return s1.compareTo(s2); }
        });
        invoiceCol.setComparator(idCol.getComparator());

        datePaidCol.setComparator(UiUtils.dateStringComparator(DATE_FORMAT));



        try {
            List<Payment> payments = paymentService.findAll();
            currentList.setAll(payments);
        } catch (DatabaseException e) {
            Alerts.error("Greška u učitavanju podataka o uplatama.", e);
            currentList.clear();
        }

        // filtriranje i sortiranje
        filtered = new FilteredList<>(currentList, p -> true);
        SortedList<Payment> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(paymentTable.comparatorProperty());
        paymentTable.setItems(sorted);


        User u = SessionManager.getCurrentUser();
        if (u.role() == Role.FREELANCER) {
            userFreelancerId = u.linkedEntityId();
            addButton.setDisable(true);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }

        applyFilters();
    }

    @FXML
    void applyFilters() {
        String q = normalize(filterField.getText());
        LocalDate from = paidFrom.getValue();
        LocalDate to   = paidTo.getValue();

        filtered.setPredicate(p -> {
            // ako je rola freelancer, pokaži mu samo njegove uplate
            if (userFreelancerId != null && !p.getInvoice().getFreelancer().getId().equals(userFreelancerId))
                return false;
            // filter po datumu
            LocalDate paidDate = p.getPaidOn().toLocalDate();
            if (from != null && paidDate.isBefore(from)) return false;
            if (to   != null && paidDate.isAfter(to))   return false;

            // filtrira tekst iz search boxa
            if (q.isEmpty()) return true;
            return matchesText(p, q);
        });
    }

    private boolean matchesText(Payment p, String q) {
        // payment id
        if (String.valueOf(p.getId()).contains(q)) return true;

        // invoice id
        if (String.valueOf(p.getInvoice().getId()).contains(q)) return true;

        // filter prema punom imena suradnika i firme
        Freelancer f = p.getInvoice().getFreelancer();
        String full = (f.getFirstName() + " " + f.getLastName()).toLowerCase(Locale.ROOT);
        if (full.contains(q)) return true;

        String company = f.getBusinessName().toLowerCase(Locale.ROOT);
        if (company.contains(q)) return true;

        // transaction id
        if (p.getTransactionId().toLowerCase(Locale.ROOT).contains(q)) return true;

        // iznos
        return p.getAmount().toPlainString().contains(q);
    }

    private Stage getStage() {
        return (Stage) paymentTable.getScene().getWindow();
    }

    private void reloadPayments() {
        try {
            currentList.setAll(paymentService.findAll()); // fully hydrated
            applyFilters(); // keep current filters
        } catch (DatabaseException e) {
            currentList.clear();
            Alerts.error("Dogodila se greška u dohvaćanju uplata", e);
        }
    }

    @FXML
    private void onAddPayment() {
        Windows.openPaymentForm(getStage(), ScreenMode.CREATE, null);
        reloadPayments();
    }

    @FXML
    private void onEditPayment() {
        Payment selected = paymentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Odaberite uplatu za uređivanje.");
            return;
        }
        Windows.openPaymentForm(getStage(), ScreenMode.EDIT, selected);
        reloadPayments();
    }

    @FXML
    private void onViewPayment() {
        Payment selected = paymentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Odaberite uplatu za pregled.");
            return;
        }
        Windows.openPaymentForm(getStage(), ScreenMode.VIEW, selected);
    }

    @FXML
    private void onDeletePayment() {
        Payment selected = paymentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Odaberite uplatu za brisanje.");
            return;
        }
        if (!Alerts.confirm("Jeste li sigurni da želite obrisati odabranu uplatu?")) return;

        try {
            paymentService.delete(selected.getId());
            currentList.remove(selected);
        } catch (Exception e) {
            Alerts.error("Brisanje nije uspjelo. Uplata možda ima povezane zapise.", e);
        }
    }

    private static String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
