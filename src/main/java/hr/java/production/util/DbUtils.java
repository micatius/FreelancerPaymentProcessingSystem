package hr.java.production.util;

import hr.java.production.exception.DatabaseConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Klasa za povezivanje na bazu podataka.
 * Sadrži statičnu metodu za uspostavljanje veze s bazom koristeći konfiguracijske parametre.
 */
public final class DbUtils {
    private static final String DATABASE_FILE = "/db/db.properties";
    private DbUtils() {}

    /**
     * Spaja se na bazu podataka koristeći konfiguracijske parametre iz
     * datoteke "db/db.properties".
     *
     * @return objekt Connection koji predstavlja uspješno uspostavljenu vezu s bazom podataka
     * @throws DatabaseConnectionException ako dođe do problema prilikom spajanja na bazu podataka
     */
    public static Connection connectToDatabase() throws DatabaseConnectionException {
        Properties props = new Properties();

        try (InputStream in = DbUtils.class
                .getResourceAsStream(DATABASE_FILE)) {

            if (in == null) {
                throw new IllegalStateException("Nije pronađen db/db.properties");
            }
            props.load(in);

            String url = props.getProperty("dbUrl");
            String user = props.getProperty("username");
            String password = props.getProperty("password");

            return DriverManager.getConnection(url, user, password);

        } catch (IOException | IllegalStateException | SQLException e) {
            throw new DatabaseConnectionException(e.getMessage(), e);
        }
    }
}