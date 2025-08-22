package hr.java.production.exception;

public class BinaryFileException extends RuntimeException {
    public BinaryFileException(String message) {
        super(message);
    }

    public BinaryFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public BinaryFileException(Throwable cause) {
        super(cause);
    }

    public BinaryFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BinaryFileException() {
    }
}
