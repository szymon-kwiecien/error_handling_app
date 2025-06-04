package pl.error_handling_app.exception;

public class UserLacksRequiredRoleException extends RuntimeException{

    public UserLacksRequiredRoleException(String message) {
        super(message);
    }
}
