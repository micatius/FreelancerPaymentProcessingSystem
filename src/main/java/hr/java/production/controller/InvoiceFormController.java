package hr.java.production.controller;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;
import hr.java.production.model.Service;
import hr.java.production.service.FreelancerService;
import hr.java.production.service.InvoiceService;
import hr.java.production.ui.ScreenMode;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public final class InvoiceFormController {

    // Header
    @FXML private DatePicker dateReceivedPicker;
    @FXML private DatePicker dateDuePicker;
    @FXML private ComboBox<Freelancer> freelancerComboBox;

    // Services
    @FXML private TableView<Service> serviceTable;
    @FXML private TableColumn<Service, String> nameCol;
    @FXML private TableColumn<Service, String> quantityCol;
    @FXML private TableColumn<Service, String> unitFeeCol;

    // Actions
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button addRowBtn;
    @FXML private Button deleteRowBtn;

    private final InvoiceService invoiceService = new InvoiceService();
    private final FreelancerService freelancerService = new FreelancerService();
    private final ObservableList<Service> rows = FXCollections.observableArrayList();

    private ScreenMode mode = ScreenMode.EDIT;
    private Invoice model;
    private boolean saved = false;

    @FXML
    private void initialize() {
        setupFreelancerCombo();
        setupServiceTable();
    }

    public void init(ScreenMode mode, Invoice invoice) {
        this.mode = mode;
        this.model = invoice;
        populateHeader(invoice);
        populateServices(invoice);
        applyMode(mode);
    }

    private void setupFreelancerCombo() {
        freelancerComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Freelancer f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : (f.getFirstName() + " " + f.getLastName()));
            }
        });
        freelancerComboBox.setButtonCell(freelancerComboBox.getCellFactory().call(null));
        try {
            freelancerComboBox.getItems().setAll(freelancerService.findAll());
        } catch (DatabaseException e) {
            freelancerComboBox.getItems().clear();
        }
    }

    private void setupServiceTable() {
        serviceTable.setItems(rows);
        serviceTable.setEditable(true);

        nameCol.setCellValueFactory(d -> new SimpleStringProperty(nullToEmpty(d.getValue().getName())));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            e.getRowValue().setName(e.getNewValue().trim());
            serviceTable.refresh();
        });

        quantityCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(nvl(d.getValue().getQuantity(), 1))));
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn());
        quantityCol.setOnEditCommit(e -> {
            e.getRowValue().setQuantity(parseInt(e.getNewValue(), 1));
            serviceTable.refresh();
        });

        unitFeeCol.setCellValueFactory(d -> new SimpleStringProperty(toText(d.getValue().getUnitFee())));
        unitFeeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        unitFeeCol.setOnEditCommit(e -> {
            e.getRowValue().setUnitFee(parseDecimal(e.getNewValue()));
            serviceTable.refresh();
        });

        serviceTable.setOnKeyPressed(k -> {
            if (k.getCode() == javafx.scene.input.KeyCode.DELETE) onDeleteSelectedRow();
        });
    }

    private void populateHeader(Invoice inv) {
        if (inv == null) {
            dateReceivedPicker.setValue(LocalDate.now());
            dateDuePicker.setValue(LocalDate.now().plusDays(14));
            return;
        }
        dateReceivedPicker.setValue(inv.getInvoiceDate());
        dateDuePicker.setValue(inv.getDueDate());
        if (inv.getFreelancer() != null) {
            freelancerComboBox.getItems().stream()
                    .filter(f -> f.getId().equals(inv.getFreelancer().getId()))
                    .findFirst().ifPresent(freelancerComboBox::setValue);
        }
    }

    private void populateServices(Invoice inv) {
        rows.clear();
        if (inv != null && inv.getServices() != null && !inv.getServices().isEmpty()) {
            for (Service s : inv.getServices()) rows.add(new Service.Builder(s).build());
        }
    }

    private void applyMode(ScreenMode mode) {
        boolean viewOnly = (mode == ScreenMode.VIEW);
        freelancerComboBox.setDisable(viewOnly);
        dateReceivedPicker.setDisable(viewOnly);
        dateDuePicker.setDisable(viewOnly);
        serviceTable.setEditable(!viewOnly);
        saveBtn.setVisible(!viewOnly); saveBtn.setManaged(!viewOnly);
        addRowBtn.setVisible(!viewOnly); addRowBtn.setManaged(!viewOnly);
        deleteRowBtn.setVisible(!viewOnly); deleteRowBtn.setManaged(!viewOnly);
    }

    @FXML
    private void onAddRow() {
        rows.add(new Service.Builder()
                .serviceName("")
                .unitFee(BigDecimal.ZERO)
                .quantity(1)
                .build());
        int ix = rows.size() - 1;
        serviceTable.getSelectionModel().select(ix);
        serviceTable.edit(ix, nameCol);
    }

    @FXML
    private void onDeleteSelectedRow() {
        var sel = new ArrayList<>(serviceTable.getSelectionModel().getSelectedItems());
        if (!sel.isEmpty()) rows.removeAll(sel);
    }

    @FXML
    private void onSave() {
        try {
            Freelancer f = freelancerComboBox.getValue();
            LocalDate invDate = dateReceivedPicker.getValue();
            LocalDate dueDate = dateDuePicker.getValue();

            if (model == null) {
                model = new Invoice.Builder()
                        .freelancer(f)
                        .invoiceDate(invDate)
                        .dueDate(dueDate)
                        .services(new ArrayList<>(rows))
                        .build();
                Long id = invoiceService.save(model);
                model.setId(id);
            } else {
                model.setFreelancer(f);
                model.setInvoiceDate(invDate);
                model.setDueDate(dueDate);
                model.setServices(new ArrayList<>(rows));
                invoiceService.update(model);
            }
            saved = true;
            close();

        } catch (DatabaseException e) {
            alert(Alert.AlertType.ERROR, "Greška pri spremanju računa:\n" + e.getMessage());
        } catch (RuntimeException ve) {
            alert(Alert.AlertType.WARNING, "Neispravni podaci:\n" + ve.getMessage());
        }
    }

    @FXML
    private void onCancel() { saved = false; close(); }

    private void close() { ((Stage) cancelBtn.getScene().getWindow()).close(); }

    public boolean isSaved() { return saved; }

    public Invoice getModel() { return model; }

    public String getWindowTitle() { return (mode == ScreenMode.VIEW) ? "Pregled računa" : "Uređivanje računa"; }

    // --- tiny helpers ---
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static int nvl(Integer v, int d) { return v == null ? d : v; }
    private static String toText(BigDecimal v) { return v == null ? "" : v.toPlainString(); }
    private static int parseInt(String s, int def) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; } }
    private static BigDecimal parseDecimal(String s) { try { return new BigDecimal(s.trim()); } catch (Exception e) { return BigDecimal.ZERO; } }
    private static void alert(Alert.AlertType t, String m) { new Alert(t, m, ButtonType.OK).showAndWait(); }
}
