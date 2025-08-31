package hr.java.production.exception;

public class ObjectValidationException extends RuntimeException {

    public ObjectValidationException(String message) {
        super(message);
    }

    public ObjectValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectValidationException(Throwable cause) {
        super(cause);
    }

    public ObjectValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ObjectValidationException() {
    }
}
