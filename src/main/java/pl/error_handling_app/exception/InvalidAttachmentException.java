package pl.error_handling_app.exception;

public class InvalidAttachmentException extends RuntimeException{

    public InvalidAttachmentException(String message) {
        super(message);
    }
}
