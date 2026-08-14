package lu.police.pcms.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO retourné par l'API REST pour représenter
 * un journal d'audit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    /**
     * Identifiant du journal d'audit.
     */
    private Long id;

    /**
     * Identifiant de l'utilisateur à l'origine de l'action.
     */
    private Long userId;

    /**
     * Type d'action réalisée.
     *
     * <p>
     * Exemples : CREATE, UPDATE, DELETE, LOGIN, LOGOUT.
     * </p>
     */
    private String action;

    /**
     * Nom de l'entité métier concernée.
     */
    private String entityName;

    /**
     * Identifiant de l'entité concernée.
     */
    private Long entityId;

    /**
     * Informations complémentaires concernant l'action.
     */
    private String details;

    /**
     * Adresse IP depuis laquelle l'action a été réalisée.
     */
    private String ipAddress;

    // ===== Champs d'audit =====

    /**
     * Date et heure de création du journal.
     */
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    private Instant createdAt;

    /**
     * Utilisateur ou contexte ayant créé le journal.
     */
    private String createdBy;
}