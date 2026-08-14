package lu.police.pcms.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente les données envoyées par le client
 * lors de la création d'un nouveau rôle.
 *
 * <p>
 * Ce DTO constitue le point d'entrée de l'API REST
 * pour l'opération {@code POST /roles}.
 * </p>
 *
 * <p>
 * Seules les informations métier nécessaires à la
 * création d'un rôle sont exposées.
 * Les informations techniques (identifiant, audit,
 * suppression logique, relations JPA) sont gérées
 * par le serveur.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {

    /**
     * Nom du rôle.
     *
     * <p>
     * Le nom doit respecter la convention Spring Security
     * en commençant obligatoirement par {@code ROLE_}.
     * </p>
     *
     * <p>
     * Exemples valides :
     * </p>
     * <ul>
     *     <li>ROLE_ADMIN</li>
     *     <li>ROLE_USER</li>
     *     <li>ROLE_SUPERVISOR</li>
     *     <li>ROLE_CASE_MANAGER</li>
     * </ul>
     */
    @NotBlank(message = "Le nom du rôle est obligatoire.")
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
     * Description fonctionnelle du rôle.
     *
     * <p>
     * Ce champ est facultatif et permet de documenter
     * l'utilisation du rôle dans le système.
     * </p>
     */
    @Size(
            max = 255,
            message = "La description ne peut pas dépasser 255 caractères."
    )
    private String description;
}