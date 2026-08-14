package lu.police.pcms.suspect.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO utilisé pour modifier partiellement un suspect (PATCH).
 *
 * <p>
 * Tous les champs sont optionnels.
 * Seuls les champs fournis seront mis à jour.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchSuspectRequest {

    /**
     * Prénom du suspect.
     */
    @Size(
        max = 100,
        message = "Le prénom ne peut pas dépasser 100 caractères"
    )
    private String firstName;

    /**
     * Nom du suspect.
     */
    @Size(
        max = 100,
        message = "Le nom ne peut pas dépasser 100 caractères"
    )
    private String lastName;

    /**
     * Date de naissance du suspect.
     */
    private LocalDate birthDate;

    /**
     * Nationalité du suspect.
     */
    @Size(
        max = 100,
        message = "La nationalité ne peut pas dépasser 100 caractères"
    )
    private String nationality;

    /**
     * Notes complémentaires sur le suspect.
     */
    private String notes;
}