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
 * DTO représentant les données nécessaires à la modification
 * d'un utilisateur existant.
 *
 * <p>
 * Ce DTO est utilisé par l'API REST lors de la modification
 * des informations métier d'un utilisateur.
 * </p>
 *
 * <p>
 * Le mot de passe n'est volontairement pas présent dans ce DTO.
 * La modification du mot de passe devra être réalisée via
 * une opération dédiée.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

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
     * Indique si le compte utilisateur est activé.
     */
    @NotNull(message = "L'état du compte est obligatoire")
    private Boolean enabled;

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