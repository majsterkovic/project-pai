package put.poznan.txtdocsbackend.service.exceptions;

public class UnauthorizedException extends Exception {
    public UnauthorizedException(String invalidPassword) {
        super(invalidPassword);
    }
}
