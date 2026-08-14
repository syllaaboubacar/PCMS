package lu.police.pcms.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO représentant les données d'un utilisateur retournées
 * par l'API REST.
 *
 * <p>
 * Ce DTO est utilisé pour exposer les informations d'un utilisateur
 * sans exposer directement l'entité JPA ni les informations sensibles.
 * </p>
 *
 * <p>
 * Le mot de passe n'est volontairement jamais exposé.
 * Les relations {@code Role}, {@code Department},
 * {@code CaseAssignment}, {@code CaseComment} et {@code AuditLog}
 * ne sont pas exposées sous forme d'entités JPA.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * Identifiant unique de l'utilisateur.
     */
    private Long id;

    /**
     * Prénom de l'utilisateur.
     */
    private String firstName;

    /**
     * Nom de famille de l'utilisateur.
     */
    private String lastName;

    /**
     * Adresse e-mail de l'utilisateur.
     */
    private String email;

    /**
     * Indique si le compte utilisateur est activé.
     */
    private Boolean enabled;

    /**
     * Identifiant du rôle.
     */
    private Long roleId;

    /**
     * Nom du rôle.
     */
    private String roleName;

    /**
     * Identifiant du département.
     */
    private Long departmentId;

    /**
     * Code du département.
     */
    private String departmentCode;

    /**
     * Nom du département.
     */
    private String departmentName;

    /**
     * Date de création de l'utilisateur.
     */
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    private Instant createdAt;

    /**
     * Utilisateur ayant créé l'enregistrement.
     */
    private String createdBy;

    /**
     * Date de dernière modification.
     */
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    private Instant updatedAt;

    /**
     * Utilisateur ayant effectué la dernière modification.
     */
    private String updatedBy;

    /**
     * Indique si l'utilisateur a été marqué comme supprimé.
     */
    private Boolean deleted;
}