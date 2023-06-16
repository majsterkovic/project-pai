package put.poznan.txtdocsbackend.service.exceptions;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String userAlreadyExists) {
        super(userAlreadyExists);
    }
}
