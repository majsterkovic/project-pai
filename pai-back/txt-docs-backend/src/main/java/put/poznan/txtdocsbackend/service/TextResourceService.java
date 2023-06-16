package put.poznan.txtdocsbackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import put.poznan.txtdocsbackend.dto.FiltersDTO;
import put.poznan.txtdocsbackend.dto.PublicUrlTokenDTO;
import put.poznan.txtdocsbackend.model.TextResource;
import put.poznan.txtdocsbackend.model.User;
import put.poznan.txtdocsbackend.repository.TextResourceRepository;
import put.poznan.txtdocsbackend.repository.TextResourceSpecification;
import put.poznan.txtdocsbackend.repository.UserRepository;
import put.poznan.txtdocsbackend.repository.TagRepository;
import put.poznan.txtdocsbackend.service.exceptions.ResourceNotFoundException;
import put.poznan.txtdocsbackend.service.exceptions.TextResourceAlreadyExist;
import put.poznan.txtdocsbackend.service.exceptions.TokenExpiredException;
import put.poznan.txtdocsbackend.service.exceptions.UnauthorizedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TextResourceService {

    private final TextResourceRepository textResourceRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    private final String base64EncodedSecretKey = "UEFJMjAyM01hcml1c3pIeWJpYWtQb2xpdGVjaG5pa2E=";
    byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
    Key key = Keys.hmacShaKeyFor(keyBytes);

    @Autowired
    public TextResourceService(
            TextResourceRepository textResourceRepository,
            UserRepository userRepository,
            TagRepository tagRepository
    ) {
        this.textResourceRepository = textResourceRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    private User getUserFromHeader(String header) throws UnauthorizedException, TokenExpiredException {
        String token;
        Long userId;
        String userIdString;
        Date expirationDate;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else {
            throw new UnauthorizedException("Niepoprawny token");
        }

        try {
            Jws<Claims> parsedToken =  Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsedToken.getBody();
            userIdString = claims.getSubject();
            expirationDate = claims.getExpiration();
        } catch (Exception e) {
            throw new UnauthorizedException("Niepoprawny token");
        }

        if (expirationDate.before(new Date())) {
            throw new TokenExpiredException("Token wygasł");
        }
        userId = Long.parseLong(userIdString);
        log.info("User id: " + userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Niepoprawny token"));
    }
    public List<TextResource> getAllTextResourcesByUser(String header) throws UnauthorizedException, TokenExpiredException {
        User user = getUserFromHeader(header);
        return textResourceRepository.findAllByUser(user);
    }

    private String saveFile(MultipartFile file) throws IOException {
        String directory = System.getProperty("user.dir") + "\\images";
        log.info("Saving file to: " + directory);
        String fileName = UUID.randomUUID().toString() + file.getOriginalFilename();
        Path filePath = Paths.get(directory, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/images/" + fileName;
    }


    public void saveTextResource(TextResource resource, MultipartFile file, String header) throws TextResourceAlreadyExist, UnauthorizedException, TokenExpiredException {

        User user = getUserFromHeader(header);

        Optional<TextResource> existingResource = textResourceRepository.findByNameAndUser(resource.getName(), user);
        if (existingResource.isPresent()) {
            throw new TextResourceAlreadyExist("Zasób o podanej nazwie już istnieje");
        }

        String filePath;
        try {
            filePath = saveFile(file);
        } catch (IOException e) {
            throw new TextResourceAlreadyExist("Nie udało się zapisać pliku");
        }

        resource.setImagePath(filePath);

        resource.setUser(user);
        resource.setTags(
                resource
                        .getTags()
                        .stream()
                        .map(tagRepository::save)
                        .collect(Collectors.toList()));
        textResourceRepository.save(resource);
    }

    public TextResource getTextResourceById(Long id, String publicUrlToken, String header) throws UnauthorizedException, ResourceNotFoundException, TokenExpiredException {
        Optional<TextResource> resource;

        if (publicUrlToken != null) {
            resource = textResourceRepository.findByIdAndPublicUrlToken(id, publicUrlToken);
            if (resource.isPresent()) {
                return resource.get();
            } else {
                throw new ResourceNotFoundException("Zasób o podanym id nie istnieje");
            }
        } else {
            User user = getUserFromHeader(header);

            resource = textResourceRepository.findByIdAndUser(id, user);
            if (resource.isPresent()) {
                return resource.get();
            } else {
                throw new ResourceNotFoundException("Zasób o podanym id nie istnieje");
            }
        }
    }


    public void updateTextResource(Long id, TextResource newResource, String header) throws UnauthorizedException, ResourceNotFoundException, TokenExpiredException {

        Optional<TextResource> optionalResource;
        User user = getUserFromHeader(header);

        optionalResource = textResourceRepository.findById(id);

        if (optionalResource.isEmpty()) {
            throw new ResourceNotFoundException("Zasób nie znaleziony");
        }

        TextResource existingResource = optionalResource.get();

        if (!existingResource.getUser().equals(user)) {
            throw new UnauthorizedException("Brak uprawnień do zasobu");
        }

        existingResource.setName(newResource.getName());
        existingResource.setContent(newResource.getContent());
        existingResource.setTags(
                newResource
                        .getTags()
                        .stream()
                        .map(tagRepository::save)
                        .collect(Collectors.toList()));

        textResourceRepository.save(existingResource);
    }

    public PublicUrlTokenDTO generatePublicUrl(Long id, String header) throws UnauthorizedException, ResourceNotFoundException, TokenExpiredException {

        Optional<TextResource> optionalResource;
        User user = getUserFromHeader(header);

        optionalResource = textResourceRepository.findById(id);
        if (optionalResource.isEmpty()) {
            throw new ResourceNotFoundException("Zasób nie znaleziony");
        }

        TextResource existingResource = optionalResource.get();
        if (!existingResource.getUser().equals(user)) {
            throw new UnauthorizedException("Brak uprawnień do zasobu");
        }

        // generate uid based on resource id or resource id with user id?
        String publicUrlToken = UUID.randomUUID().toString();
        existingResource.setPublicUrlToken(publicUrlToken);
        textResourceRepository.save(existingResource);
        return new PublicUrlTokenDTO(publicUrlToken);
    }

    public void deleteTextResource(Long id, String header) throws UnauthorizedException, ResourceNotFoundException, TokenExpiredException {
        Optional<TextResource> optionalResource;
        User user = getUserFromHeader(header);

        optionalResource = textResourceRepository.findById(id);
        if (optionalResource.isEmpty()) {
            throw new ResourceNotFoundException("Zasób nie znaleziony");
        }

        TextResource existingResource = optionalResource.get();
        if (!existingResource.getUser().equals(user)) {
            throw new UnauthorizedException("Brak uprawnień do zasobu");
        }
        textResourceRepository.delete(existingResource);
    }

    public List<TextResource> getAllTextResourcesByFilters(FiltersDTO filters, String header) throws UnauthorizedException, TokenExpiredException {

        User user = getUserFromHeader(header);
        filters.setUser(user);

        if (filters.getTags().isEmpty() && filters.getContent().isEmpty() && filters.getName().isEmpty() && filters.getUser() == null) {
            return textResourceRepository.findAllByUser(user);
        }

        TextResourceSpecification spec = new TextResourceSpecification(filters);
        return textResourceRepository.findAll(spec);

    }
}
