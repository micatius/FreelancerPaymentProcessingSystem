package hr.java.production.exception;

public class BinaryFileWriteException extends BinaryFileException {
    public BinaryFileWriteException(String message) {
        super(message);
    }

    public BinaryFileWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public BinaryFileWriteException(Throwable cause) {
        super(cause);
    }

    public BinaryFileWriteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BinaryFileWriteException() {
    }
}
