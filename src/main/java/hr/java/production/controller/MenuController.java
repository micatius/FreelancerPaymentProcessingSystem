package hr.java.production.controller;

import hr.java.production.model.Role;
import hr.java.production.model.User;
import hr.java.production.thread.FinanceOverdueRefresher;
import hr.java.production.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    @FXML private Label overdueBanner;

    private FinanceOverdueRefresher overdueRefresher;

    public void initialize() {
        User u = SessionManager.getCurrentUser();
        if (u == null || u.role() == null) {
            return;
        }

        if (u.role().equals(Role.FREELANCER)) {
            applyFreelancerView();
        }

        overdueRefresher = new FinanceOverdueRefresher(overdueBanner, 10);
        overdueRefresher.start();

        javafx.application.Platform.runLater(() -> {
            var window = overdueBanner.getScene().getWindow();
            window.setOnHiding(e -> overdueRefresher.close());
        });
    }

    private void applyFreelancerView() {
        tabInvoices.setText("Moje fakture");
        tabPayments.setText("Moje isplate");

        mainTabs.getTabs().remove(tabFreelancers);
        mainTabs.getTabs().remove(tabChanges);
    }
}
