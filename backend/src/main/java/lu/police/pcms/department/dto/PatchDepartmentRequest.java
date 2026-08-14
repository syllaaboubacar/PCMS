package lu.police.pcms.department.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente les données envoyées par le client
 * lors d'une mise à jour partielle d'un département existant.
 *
 * <p>
 * Ce DTO constitue le point d'entrée de l'API REST
 * pour l'opération {@code PATCH /departments/{id}}.
 * </p>
 *
 * <p>
 * Tous les champs sont optionnels. Seuls les champs
 * fournis (non-nuls) seront mis à jour.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchDepartmentRequest {

    /**
     * Code métier du département (optionnel).
     *
     * <p>
     * Le code ne peut pas dépasser 20 caractères.
     * </p>
     */
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
     * Nom du département (optionnel).
     *
     * <p>
     * Le nom ne peut pas dépasser 100 caractères.
     * </p>
     */
    @Size(
            max = 100,
            message = "Le nom du département ne peut pas dépasser 100 caractères."
    )
    private String name;
}