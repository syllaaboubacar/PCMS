package lu.police.pcms.suspect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO utilisé pour créer un nouveau suspect (POST).
 *
 * <p>
 * Le suspect doit être associé à un dossier existant
 * via son identifiant.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSuspectRequest {

    /**
     * Identifiant du dossier auquel le suspect est rattaché.
     */
    @NotNull(message = "L'identifiant du dossier est obligatoire")
    private Long caseFileId;

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