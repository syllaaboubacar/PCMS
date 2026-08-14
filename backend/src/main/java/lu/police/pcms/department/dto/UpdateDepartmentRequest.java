package lu.police.pcms.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente les données envoyées par le client
 * lors de la modification d'un département existant.
 *
 * <p>
 * Ce DTO constitue le point d'entrée de l'API REST
 * pour l'opération {@code PUT /departments/{id}}.
 * </p>
 *
 * <p>
 * Les informations techniques et les données d'audit
 * restent entièrement gérées par le serveur.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentRequest {

    /**
     * Code métier du département.
     *
     * <p>
     * Le code est obligatoire et ne peut pas dépasser
     * 20 caractères.
     * </p>
     */
    @NotBlank(message = "Le code du département est obligatoire.")
    @Size(
            max = 20,
            message = "Le code du département ne peut pas dépasser 20 caractères."
    )
    @Pattern(
            regexp = "^[A-Z0-9_-]+$",
            message = "Le code doit contenir uniquement des lettres majuscules, des chiffres, des tirets (-) et des underscores (_)."
    )
    private String code;

    /**
     * Nom du département.
     *
     * <p>
     * Le nom est obligatoire et ne peut pas dépasser
     * 100 caractères.
     * </p>
     */
    @NotBlank(message = "Le nom du département est obligatoire.")
    @Size(
            max = 100,
            message = "Le nom du département ne peut pas dépasser 100 caractères."
    )
    private String name;
}