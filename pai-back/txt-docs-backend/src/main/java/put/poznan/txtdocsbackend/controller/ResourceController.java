package put.poznan.txtdocsbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import put.poznan.txtdocsbackend.dto.FiltersDTO;
import put.poznan.txtdocsbackend.dto.PublicUrlTokenDTO;
import put.poznan.txtdocsbackend.model.TextResource;

import put.poznan.txtdocsbackend.service.TextResourceService;
import put.poznan.txtdocsbackend.service.exceptions.ResourceNotFoundException;
import put.poznan.txtdocsbackend.service.exceptions.TextResourceAlreadyExist;
import put.poznan.txtdocsbackend.service.exceptions.TokenExpiredException;
import put.poznan.txtdocsbackend.service.exceptions.UnauthorizedException;

import java.util.List;


@CrossOrigin
@RestController
@RequestMapping("/api/resource")
@Slf4j
public class ResourceController {

    private final TextResourceService textResourceService;

    @Autowired
    public ResourceController(
            TextResourceService textResourceService
    ) {
    this.textResourceService = textResourceService;
    }

    @GetMapping("/{id}/{publicUrlToken}")
    @Transactional
    public ResponseEntity<?> getResourceById(@PathVariable Long id, @PathVariable String publicUrlToken, HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        TextResource resource;
        try {
            resource = textResourceService.getTextResourceById(id, publicUrlToken, header);
        } catch (UnauthorizedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (TokenExpiredException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token wygasł");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resource);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<?> getResourceById(@PathVariable Long id, HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        TextResource resource;
        try {
            resource = textResourceService.getTextResourceById(id, null, header);
        } catch (UnauthorizedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (TokenExpiredException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token wygasł");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResource(@PathVariable Long id, @Valid @RequestBody TextResource resource, HttpServletRequest request) {

        String header = request.getHeader("Authorization");
        try {
            textResourceService.updateTextResource(id, resource, header);
        } catch (UnauthorizedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Brak tokenu");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body("Zasób nie znaleziony");
        } catch (TokenExpiredException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token wygasł");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Zasób zaktualizowany");
    }


    @GetMapping("/list")
    @Transactional
    public ResponseEntity<?> getResourceList(HttpServletRequest request) {

        String header = request.getHeader("Authorization");
        List<TextResource> resources;
        try {
            resources = textResourceService.getAllTextResourcesByUser(header);
        } catch (UnauthorizedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        } catch (TokenExpiredException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token wygasł");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resources);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createResource(@RequestPart("resource") TextResource resource,
                                            @RequestPart("file") MultipartFile file,
                                            @RequestHeader("Authorization") String header) {

        try {
            textResourceService.saveTextResource(resource, file, header);
        } catch (UnauthorizedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Brak tokenu");
        } catch (TextResourceAlreadyExist e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Zasób już istnieje");
        } catch (TokenExpiredException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token wygasł");
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Zasób utworzony");
    }


    @GetMapping("/{id}/generate-url")
    public ResponseEntity<?> generatePublicUrl(@PathVariable Long id, HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        PublicUrlTokenDTO publicUrlToken;

        try {
            publicUrlToken = textResourceService.generatePublicUrl(id, header);
        } catch (ResourceNotFoundException e) {
            log.info("Resource not found");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Zasób nie znaleziony");
        } catch (UnauthorizedException e) {
            log.info("Unauthorized");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Brak tokenu");
        } catch (TokenExpiredException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token wygasł");
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(publicUrlToken);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable Long id, HttpServletRequest request) {

        String header = request.getHeader("Authorization");
        try {
            textResourceService.deleteTextResource(id, header);
        } catch (UnauthorizedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Brak tokenu");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body("Zasób nie znaleziony");
        } catch (TokenExpiredException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token wygasł");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Zasób usunięty");
    }

    @PostMapping("/filter")
    public ResponseEntity<?> getResourceListByFilters(@RequestBody FiltersDTO filters, HttpServletRequest request) {

        String header = request.getHeader("Authorization");
        List<TextResource> resources;
        try {
            resources = textResourceService.getAllTextResourcesByFilters(filters, header);
        } catch (UnauthorizedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Brak tokenu");
        } catch (TokenExpiredException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token wygasł");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resources);
    }


}