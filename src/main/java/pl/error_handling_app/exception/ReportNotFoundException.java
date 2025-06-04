package pl.error_handling_app.exception;

public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String message) {
        super(message);
    }
}
