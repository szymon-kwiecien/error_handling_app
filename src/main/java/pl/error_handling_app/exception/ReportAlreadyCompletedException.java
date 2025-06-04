package pl.error_handling_app.exception;

public class ReportAlreadyCompletedException extends RuntimeException{

    public ReportAlreadyCompletedException(String message) {
        super(message);
    }
}
