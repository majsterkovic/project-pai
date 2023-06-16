package put.poznan.txtdocsbackend.service.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String userNotFound) {
        super(userNotFound);
    }
}
