package hr.java.production.controller;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;
import hr.java.production.model.Role;
import hr.java.production.model.User;
import hr.java.production.service.InvoiceService;
import hr.java.production.service.InvoiceService.InvoiceView;
import hr.java.production.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class InvoiceViewController {

    @FXML private TableView<InvoiceView> invoiceTable;

    @FXML private TableColumn<InvoiceView, String> invoiceIdCol;
    @FXML private TableColumn<InvoiceView, String> freelancerCol;
    @FXML private TableColumn<InvoiceView, String> businessCol;
    @FXML private TableColumn<InvoiceView, String> dateReceivedCol;
    @FXML private TableColumn<InvoiceView, String> dueDateCol;
    @FXML private TableColumn<InvoiceView, String> paidCol;

    @FXML private DatePicker datePickerFrom;
    @FXML private DatePicker datePickerTo;

    @FXML private CheckBox paidCheckBox;
    @FXML private TextField filterField;

    private final InvoiceService invoiceService = new InvoiceService();
    private final ObservableList<InvoiceView> currentList = FXCollections.observableArrayList();
    private FilteredList<InvoiceView> filtered;
    private Long userFreelancerId = null;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    private enum PaidFilter { ANY, PAID, UNPAID }

    @FXML
    private void initialize() {
        // --- columns ---
        invoiceIdCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().invoice().getId())));

        freelancerCol.setCellValueFactory(data -> {
            Freelancer f = data.getValue().invoice().getFreelancer();
            String full = (f.getFirstName() + " " + f.getLastName()).trim();
            return new SimpleStringProperty(full);
        });

        businessCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().invoice().getFreelancer().getBusinessName()));

        dateReceivedCol.setCellValueFactory(data ->
                new SimpleStringProperty(DATE_FORMAT.format(data.getValue().invoice().getInvoiceDate())));

        dueDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(DATE_FORMAT.format(data.getValue().invoice().getDueDate())));

        paidCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isPaid() ? "Da" : "Ne"));

        invoiceIdCol.setComparator((s1, s2) -> {
            try { return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)); }
            catch (NumberFormatException e) { return s1.compareTo(s2); }
        });

        // --- load data ---
        try {
            List<InvoiceView> views = invoiceService.findAll();
            currentList.setAll(views);
        } catch (DatabaseException e) {
            currentList.clear();
        }

        // --- filtering & sorting ---
        filtered = new FilteredList<>(currentList, iv -> true);
        SortedList<InvoiceView> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(invoiceTable.comparatorProperty());
        invoiceTable.setItems(sorted);

        User u = SessionManager.getCurrentUser();
        if (u.role() == Role.FREELANCER) {
            userFreelancerId = u.linkedEntityId();
        }

        // initial filter pass
        applyFilters();
    }

    @FXML
    private void applyFilters() {
        String q = normalize(filterField.getText());
        PaidFilter pf = getPaidFilter();
        LocalDate dateFrom = datePickerFrom.getValue();
        LocalDate dateTo   = datePickerTo.getValue();

        filtered.setPredicate(iv -> {
            if (userFreelancerId != null && !iv.invoice().getFreelancerId().equals(userFreelancerId)) {
                return false;
            }
            // paid filter
            boolean isPaid = iv.isPaid();
            if ((pf == PaidFilter.PAID && !isPaid) || (pf == PaidFilter.UNPAID && isPaid))
                return false;

            // Date range check (with null safety)
            LocalDate invDate = iv.invoice().getInvoiceDate();
            if (invDate != null && ((dateFrom != null && invDate.isBefore(dateFrom)) ||
                    (dateTo != null && invDate.isAfter(dateTo))))
                return false;


            // text filter
            if (q.isEmpty()) return true;
            return matchesText(iv, q);
        });
    }

    private PaidFilter getPaidFilter() {
        if (paidCheckBox.isIndeterminate()) return PaidFilter.ANY;
        return paidCheckBox.isSelected() ? PaidFilter.PAID : PaidFilter.UNPAID;
    }

    private boolean matchesText(InvoiceView iv, String q) {
        Invoice inv = iv.invoice();

        if (String.valueOf(inv.getId()).contains(q)) return true;

        Freelancer f = inv.getFreelancer();
        if (f != null) {
            String full = (f.getFirstName() + " " + f.getLastName())
                    .toLowerCase(Locale.ROOT);
            if (full.contains(q)) return true;

            String company = f.getBusinessName().toLowerCase(Locale.ROOT);
            if (company.contains(q)) return true;
        }

        if (inv.getInvoiceDate() != null &&
                DATE_FORMAT.format(inv.getInvoiceDate()).contains(q)) return true;

        return inv.getDueDate() != null &&
                DATE_FORMAT.format(inv.getDueDate()).contains(q);
    }

    private static String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
