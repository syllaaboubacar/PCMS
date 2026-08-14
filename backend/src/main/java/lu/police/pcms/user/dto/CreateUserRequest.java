package lu.police.pcms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO représentant les données nécessaires à la création
 * d'un nouvel utilisateur.
 *
 * <p>
 * Ce DTO constitue le point d'entrée de l'API REST
 * pour l'opération de création d'un utilisateur.
 * </p>
 *
 * <p>
 * Le rôle et le département sont identifiés par leur identifiant.
 * Le service se chargera ensuite de récupérer les entités
 * {@code Role} et {@code Department} correspondantes.
 * </p>
 *
 * <p>
 * Le mot de passe est fourni uniquement lors de la création.
 * Il doit être chiffré avant d'être persisté en base de données.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    /**
     * Prénom de l'utilisateur.
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(
            max = 100,
            message = "Le prénom ne peut pas dépasser 100 caractères"
    )
    private String firstName;

    /**
     * Nom de famille de l'utilisateur.
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(
            max = 100,
            message = "Le nom ne peut pas dépasser 100 caractères"
    )
    private String lastName;

    /**
     * Adresse e-mail de l'utilisateur.
     */
    @NotBlank(message = "L'adresse e-mail est obligatoire")
    @Email(message = "L'adresse e-mail doit être valide")
    @Size(
            max = 255,
            message = "L'adresse e-mail ne peut pas dépasser 255 caractères"
    )
    private String email;

    /**
     * Mot de passe initial de l'utilisateur.
     *
     * <p>
     * Le mot de passe ne doit jamais être stocké en clair.
     * Il doit être encodé avant la persistance.
     * </p>
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(
            min = 8,
            max = 255,
            message = "Le mot de passe doit contenir entre 8 et 255 caractères"
    )
    private String password;

    /**
     * Indique si le compte utilisateur est activé.
     */
    @NotNull(message = "L'état du compte est obligatoire")
    private Boolean enabled = true;

    /**
     * Identifiant du rôle attribué à l'utilisateur.
     */
    @NotNull(message = "Le rôle est obligatoire")
    private Long roleId;

    /**
     * Identifiant du département auquel appartient l'utilisateur.
     */
    @NotNull(message = "Le département est obligatoire")
    private Long departmentId;
}