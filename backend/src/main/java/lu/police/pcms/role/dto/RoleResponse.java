package lu.police.pcms.role.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Représente les informations d'un rôle
 * renvoyées au client par l'API REST.
 *
 * <p>
 * Ce DTO est utilisé comme objet de réponse
 * pour les opérations de consultation,
 * de création et de modification des rôles.
 * </p>
 *
 * <p>
 * Il expose uniquement les informations
 * utiles au client tout en masquant les
 * relations JPA et les détails techniques
 * de persistance.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    /**
     * Identifiant unique du rôle.
     */
    private Long id;

    /**
     * Nom du rôle.
     */
    private String name;

    /**
     * Description fonctionnelle du rôle.
     */
    private String description;

    /**
     * Date de création du rôle.
     */
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    private Instant createdAt;

    /**
     * Auteur de la création.
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
     * Auteur de la dernière modification.
     */
    private String updatedBy;

    /**
     * Indique si le rôle est supprimé
     * logiquement.
     */
    private Boolean deleted;
}