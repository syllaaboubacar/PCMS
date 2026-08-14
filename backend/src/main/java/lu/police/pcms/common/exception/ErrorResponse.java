package lu.police.pcms.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO représentant une réponse d'erreur structurée.
 *
 * <p>
 * Ce DTO est utilisé par le {@link GlobalExceptionHandler} pour renvoyer
 * des informations d'erreur détaillées au client.
 * </p>
 *
 * <p>
 * Exemple de réponse (validation) :
 * <pre>
 * {
 *     "status": 400,
 *     "error": "Bad Request",
 *     "message": "Validation échouée",
 *     "timestamp": "2026-08-11T10:00:00Z",
 *     "path": "/api/roles",
 *     "errors": {
 *         "name": "Le nom du rôle est obligatoire."
 *     }
 * }
 * </pre>
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Code HTTP de la réponse.
     */
    private int status;

    /**
     * Message d'erreur général.
     */
    private String error;

    /**
     * Message détaillé de l'erreur.
     */
    private String message;

    /**
     * Horodatage de l'erreur (format ISO-8601, UTC).
     */
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Chemin de la requête ayant provoqué l'erreur.
     */
    private String path;

    /**
     * Détails des erreurs de validation (champ → message).
     */
    private Map<String, String> errors;

    /**
     * Liste d'erreurs supplémentaires (pour les erreurs multiples).
     */
    private List<String> details;

    // ============================================================
    // Méthodes statiques pour simplifier la construction
    // ============================================================

    /**
     * Construit une erreur de validation (400).
     *
     * @param path    Chemin de la requête
     * @param errors  Map des erreurs (champ → message)
     * @return Une instance de {@code ErrorResponse}
     */
    public static ErrorResponse validationError(String path, Map<String, String> errors) {
        return ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Validation échouée")
                .path(path)
                .errors(errors)
                .build();
    }

    /**
     * Construit une erreur 404 (Ressource non trouvée).
     *
     * @param path    Chemin de la requête
     * @param message Message d'erreur
     * @return Une instance de {@code ErrorResponse}
     */
    public static ErrorResponse notFoundError(String path, String message) {
        return ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Construit une erreur 409 (Conflit).
     *
     * @param path    Chemin de la requête
     * @param message Message d'erreur
     * @return Une instance de {@code ErrorResponse}
     */
    public static ErrorResponse conflictError(String path, String message) {
        return ErrorResponse.builder()
                .status(409)
                .error("Conflict")
                .message(message)
                .path(path)
                .build();
    }
}