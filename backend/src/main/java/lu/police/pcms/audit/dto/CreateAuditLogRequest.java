package lu.police.pcms.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO utilisé pour créer un nouveau journal d'audit.
 *
 * <p>
 * Un journal d'audit représente une opération réalisée
 * sur une entité métier du système PCMS.
 * </p>
 *
 * <p>
 * Les champs d'identification et d'audit tels que
 * {@code id}, {@code createdAt} et {@code createdBy}
 * sont générés automatiquement et ne doivent pas
 * être fournis par le client.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuditLogRequest {

    /**
     * Identifiant de l'utilisateur à l'origine de l'action.
     */
    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    private Long userId;

    /**
     * Type d'action réalisée.
     *
     * <p>
     * Exemples : CREATE, UPDATE, DELETE, LOGIN, LOGOUT.
     * </p>
     */
    @NotBlank(message = "L'action est obligatoire")
    @Size(max = 100, message = "L'action ne peut pas dépasser 100 caractères")
    private String action;

    /**
     * Nom de l'entité métier concernée.
     *
     * <p>
     * Exemples : USER, ROLE, CASE_FILE, DEPARTMENT.
     * </p>
     */
    @NotBlank(message = "Le nom de l'entité est obligatoire")
    @Size(max = 100, message = "Le nom de l'entité ne peut pas dépasser 100 caractères")
    private String entityName;

    /**
     * Identifiant de l'enregistrement concerné.
     */
    @NotNull(message = "L'identifiant de l'entité est obligatoire")
    private Long entityId;

    /**
     * Informations complémentaires concernant l'opération.
     */
    private String details;

    /**
     * Adresse IP depuis laquelle l'action a été effectuée.
     */
    @Size(max = 45, message = "L'adresse IP ne peut pas dépasser 45 caractères")
    private String ipAddress;
}