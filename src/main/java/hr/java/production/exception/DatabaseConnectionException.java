package hr.java.production.exception;

public class DatabaseConnectionException extends DatabaseException {
    public DatabaseConnectionException(String message) {
        super(message);
    }

    public DatabaseConnectionException() {
    }

    public DatabaseConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DatabaseConnectionException(Throwable cause) {
        super(cause);
    }

    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
