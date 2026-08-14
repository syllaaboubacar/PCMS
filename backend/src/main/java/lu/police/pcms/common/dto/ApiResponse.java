package lu.police.pcms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Wrapper générique pour toutes les réponses HTTP.
 *
 * <p>
 * Ce wrapper standardise les réponses de l'API avec :
 * <ul>
 *     <li>un indicateur de succès ({@code success}) ;</li>
 *     <li>un message explicatif ({@code message}) ;</li>
 *     <li>la donnée métier ({@code data}) ;</li>
 *     <li>l'horodatage de la réponse ({@code timestamp}).</li>
 * </ul>
 * </p>
 *
 * <p>
 * Exemple de réponse (succès) :
 * <pre>
 * {
 *     "success": true,
 *     "message": "Rôle créé avec succès",
 *     "data": { "id": 1, "name": "ROLE_ADMIN" },
 *     "timestamp": "2026-08-11T10:00:00Z"
 * }
 * </pre>
 * </p>
 *
 * <p>
 * Exemple de réponse (erreur) :
 * <pre>
 * {
 *     "success": false,
 *     "message": "Rôle introuvable avec l'identifiant : 99",
 *     "data": null,
 *     "timestamp": "2026-08-11T10:00:00Z"
 * }
 * </pre>
 * </p>
 *
 * @param <T> le type de la donnée métier
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indique si la requête a réussi.
     */
    private boolean success;

    /**
     * Message explicatif pour le client.
     */
    private String message;

    /**
     * La donnée métier (peut être {@code null} en cas d'erreur).
     */
    private T data;

    /**
     * Horodatage de la réponse (format ISO-8601, UTC).
     */
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    @Builder.Default
    private Instant timestamp = Instant.now();

    // ============================================================
    // Méthodes statiques pour construire facilement des réponses
    // ============================================================

    /**
     * Construit une réponse de succès sans donnée.
     *
     * @param message Message de succès
     * @param <T>     Le type de la donnée
     * @return Une instance de {@code ApiResponse}
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Construit une réponse de succès avec donnée.
     *
     * @param message Message de succès
     * @param data    La donnée métier
     * @param <T>     Le type de la donnée
     * @return Une instance de {@code ApiResponse}
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Construit une réponse d'erreur.
     *
     * @param message Message d'erreur
     * @param <T>     Le type de la donnée (généralement {@code Void})
     * @return Une instance de {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}