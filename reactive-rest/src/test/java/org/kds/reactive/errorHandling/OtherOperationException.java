package org.kds.reactive.errorHandling;

public class OtherOperationException extends RuntimeException {

    public OtherOperationException() {
    }

    public OtherOperationException(String message) {
        super(message);
    }

    public OtherOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public OtherOperationException(Throwable cause) {
        super(cause);
    }

    public OtherOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
