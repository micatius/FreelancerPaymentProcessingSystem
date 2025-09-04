package hr.java.production.controller;

import hr.java.production.model.Role;
import hr.java.production.model.User;
import hr.java.production.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MenuController {

    @FXML
    private TabPane mainTabs;
    @FXML
    private Tab tabInvoices;
    @FXML
    private Tab tabPayments;
    @FXML
    private Tab tabFreelancers;
    @FXML
    private Tab tabChanges;

    public void initialize() {
        User u = SessionManager.getCurrentUser();
        if (u == null || u.role() == null) {
            return; // no session → show everything with defaults
        }

        if (u.role().equals(Role.FREELANCER)) {
            applyFreelancerView();
        }
    }

    private void applyFreelancerView() {
        tabInvoices.setText("Moje fakture");
        tabPayments.setText("Moje isplate");

        mainTabs.getTabs().remove(tabFreelancers);
        mainTabs.getTabs().remove(tabChanges);
    }
}
