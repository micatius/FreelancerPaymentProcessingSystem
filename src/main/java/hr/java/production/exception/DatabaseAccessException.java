package hr.java.production.exception;

public class DatabaseAccessException extends DatabaseException {
  public DatabaseAccessException(String message) {
    super(message);
  }

  public DatabaseAccessException(String message, Throwable cause) {
    super(message, cause);
  }

  public DatabaseAccessException(Throwable cause) {
    super(cause);
  }

  public DatabaseAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public DatabaseAccessException() {
  }
}
