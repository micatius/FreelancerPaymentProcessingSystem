package hr.java.production.controller;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Address;
import hr.java.production.model.Freelancer;
import hr.java.production.service.FreelancerService;
import hr.java.production.ui.Alerts;
import hr.java.production.ui.ScreenMode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public final class FreelancerFormController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNoField;
    @FXML private TextField companyNameField;
    @FXML private TextField companyOibField;
    @FXML private TextField bankAccountNoField;
    @FXML private TextField streetField;
    @FXML private TextField houseNoField;
    @FXML private TextField townField;
    @FXML private TextField postalCodeField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button actionButton;

    private final FreelancerService freelancerService = new FreelancerService();

    private ScreenMode mode = ScreenMode.CREATE;
    private Freelancer modelFreelancer;

    @FXML
    private void initialize() {
        activeCheckBox.setSelected(true);
    }

    public void setup(ScreenMode mode, Freelancer model) {
        this.mode  = mode;
        this.modelFreelancer = model;

        if (mode != ScreenMode.CREATE && model != null) {
            populateFields(model);
        }
        applyMode(mode);
    }

    private void populateFields(Freelancer f) {
        firstNameField.setText(f.getFirstName());
        lastNameField.setText(f.getLastName());
        emailField.setText(f.getEmail());
        phoneNoField.setText(f.getPhoneNumber());
        companyNameField.setText(f.getBusinessName());
        companyOibField.setText(f.getBusinessIdentificationNumber());
        bankAccountNoField.setText(f.getBankAccountNumber());
        activeCheckBox.setSelected(f.getActive());

        Address a = f.getAddress();
        if (a != null) {
            streetField.setText(a.getStreet());
            houseNoField.setText(a.getHouseNumber());
            townField.setText(a.getCity());
            postalCodeField.setText(a.getPostalCode());
        } else {
            streetField.clear();
            houseNoField.clear();
            townField.clear();
            postalCodeField.clear();
        }
    }

    private void applyMode(ScreenMode m) {
        boolean viewOnly = (m == ScreenMode.VIEW);

        setEditable(firstNameField, !viewOnly);
        setEditable(lastNameField, !viewOnly);
        setEditable(emailField, !viewOnly);
        setEditable(phoneNoField, !viewOnly);
        setEditable(companyNameField, !viewOnly);
        setEditable(companyOibField, !viewOnly);
        setEditable(bankAccountNoField, !viewOnly);
        setEditable(streetField, !viewOnly);
        setEditable(houseNoField, !viewOnly);
        setEditable(townField, !viewOnly);
        setEditable(postalCodeField, !viewOnly);
        activeCheckBox.setDisable(viewOnly);

        if (viewOnly) {
            actionButton.setVisible(false);
            actionButton.setManaged(false);
        } else {
            actionButton.setVisible(true);
            actionButton.setManaged(true);
            actionButton.setText(m == ScreenMode.CREATE ? "Kreiraj" : "Spremi promjene");
        }
    }

    private static void setEditable(TextField tf, boolean editable) {
        tf.setEditable(editable);
        tf.setDisable(!editable);
    }

    @FXML
    private void onActionButton() {
        if (!Alerts.confirm("Jeste li sigurni da želite spremiti promjene?")) return;

        try {
            if (mode == ScreenMode.CREATE) {
                Freelancer created = buildFreelancer(null);
                Long id = freelancerService.save(created);
                created.setId(id);
                modelFreelancer = created;
                Alerts.info("Suradnik je uspješno kreiran.");
            } else { // EDIT
                Freelancer updated = buildFreelancer(modelFreelancer.getId());
                freelancerService.update(updated);
                modelFreelancer = updated;
                Alerts.info("Promjene su uspješno spremljene.");
            }
            close();
        } catch (DatabaseException ex) {
            Alerts.error("Greška baze podataka: " + ex.getMessage(), ex);
        } catch (RuntimeException ve) {
            Alerts.error("Neispravni podaci: " + ve.getMessage(), ve);
        }
    }

    private Freelancer buildFreelancer(Long id) {
        Address.Builder addressBuilder = new Address.Builder()
                .street(streetField.getText())
                .houseNumber(houseNoField.getText())
                .city(townField.getText())
                .postalCode(postalCodeField.getText());

        if (id != null && modelFreelancer != null && modelFreelancer.getAddress() != null) {
            addressBuilder.id(modelFreelancer.getAddress().getId());
        }

        Address address = addressBuilder.build();


        Freelancer.Builder fb = new Freelancer.Builder()
                .firstName(firstNameField.getText())
                .lastName(lastNameField.getText())
                .email(emailField.getText())
                .phoneNumber(phoneNoField.getText())
                .address(address)
                .businessName(companyNameField.getText())
                .businessIdentificationNumber(companyOibField.getText())
                .bankAccountNumber(bankAccountNoField.getText())
                .active(activeCheckBox.isSelected());

        if (id != null) fb.id(id);
        return fb.build();
    }

    private void close() {
        ((Stage) actionButton.getScene().getWindow()).close();
    }

    public Freelancer getModelFreelancer(){ return modelFreelancer; }
    public String getWindowTitle() {
        return switch (mode) {
            case VIEW   -> "Pregled suradnika";
            case CREATE -> "Novi suradnik";
            case EDIT   -> "Uređivanje suradnika";
        };
    }
}
