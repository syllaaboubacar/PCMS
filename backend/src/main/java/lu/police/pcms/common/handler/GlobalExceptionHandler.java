package lu.police.pcms.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ErrorResponse;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour l'API REST.
 *
 * <p>
 * Ce gestionnaire intercepte toutes les exceptions levées par les contrôleurs
 * et les transforme en réponses HTTP structurées et cohérentes.
 * </p>
 *
 * <p>
 * Les exceptions suivantes sont gérées :
 * </p>
 * <ul>
 *     <li>{@link ResourceNotFoundException} → 404 Not Found</li>
 *     <li>{@link DuplicateResourceException} → 409 Conflict</li>
 *     <li>{@link MethodArgumentNotValidException} → 400 Bad Request (validation DTO)</li>
 *     <li>{@link ConstraintViolationException} → 400 Bad Request (validation paramètres)</li>
 *     <li>{@link Exception} → 500 Internal Server Error (fallback)</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les exceptions {@link ResourceNotFoundException}.
     * 
     * @param ex      L'exception
     * @param request La requête HTTP
     * @return Une réponse 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Ressource non trouvée : {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.notFoundError(
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Gère les exceptions {@link DuplicateResourceException}.
     *
     * @param ex      L'exception
     * @param request La requête HTTP
     * @return Une réponse 409 Conflict
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        log.warn("Conflit de ressource : {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.conflictError(
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Gère les exceptions de validation des DTO (issues de {@code @Valid}).
     *
     * @param ex      L'exception
     * @param request La requête HTTP
     * @return Une réponse 400 Bad Request avec les détails des erreurs
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Validation échouée : {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });

        ErrorResponse error = ErrorResponse.validationError(
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Gère les exceptions de validation des paramètres
     * (ex: {@code @PathVariable}, {@code @RequestParam}).
     *
     * @param ex      L'exception
     * @param request La requête HTTP
     * @return Une réponse 400 Bad Request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        log.warn("Contrainte violée : {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.validationError(
                request.getRequestURI(),
                Map.of("validation", ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Gère toutes les autres exceptions (fallback).
     *
     * @param ex      L'exception
     * @param request La requête HTTP
     * @return Une réponse 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Erreur interne du serveur : ", ex);

        ErrorResponse error = ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("Une erreur inattendue s'est produite.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}