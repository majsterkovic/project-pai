package put.poznan.txtdocsbackend.service.exceptions;

public class TokenExpiredException extends Exception {
    public TokenExpiredException(String message) {
        super(message);
    }

}
