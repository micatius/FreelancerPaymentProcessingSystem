package hr.java.production.exception;

public class BuilderValidationException extends RuntimeException {

    public BuilderValidationException(String message) {
        super(message);
    }

    public BuilderValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderValidationException(Throwable cause) {
        super(cause);
    }

    public BuilderValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BuilderValidationException() {
    }
}
