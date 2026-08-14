package lu.police.pcms.role.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente les données envoyées par le client
 * lors d'une mise à jour partielle d'un rôle existant.
 *
 * <p>
 * Ce DTO constitue le point d'entrée de l'API REST
 * pour l'opération {@code PATCH /roles/{id}}.
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
public class PatchRoleRequest {

    /**
     * Nom du rôle (optionnel).
     *
     * <p>
     * Le nom doit respecter la convention Spring Security
     * en commençant obligatoirement par {@code ROLE_}.
     * </p>
     */
    @Size(
            min = 6,
            max = 50,
            message = "Le nom du rôle doit contenir entre 6 et 50 caractères."
    )
    @Pattern(
            regexp = "^ROLE_[A-Z][A-Z0-9_]*$",
            message = "Le nom du rôle doit commencer par 'ROLE_' et contenir uniquement des lettres majuscules, des chiffres et des underscores."
    )
    private String name;

    /**
     * Description fonctionnelle du rôle (optionnelle).
     */
    @Size(
            max = 255,
            message = "La description ne peut pas dépasser 255 caractères."
    )
    private String description;
}