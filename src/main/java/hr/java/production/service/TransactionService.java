package hr.java.production.service;

import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.exception.DatabaseException;
import hr.java.production.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
/**
 * Bazna klasa za servisni sloj koja sadrži pomoćne metode za transakcijsko izvršavanje posla
 * te korisne util metode za rukovanje iznimkama i transakcijama.
 */
public abstract class TransactionService {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Pomoćna metoda: izvrši posao u transakciji bez ugniježđenih try blokova.
     *
     * @param work         lambda koja prima Connection i vraća rezultat
     * @param errorMessage poruka u slučaju greške
     * @param <R>          tip rezultata
     * @return rezultat posla
     * @throws DatabaseException u slučaju greške pri radu s bazom
     */
    protected <R> R inTransaction(SQLFunction<R> work, String errorMessage) throws DatabaseException {
        Connection conn = null;
        boolean committed = false;
        boolean prevAuto = true;

        try {
            conn = DbUtil.connectToDatabase();

            prevAuto = getAutoCommitOrTrue(conn);
            setAutoCommitQuietly(conn, false);

            R result = work.apply(conn);

            conn.commit();
            committed = true;
            return result;

        } catch (DatabaseConnectionException | SQLException e) {
            throw new DatabaseException("Greška pri uspostavi veze prema bazi", e);
        } catch (RuntimeException e) {
            throw new DatabaseException(errorMessage, e);
        } finally {
            if (conn != null) {
                if (!committed) rollbackQuietly(conn);
                setAutoCommitQuietly(conn, prevAuto);
                closeQuietly(conn);
            }
        }
    }

    /** Funkcionalno sučelje: posao koji prima Connection i vraća rezultat R. */
    @FunctionalInterface
    protected interface SQLFunction<R> {
        R apply(Connection conn) throws SQLException, DatabaseException;
    }

    private boolean getAutoCommitOrTrue(Connection conn) {
        try {
            return conn.getAutoCommit();
        } catch (SQLException e) {
            log.debug("Failed to read autoCommit (defaulting to true)", e);
            return true;
        }
    }

    // pomoćne metode za manipulaciju Connection u transakciji (inTransaction metoda)

    private void setAutoCommitQuietly(Connection conn, boolean value) {
        try {
            conn.setAutoCommit(value);
        } catch (SQLException e) {
            log.debug("Failed to set autoCommit={} (ignored)", value, e);
        }
    }

    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.debug("Failed to rollback connection (ignored)", e);
        }
    }

    private void closeQuietly(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            log.debug("Failed to close connection (ignored)", e);
        }
    }
}
