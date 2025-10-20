package service;

public class InvalidAuthException extends RuntimeException {
    public InvalidAuthException(String message) {
        super(message);
    }

    public InvalidAuthException(String message, Throwable ex) {
        super(message, ex);
    }
}
