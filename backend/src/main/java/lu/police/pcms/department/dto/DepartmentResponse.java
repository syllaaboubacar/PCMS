package lu.police.pcms.department.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Représente les informations d'un département
 * renvoyées au client par l'API REST.
 *
 * <p>
 * Ce DTO est utilisé comme objet de réponse
 * lors des opérations de consultation,
 * de création et de modification d'un département.
 * </p>
 *
 * <p>
 * Il expose les informations métier ainsi que
 * les informations d'audit nécessaires au client,
 * tout en masquant les relations JPA et les détails
 * internes de persistance.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    /**
     * Identifiant unique du département.
     */
    private Long id;

    /**
     * Code métier unique du département.
     */
    private String code;

    /**
     * Nom du département.
     */
    private String name;

    /**
     * Date de création du département.
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
     * Indique si le département est supprimé
     * logiquement.
     */
    private Boolean deleted;
}