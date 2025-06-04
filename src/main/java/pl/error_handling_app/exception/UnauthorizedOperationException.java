package pl.error_handling_app.exception;

public class UnauthorizedOperationException extends RuntimeException{

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
