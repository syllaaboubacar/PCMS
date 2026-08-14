package lu.police.pcms.suspect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO utilisé pour remplacer complètement un suspect (PUT).
 *
 * <p>
 * Tous les champs modifiables doivent être fournis.
 * L'identifiant du dossier (caseFileId) n'est pas modifiable.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSuspectRequest {

    /**
     * Prénom du suspect.
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(
        max = 100,
        message = "Le prénom ne peut pas dépasser 100 caractères"
    )
    private String firstName;

    /**
     * Nom du suspect.
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(
        max = 100,
        message = "Le nom ne peut pas dépasser 100 caractères"
    )
    private String lastName;

    /**
     * Date de naissance du suspect (optionnelle).
     */
    private LocalDate birthDate;

    /**
     * Nationalité du suspect (optionnelle).
     */
    @Size(
        max = 100,
        message = "La nationalité ne peut pas dépasser 100 caractères"
    )
    private String nationality;

    /**
     * Notes complémentaires sur le suspect (optionnelles).
     */
    private String notes;
}