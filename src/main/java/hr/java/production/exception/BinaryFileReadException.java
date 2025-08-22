package hr.java.production.exception;

public class BinaryFileReadException extends BinaryFileException {
    public BinaryFileReadException(String message) {
        super(message);
    }

    public BinaryFileReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public BinaryFileReadException(Throwable cause) {
        super(cause);
    }

    public BinaryFileReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BinaryFileReadException() {
    }
}
