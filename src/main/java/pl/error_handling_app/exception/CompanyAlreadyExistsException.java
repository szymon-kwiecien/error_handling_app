package pl.error_handling_app.exception;

public class CompanyAlreadyExistsException extends RuntimeException{

    public CompanyAlreadyExistsException(String message) {
        super(message);
    }
}
