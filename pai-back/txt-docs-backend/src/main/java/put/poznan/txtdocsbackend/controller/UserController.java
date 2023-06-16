package put.poznan.txtdocsbackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import put.poznan.txtdocsbackend.dto.TokenDTO;
import put.poznan.txtdocsbackend.model.User;
import put.poznan.txtdocsbackend.service.exceptions.UserAlreadyExistsException;
import put.poznan.txtdocsbackend.service.exceptions.UserNotFoundException;
import put.poznan.txtdocsbackend.service.UserService;
import put.poznan.txtdocsbackend.service.exceptions.UnauthorizedException;

import java.security.NoSuchAlgorithmException;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/auth")

public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        try {
            userService.registerUser(user);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Utworzono konto użytkownika");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Taki użytkownik już istnieje");
        } catch (NoSuchAlgorithmException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Bład serwera");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        try {
            TokenDTO tokenDTO = userService.loginUser(user);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(tokenDTO);
        } catch (UserNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Nie znaleziono użytkownika");
        } catch (UnauthorizedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Niepoprawne hasło");
        } catch (NoSuchAlgorithmException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Bład serwera");
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> passwordForgot(@RequestBody User user) {
        try {
            userService.forgotPassword(user);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Email został wysłany");
        } catch (UserNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Użytkownik nie istnieje");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> passwordReset(@RequestBody User user) {
        try {
            userService.resetPassword(user);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Hasło zostało zmienione");
        } catch (UserNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Użytkownik nie istnieje");
        } catch (NoSuchAlgorithmException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Bład serwera");
        }
    }
}
