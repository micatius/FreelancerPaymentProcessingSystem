package hr.java.production.controller;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;
import hr.java.production.model.Service;
import hr.java.production.service.FreelancerService;
import hr.java.production.service.InvoiceService;
import hr.java.production.ui.Alerts;
import hr.java.production.ui.ScreenMode;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class InvoiceFormController {

    // Header
    @FXML private DatePicker dateReceivedPicker;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<Freelancer> freelancerComboBox;

    // Services
    @FXML private TableView<Service> servicesTable;
    @FXML private TableColumn<Service, String> nameCol;
    @FXML private TableColumn<Service, String> quantityCol;
    @FXML private TableColumn<Service, String> unitFeeCol;

    // Actions
    @FXML private Button actionButton;
    @FXML private Button addRowButton;
    @FXML private Button deleteRowButton;

    @FXML private TextField serviceNameField;
    @FXML private TextField unitFeeField;
    @FXML private TextField quantityField;

    private final InvoiceService invoiceService = new InvoiceService();
    private final FreelancerService freelancerService = new FreelancerService();
    private final ObservableList<Service> rows = FXCollections.observableArrayList();

    private ScreenMode mode = ScreenMode.EDIT;
    private Invoice modelInvoice; // null za CREATE

    @FXML
    private void initialize() {
        configureFreelancerCombo();
        configureServiceTable();
        servicesTable.setItems(rows);
    }

    public void setup(ScreenMode mode, Invoice model) {
        this.mode = mode;
        this.modelInvoice = model;
        loadFreelancers();

        if (mode != ScreenMode.CREATE && model != null) {
            populateFields(model);
        }

        applyMode(mode);
    }

    private void configureFreelancerCombo() {
        freelancerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Freelancer f) {
                if (f == null) return "";
                String name = (f.getFirstName() + " " + f.getLastName()).trim();
                String biz  = f.getBusinessName();
                return (biz == null || biz.isBlank()) ? name : (name + " – " + biz);
            }
            @Override public Freelancer fromString(String s) { return null; }
        });
        freelancerComboBox.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Freelancer f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setText(null);
                } else {
                    String name = (f.getFirstName() + " " + f.getLastName()).trim();
                    String biz  = f.getBusinessName();
                    setText((biz == null || biz.isBlank()) ? name : (name + " – " + biz));
                }
            }
        });
    }

    private void configureServiceTable() {
        servicesTable.setEditable(false);

        nameCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getName()));
        quantityCol.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().getQuantity())));
        unitFeeCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getUnitFee().toPlainString()));
    }

    private void loadFreelancers() {
        try {
            List<Freelancer> all = freelancerService.findAll();
            freelancerComboBox.getItems().setAll(all);
        } catch (DatabaseException e) {
            freelancerComboBox.getItems().clear();
            Alerts.error("Greška pri dohvaćanju suradnika.", e);
        }
    }

    private void populateFields(Invoice inv) {
        dateReceivedPicker.setValue(inv.getInvoiceDate());
        dueDatePicker.setValue(inv.getDueDate());
        if (inv.getFreelancer() != null) {

            var targetId = inv.getFreelancer().getId();
            freelancerComboBox.getItems().stream()
                    .filter(f -> f.getId().equals(targetId))
                    .findFirst()
                    .ifPresent(freelancerComboBox::setValue);
        }
        rows.setAll(inv.getServices() == null ? List.of() : inv.getServices());
    }

    private void applyMode(ScreenMode m) {
        boolean viewOnly = (m == ScreenMode.VIEW);

        dateReceivedPicker.setDisable(viewOnly);
        dueDatePicker.setDisable(viewOnly);
        freelancerComboBox.setDisable(viewOnly);
        servicesTable.setDisable(viewOnly);

        serviceNameField.setDisable(viewOnly);
        unitFeeField.setDisable(viewOnly);
        quantityField.setDisable(viewOnly);
        addRowButton.setDisable(viewOnly);
        deleteRowButton.setDisable(viewOnly);

        if (viewOnly) {
            actionButton.setVisible(false);
            actionButton.setManaged(false);
        } else {
            actionButton.setVisible(true);
            actionButton.setManaged(true);
            actionButton.setText(m == ScreenMode.CREATE ? "Kreiraj" : "Spremi promjene");
        }
    }

    /* ------------------------------ actions ------------------------------ */

    @FXML
    private void onAddRow() {
        Service s = buildServiceFromFields();
        if (s == null) return; // validation already reported
        rows.add(s);
        servicesTable.getSelectionModel().select(s);
        servicesTable.scrollTo(s);
        clearServiceEntryFields();
    }

    private Service buildServiceFromFields() {
        String name = (serviceNameField.getText() == null) ? "" : serviceNameField.getText().trim();
        if (name.isEmpty()) {
            Alerts.info("Naziv usluge je obavezan.");
            return null;
        }

        int qty = Integer.parseInt(quantityField.getText().trim());
        if (qty <= 0) {
            Alerts.info("Količina mora biti pozitivan cijeli broj.");
            return null;
        }

        java.math.BigDecimal fee;
        try {
            fee = new java.math.BigDecimal(unitFeeField.getText().trim());
            if (fee.signum() < 0) throw new NumberFormatException();
        } catch (Exception ex) {
            Alerts.error("Jedinična cijena mora biti nenegativan broj (npr. 100.00).", ex);
            return null;
        }

        return new Service.Builder()
                .serviceName(name)
                .quantity(qty)
                .unitFee(fee)
                .build();
    }

    private void clearServiceEntryFields() {
        serviceNameField.clear();
        quantityField.clear();
        unitFeeField.clear();
    }


    @FXML
    private void onDeleteRow() {
        Service sel = servicesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alerts.info("Odaberite stavku za brisanje.");
            return;
        }
        rows.remove(sel);
    }

    @FXML
    private void onActionButton() {
        if (!Alerts.confirm("Jeste li sigurni da želite spremiti promjene?")) return;

        try {
            if (mode == ScreenMode.CREATE) {
                Invoice created = buildInvoice(null);
                Long id = invoiceService.save(created);
                created.setId(id);
                this.modelInvoice = created;
                Alerts.info("Faktura je uspješno kreirana.");
            } else { // EDIT
                Invoice updated = buildInvoice(modelInvoice.getId());
                invoiceService.update(updated);
                this.modelInvoice = updated;
                Alerts.info("Promjene su uspješno spremljene.");
            }
            close();
        } catch (DatabaseException ex) {
            Alerts.error("Greška baze podataka: " + ex.getMessage(), ex);
        } catch (RuntimeException ve) {
            Alerts.error("Neispravni podaci: " + ve.getMessage(), ve);
        }
    }

    /* ------------------------------ builders ------------------------------ */

    private Invoice buildInvoice(Long id) {
        LocalDate invDate = dateReceivedPicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        Freelancer selFreelancer = freelancerComboBox.getValue();

        if (invDate == null) throw new IllegalArgumentException("Datum zaprimanja je obavezan.");
        if (dueDate == null) throw new IllegalArgumentException("Datum dospijeća je obavezan.");
        if (selFreelancer == null) throw new IllegalArgumentException("Suradnik je obavezan.");

        // build shallow services list (invoiceId set in service layer on save)
        List<Service> services = new ArrayList<>(rows);
        if (services.isEmpty()) throw new IllegalArgumentException("Faktura mora imati barem jednu stavku.");

        Invoice.Builder b = new Invoice.Builder()
                .freelancer(selFreelancer)
                .invoiceDate(invDate)
                .dueDate(dueDate)
                .services(services);

        if (id != null) b.id(id);
        return b.build();
    }

    private void close() {
        ((Stage) actionButton.getScene().getWindow()).close();
    }

    public String getWindowTitle() {
        return switch (mode) {
            case VIEW   -> "Pregled fakture";
            case CREATE -> "Nova faktura";
            case EDIT   -> "Uređivanje fakture";
        };
    }
}
