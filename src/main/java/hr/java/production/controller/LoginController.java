package hr.java.production.controller;

import hr.java.production.exception.AuthenticationException;
import hr.java.production.model.User;
import hr.java.production.util.AuthenticationService;
import hr.java.production.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

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

            // 2) Save into your session singleton
            SessionManager.setCurrentUser(user);

            Alert success = new Alert(Alert.AlertType.INFORMATION,
                    "Prijava uspješna!", ButtonType.OK);
            success.setHeaderText(null);
            success.setTitle("Uspjeh");
            success.showAndWait();

            // TODO: switch to your main application screen here

        } catch (AuthenticationException ex) {
            // Show the error message from your service
            Alert error = new Alert(Alert.AlertType.ERROR,
                    ex.getMessage(), ButtonType.OK);
            error.setHeaderText("Prijava neuspješna");
            error.setTitle("Greška");
            error.showAndWait();
        }
    }
}