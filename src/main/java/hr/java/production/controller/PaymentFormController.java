package hr.java.production.controller;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Invoice;
import hr.java.production.model.Payment;
import hr.java.production.service.InvoiceService;
import hr.java.production.service.PaymentService;
import hr.java.production.ui.Alerts;
import hr.java.production.ui.ScreenMode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class PaymentFormController {
    @FXML private ComboBox<Invoice> invoiceComboBox;
    @FXML private TextField txField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePaidPicker;
    @FXML private Button actionButton;

    private ScreenMode mode;
    private Payment modelPayment;

    private final PaymentService paymentService = new PaymentService();
    private final InvoiceService invoiceService = new InvoiceService();


    @FXML
    public void setup(ScreenMode mode, Payment modelPayment) {
        this.mode  = mode;
        this.modelPayment = modelPayment;
        configureInvoiceComboDisplay();
        loadInvoicesForMode();

        if (mode != ScreenMode.CREATE && modelPayment != null) populateFields(modelPayment);
        applyMode(mode);
    }

    private void configureInvoiceComboDisplay() {
        invoiceComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Invoice inv) {
                if (inv == null) return "";
                var f = inv.getFreelancer();
                String who = (f != null) ? (f.getFirstName() + " " + f.getLastName()).trim() : "N/A";
                return "Račun #" + inv.getId() + " – " + who + " – " + inv.getInvoiceDate();
            }
            @Override public Invoice fromString(String s) { return null; }
        });
        invoiceComboBox.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Invoice inv, boolean empty) {
                super.updateItem(inv, empty);
                if (empty || inv == null) {
                    setText(null);
                } else {
                    var f = inv.getFreelancer();
                    String who = (f != null) ? (f.getFirstName() + " " + f.getLastName()).trim() : "N/A";
                    setText("Račun #" + inv.getId() + " – " + who + " – " + inv.getInvoiceDate());
                }
            }
        });
    }

    /**
     * In CREATE: show only unpaid invoices.
     * In EDIT: show unpaid + ensure current invoice (even if already paid) is available & preselected.
     * In VIEW: just include the current invoice if present and lock the controls.
     */
    private void loadInvoicesForMode() {
        try {
            switch (mode) {
                case CREATE -> {
                    // All unpaid invoices
                    var views = invoiceService.findAll();
                    var unpaid = views.stream()
                            .filter(v -> !v.isPaid())
                            .map(InvoiceService.InvoiceView::invoice)
                            .toList();
                    invoiceComboBox.getItems().setAll(unpaid);
                }
                case EDIT, VIEW -> {
                    // neplaćene fakture i fakture koja je trenutno vezana uz uplatu
                    var views = invoiceService.findAll();
                    var unpaid = views.stream().filter(v -> !v.isPaid()).map(InvoiceService.InvoiceView::invoice).toList();

                    if (modelPayment != null && modelPayment.getInvoice() != null) {
                        Invoice current = modelPayment.getInvoice();
                        Invoice fullCurrent = views.stream()
                                .map(InvoiceService.InvoiceView::invoice)
                                .filter(inv -> inv.getId().equals(current.getId()))
                                .findFirst()
                                .orElse(current);
                        if (unpaid.stream().noneMatch(inv -> inv.getId().equals(fullCurrent.getId()))) {
                            // add to beginning so it's easy to spot
                            invoiceComboBox.getItems().addFirst(fullCurrent);
                        }
                        // plus the unpaid ones
                        invoiceComboBox.getItems().addAll(unpaid);
                    } else {
                        invoiceComboBox.getItems().setAll(unpaid);
                    }
                }
            }
        } catch (DatabaseException e) {
            Alerts.error("Greška pri dohvaćanju računa za odabir uplate.", e);
            invoiceComboBox.getItems().clear();
        }
    }

    private void populateFields(Payment p) {
        if (p.getInvoice() != null) {
            // Try to preselect exact item
            Optional<Invoice> match = invoiceComboBox.getItems().stream()
                    .filter(inv -> inv.getId().equals(p.getInvoice().getId()))
                    .findFirst();
            match.ifPresent(invoiceComboBox::setValue);
        }
        txField.setText(p.getTransactionId());
        if (p.getAmount() != null) amountField.setText(p.getAmount().toPlainString());
        if (p.getPaidOn() != null) datePaidPicker.setValue(p.getPaidOn().toLocalDate());
    }

    private void applyMode(ScreenMode m) {
        boolean editable = (m != ScreenMode.VIEW);

        // Set editability for all form fields
        setEditable(invoiceComboBox, editable);
        setEditable(txField, editable);
        datePaidPicker.setDisable(!editable);

        // Configure action button
        actionButton.setVisible(editable);
        actionButton.setManaged(editable);
        if (editable) {
            actionButton.setText(m == ScreenMode.CREATE ? "Evidentiraj uplatu" : "Spremi promjene");
        }
    }

    private static void setEditable(Control control, boolean editable) {
        // Simple disable for all controls
        control.setDisable(!editable);

        // Additional setting for text fields
        if (control instanceof TextField textField) {
            textField.setEditable(editable);
        }
    }

    // ---------- Actions ----------

    @FXML
    private void onActionButton() {
        if (!Alerts.confirm("Jeste li sigurni da želite spremiti promjene?")) return;

        try {
            if (mode == ScreenMode.CREATE) {
                Payment created = buildPayment(null);
                Long id = paymentService.save(created);
                created.setId(id);
                modelPayment = paymentService.findById(id).orElse(created); // hydrate back if you want
            } else { // EDIT
                Payment updated = buildPayment(modelPayment.getId());
                paymentService.update(updated);
                modelPayment = paymentService.findById(modelPayment.getId()).orElse(updated);
            }
            close();
            Alerts.info("Uspješno spremljeno.");
        } catch (DatabaseException ex) {
            Alerts.error("Greška baze podataka: " + ex.getMessage(), ex);
        } catch (RuntimeException ove) {
            // ObjectValidationException
            Alerts.error("Neispravni podaci: " + ove.getMessage(), ove);
        }
    }

    @FXML
    private void onInvoiceSelected() {
        Invoice inv = invoiceComboBox.getValue();
        if (inv != null) {
            amountField.setText(inv.getTotalCost().toPlainString());
        } else {
            amountField.clear();
        }
        amountField.setEditable(false);
        amountField.setDisable(true);
    }

    private Payment buildPayment(Long id) {
        // --- Invoice (required) ---
        Invoice inv = invoiceComboBox.getValue();
        if (inv == null || inv.getId() == null) {
            throw new IllegalArgumentException("Morate odabrati račun.");
        }

        // --- Amount (required, > 0) ---
        String amountStr = amountField.getText();
        if (amountStr == null || amountStr.isBlank()) {
            throw new IllegalArgumentException("Iznos uplate je obavezan.");
        }
        BigDecimal amount = new BigDecimal(amountStr.trim());

        // --- Date (required) ---
        LocalDate d = datePaidPicker.getValue();
        if (d == null) throw new IllegalArgumentException("Datum uplate je obavezan.");
        LocalDateTime paidOn = d.atStartOfDay();

        // --- Tx id (optional) ---
        String tx = (txField.getText() == null) ? "" : txField.getText().trim();

        Payment.Builder pb = new Payment.Builder()
                .invoice(Invoice.ref(inv.getId())) // only ID required for DAO
                .amount(amount)
                .paidOn(paidOn)
                .transactionId(tx);

        if (id != null) pb.id(id);
        return pb.build();
    }

    private void close() {
        ((Stage) actionButton.getScene().getWindow()).close();
    }

    // ---------- Public getters for openers ----------

    public Payment getModelPayment()  { return modelPayment; }
    public String getWindowTitle() {
        return switch (mode) {
            case VIEW  -> "Pregled uplate";
            case CREATE-> "Nova uplata";
            case EDIT  -> "Uređivanje uplate";
        };
    }
}
