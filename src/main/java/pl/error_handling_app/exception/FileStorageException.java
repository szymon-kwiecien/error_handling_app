package pl.error_handling_app.exception;

public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }
}
