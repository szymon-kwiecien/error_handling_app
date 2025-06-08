package pl.error_handling_app.exception;

public class CategoryAlreadyExistsException extends RuntimeException{

    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}
