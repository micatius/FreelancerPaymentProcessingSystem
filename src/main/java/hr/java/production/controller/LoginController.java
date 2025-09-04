package hr.java.production.controller;

import hr.java.production.exception.AuthenticationException;
import hr.java.production.main.PaymentApplication;
import hr.java.production.model.User;
import hr.java.production.util.AuthenticationService;
import hr.java.production.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController {


    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private final AuthenticationService authService = new AuthenticationService();

    /**
     * Autentificira korisnika na temelju unesenog korisničkog imena i lozinke.
     * Ako su podaci ispravni, postavlja korisnika u trenutnu sesiju, u protivnom prikazuje obavijest o grešci.
     */
    @FXML
    private void authenticate() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        try {
            User user = authService.login(username, password);

            SessionManager.setCurrentUser(user);

            Alert success = new Alert(Alert.AlertType.INFORMATION,
                    "Prijava uspješna!", ButtonType.OK);
            success.setHeaderText(null);
            success.setTitle("Uspjeh");
            success.showAndWait();

            FXMLLoader fxmlLoader = new FXMLLoader(PaymentApplication.class.getResource("main-view.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Fakture");
            stage.show();

        } catch (AuthenticationException ex) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    ex.getMessage(), ButtonType.OK);
            error.setHeaderText("Prijava neuspješna");
            error.setTitle("Greška");
            error.showAndWait();
        } catch (IOException ioEx) {
            log.error("Greška u učitavanju glavnog prozora", ioEx);
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "Ne mogu učitati glavni prozor.", ButtonType.OK);
            error.setHeaderText("Greška aplikacije");
            error.setTitle("Greška");
            error.showAndWait();
        }
    }
}