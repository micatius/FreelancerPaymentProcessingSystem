package hr.java.production.ui;

import hr.java.production.controller.FreelancerFormController;
import hr.java.production.controller.InvoiceFormController;
import hr.java.production.controller.PaymentFormController;
import hr.java.production.main.PaymentApplication;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;
import hr.java.production.model.Payment;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Klasa Windows omogućuje otvaranje i upravljanje različitim uređivačkim prozorima
 * za entitete poput suradnika, faktura i uplata.
 * Koristi JavaFX za prikaz i interakciju s korisničkim sučeljem.
 */
public final class Windows {
    private Windows() {}

    public static void openFreelancerForm(Stage owner,
                                          ScreenMode mode,
                                          Freelancer freelancer) {
        try {
            FXMLLoader loader = new FXMLLoader(PaymentApplication.class.getResource("freelancer-form.fxml"));
            Parent root = loader.load();
            FreelancerFormController c = loader.getController();
            c.setup(mode, freelancer);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(c.getWindowTitle());
            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            Alerts.error("Greška u otvaranju forme za suradnika.", e);
        }
    }

    public static void openInvoiceForm(Stage owner,
                                       ScreenMode mode,
                                       Invoice invoice) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    PaymentApplication.class.getResource("invoice-form.fxml"));
            Parent root = loader.load();
            InvoiceFormController c = loader.getController();
            c.setup(mode, invoice);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(c.getWindowTitle());
            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            Alerts.error("Greška u otvaranju forme za fakture.", e);
        }
    }

    public static void openPaymentForm(Stage owner,
                                       ScreenMode mode,
                                       Payment model) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    PaymentApplication.class.getResource("payment-form.fxml"));
            Parent root = loader.load();
            PaymentFormController c = loader.getController();
            c.setup(mode, model);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(c.getWindowTitle());
            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            Alerts.error("Greška u otvaranju forme za isplate.", e);
        }
    }
}
