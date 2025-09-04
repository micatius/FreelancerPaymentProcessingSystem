package hr.java.production.controller;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Payment;
import hr.java.production.model.Role;
import hr.java.production.model.User;
import hr.java.production.service.PaymentService;
import hr.java.production.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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

    private final PaymentService paymentService = new PaymentService();
    private final ObservableList<Payment> currentList = FXCollections.observableArrayList();
    private FilteredList<Payment> filtered;
    private Long userFreelancerId = null;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    @FXML
    private void initialize() {
        // columns
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

        // numeric sort for ids
        idCol.setComparator((s1, s2) -> {
            try { return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)); }
            catch (NumberFormatException e) { return s1.compareTo(s2); }
        });
        invoiceCol.setComparator(idCol.getComparator());

        // load data
        try {
            List<Payment> payments = paymentService.findAll(); // returns fully hydrated payments
            currentList.setAll(payments);
        } catch (DatabaseException e) {
            currentList.clear();
        }

        // filtering & sorting
        filtered = new FilteredList<>(currentList, p -> true);
        SortedList<Payment> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(paymentTable.comparatorProperty());
        paymentTable.setItems(sorted);

        // role restriction for freelancers
        User u = SessionManager.getCurrentUser();
        if (u.role() == Role.FREELANCER) {
            userFreelancerId = u.linkedEntityId();
        }

        // first pass
        applyFilters();
    }

    @FXML
    void applyFilters() {
        String q = normalize(filterField.getText());
        LocalDate from = paidFrom.getValue();
        LocalDate to   = paidTo.getValue();

        filtered.setPredicate(p -> {
            // freelancer-only view
            if (userFreelancerId != null && !p.getInvoice().getFreelancer().getId().equals(userFreelancerId))
                return false;
            // date range on paidOn (inclusive)
            LocalDate paidDate = p.getPaidOn().toLocalDate();
            if (from != null && paidDate.isBefore(from)) return false;
            if (to   != null && paidDate.isAfter(to))   return false;

            // text filter: payment id, invoice id, receiver full name, business, tx id, amount
            if (q.isEmpty()) return true;
            return matchesText(p, q);
        });
    }

    private boolean matchesText(Payment p, String q) {
        // payment id
        if (String.valueOf(p.getId()).contains(q)) return true;

        // invoice id
        if (String.valueOf(p.getInvoice().getId()).contains(q)) return true;

        // freelancer name + business (lowercase the whole strings)
        Freelancer f = p.getInvoice().getFreelancer();
        String full = (f.getFirstName() + " " + f.getLastName()).toLowerCase(Locale.ROOT);
        if (full.contains(q)) return true;

        String company = f.getBusinessName().toLowerCase(Locale.ROOT);
        if (company.contains(q)) return true;

        // transaction id
        if (p.getTransactionId().toLowerCase(Locale.ROOT).contains(q)) return true;

        // amount
        return p.getAmount().toPlainString().contains(q);
    }

    private static String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
