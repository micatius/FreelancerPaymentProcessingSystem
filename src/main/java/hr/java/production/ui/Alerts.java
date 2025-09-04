package hr.java.production.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Klasa Alerts pruža metode za prikaz različitih vrsta dijaloga korisniku, uključujući potvrdu, informacijske
 * poruke i poruke o greškama. Koristi se za interakciju s korisnikom, primjerice kod potvrđivanja akcija ili
 * informiranja o greškama u aplikaciji.
 */
public class Alerts {
    private Alerts() {}
    private static final Logger log = LoggerFactory.getLogger(Alerts.class);
    /**
     * Prikazuje dijalog za potvrdu sa zadanim porukom i omogućuje korisniku odabir između "Da" i "Ne".
     *
     * @param message tekst poruke koji će se prikazati u dijalogu za potvrdu
     * @return true ako korisnik odabere "Da", inače false
     */
    public static boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.setTitle("Potvrda");
        return a.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }

    /**
     * Prikazuje informacijski dijalog korisniku s unaprijed zadanim naslovom i zadanom porukom.
     *
     * @param message tekst poruke koji će se prikazati u dijalogu
     */
    public static void info(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Info");
        a.showAndWait();
    }

    /**
     * Prikazuje dijalog greške s navedenom porukom i opcionalno logira predanom iznimkom.
     *
     * @param message tekst poruke koji će se prikazati u dijalogu greške
     * @param t iznimka koja može biti logirana
     */
    public static void error(String message, Throwable t) {
        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Greška");
        a.showAndWait();

        if (t != null) {
            log.error("Greška prikazana korisniku: {}", message, t);
        } else {
            log.error("Greška prikazana korisniku: {}", message);
        }
    }
}
