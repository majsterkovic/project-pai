package put.poznan.txtdocsbackend.service;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import put.poznan.txtdocsbackend.model.User;
import put.poznan.txtdocsbackend.repository.UserRepository;
import put.poznan.txtdocsbackend.dto.TokenDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import put.poznan.txtdocsbackend.service.exceptions.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import put.poznan.txtdocsbackend.service.exceptions.UserAlreadyExistsException;
import put.poznan.txtdocsbackend.service.exceptions.UserNotFoundException;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public UserService(UserRepository userRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    public void registerUser(User user) throws UserAlreadyExistsException, NoSuchAlgorithmException {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Użytkownik już istnieje");
        }
        else {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(user.getPassword().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String hashedPassword = hexString.toString();
            user.setPassword(hashedPassword);
            userRepository.save(user);
        }
    }


    private final String base64EncodedSecretKey = "UEFJMjAyM01hcml1c3pIeWJpYWtQb2xpdGVjaG5pa2E=";
    byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
    Key key = Keys.hmacShaKeyFor(keyBytes);




    public TokenDTO loginUser(User user) throws UnauthorizedException, UserNotFoundException, NoSuchAlgorithmException {

        User dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow(
                () -> new UserNotFoundException("Użytkownik nie istnieje")
        );

        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 24 * 60 * 60 * 1000;
        Date exp = new Date(expMillis);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(user.getPassword().getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String hashedPassword = hexString.toString();

        if (dbUser.getPassword().equals(hashedPassword)) {
            String token = Jwts.builder()
                    .setSubject(String.valueOf(dbUser.getId()))
                    .setExpiration(exp)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            return new TokenDTO(token);
        } else {
            throw new UnauthorizedException("Niepoprawne hasło");
        }

    }


    public void forgotPassword(User user) throws UserNotFoundException {

        Optional<User> dbUser = userRepository.findByUsername(user.getUsername());
        String resetToken;


        if (dbUser.isPresent())
        {
            resetToken = UUID.randomUUID().toString();
            dbUser.get().setToken(resetToken);
            userRepository.save(dbUser.get());

            // Wysyłanie maila może nie działać, bo konto na poczcie może zostać zablokowane

            String url = "http://localhost:3000/reset-password?token=" + resetToken;

            log.info("Url: {}", url);

            try {
                sendResetPasswordEmail(dbUser.get().getEmail(), url);
            } catch (MessagingException e) {
                throw new RuntimeException("Błąd wysyłania maila");
            }
        }
    }

    private void sendResetPasswordEmail(String recipientEmail, String m) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(recipientEmail);
        helper.setFrom("majsterkovic@gmail.com");
        helper.setSubject("Reset hasła");
        helper.setText("Kliknij w link do resetu hasła: " + m, true);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new MessagingException("Błąd wysyłania maila");
        }
    }

    public void resetPassword(User user) throws UserNotFoundException, NoSuchAlgorithmException {

        Optional<User> dbUser = userRepository.findByToken(user.getToken());


        if (dbUser.isPresent()) {
            dbUser.get().setToken(null);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(user.getPassword().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String hashedPassword = hexString.toString();
            dbUser.get().setPassword(hashedPassword);

            userRepository.save(dbUser.get());
        } else {
            throw new UserNotFoundException("Użytkownik nie istnieje");
        }
    }
}
