package hr.java.production.controller;

import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Freelancer;
import hr.java.production.service.FreelancerService;
import hr.java.production.ui.Alerts;
import hr.java.production.ui.ScreenMode;
import hr.java.production.ui.Windows;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.Locale;

public class FreelancerViewController {

    @FXML
    private TableView<Freelancer> freelancerTable;

    @FXML
    private TableColumn<Freelancer, String> idCol;
    @FXML
    private TableColumn<Freelancer, String> fullNameCol;
    @FXML
    private TableColumn<Freelancer, String> businessNameCol;
    @FXML
    private TableColumn<Freelancer, String> emailCol;
    @FXML
    private TableColumn<Freelancer, String> phoneNoCol;
    @FXML
    private TableColumn<Freelancer, String> activeCol;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private TextField filterField;

    private final FreelancerService freelancerService = new FreelancerService();
    private final ObservableList<Freelancer> currentList = FXCollections.observableArrayList();
    private FilteredList<Freelancer> filtered;

    private enum ActiveFilter { ANY, ACTIVE, INACTIVE }

    @FXML
    private void initialize() {
        // columns
        idCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        fullNameCol.setCellValueFactory(data -> {
            Freelancer f = data.getValue();
            String full = (f.getFirstName() + " " + f.getLastName()).trim();
            return new SimpleStringProperty(full);
        });

        businessNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBusinessName()));

        emailCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEmail()));

        phoneNoCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPhoneNumber()));

        activeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getActive() ? "Da" : "Ne"));

        idCol.setComparator((s1, s2) -> {
            try { return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)); }
            catch (NumberFormatException _) { return s1.compareTo(s2); }
        });

        try {
            List<Freelancer> freelancers = freelancerService.findAll();
            currentList.setAll(freelancers);
        } catch (DatabaseException e) {
            currentList.clear();
            Alerts.error("Dogodila se greška u dohvaćanju suradnika", e);
        }

        filtered = new FilteredList<>(currentList, f -> true);
        SortedList<Freelancer> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(freelancerTable.comparatorProperty());
        freelancerTable.setItems(sorted);


        applyFilters();
    }

    @FXML
    private void applyFilters() {
        String q = normalize(filterField.getText());
        ActiveFilter af = getActiveFilter();

        filtered.setPredicate(f -> {
            if (af == ActiveFilter.ACTIVE   && !f.getActive()) return false;
            if (af == ActiveFilter.INACTIVE &&  f.getActive()) return false;

            if (q.isEmpty()) return true;
            return matchesText(f, q);
        });
    }

    private ActiveFilter getActiveFilter() {
        if (activeCheckBox.isIndeterminate()) return ActiveFilter.ANY;
        return activeCheckBox.isSelected() ? ActiveFilter.ACTIVE : ActiveFilter.INACTIVE;
    }

    private boolean matchesText(Freelancer f, String q) {
        if (String.valueOf(f.getId()).contains(q)) return true;

        String full = (f.getFirstName() + " " + f.getLastName()).toLowerCase(Locale.ROOT);
        if (full.contains(q)) return true;

        String biz = f.getBusinessName().toLowerCase(Locale.ROOT);
        if (biz.contains(q)) return true;

        String email = f.getEmail().toLowerCase(Locale.ROOT);
        if (email.contains(q)) return true;

        String phone = f.getPhoneNumber().toLowerCase(Locale.ROOT);
        return phone.contains(q);
    }

    private void reloadFreelancers() {
        try {
            currentList.setAll(freelancerService.findAll());
            applyFilters(); // keep current filters active
        } catch (DatabaseException e) {
            currentList.clear();
            Alerts.error("Dogodila se greška u dohvaćanju suradnika", e);
        }
    }

    private Stage ownerStage() {
        return (Stage) freelancerTable.getScene().getWindow();
    }

    private static String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    @FXML
    private void onAddFreelancer() {
        Windows.openFreelancerForm(ownerStage(), ScreenMode.CREATE, null);
        reloadFreelancers();
    }

    @FXML
    private void onEditFreelancer() {
        Freelancer selected = freelancerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Odaberite suradnika za uređivanje.");
            return;
        }
        Windows.openFreelancerForm(ownerStage(), ScreenMode.EDIT, selected);
        reloadFreelancers();
    }

    @FXML
    private void onViewFreelancer() {
        Freelancer selected = freelancerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Odaberite suradnika za pregled.");
            return;
        }
        Windows.openFreelancerForm(ownerStage(), ScreenMode.VIEW, selected);
    }

    @FXML
    private void onDeleteFreelancer() {
        var selected = freelancerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Odaberite suradnika za brisanje.");
            return;
        }
        boolean ok = Alerts.confirm("Jeste li sigurni da želite obrisati odabranog suradnika?");
        if (!ok) return;

        try {
            freelancerService.delete(selected.getId());
            currentList.remove(selected);
        } catch (Exception e) {
            Alerts.error("Brisanje nije uspjelo. Suradnik možda ima povezane zapise.", e);
        }
    }
}
