package lu.police.pcms.caseassignment.mapper;

import lu.police.pcms.caseassignment.dto.CaseAssignmentResponse;
import lu.police.pcms.caseassignment.dto.CreateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.PatchCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.UpdateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.entity.CaseAssignment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link CaseAssignment} et ses DTO.
 *
 * <p>
 * Ce mapper utilise {@code componentModel = "spring"} pour être injecté
 * automatiquement par Spring dans les services et contrôleurs.
 * </p>
 *
 * <p>
 * Les méthodes de mapping sont les suivantes :
 * </p>
 * <ul>
 *     <li>{@link #toEntity(CreateCaseAssignmentRequest)} → pour la création (POST)</li>
 *     <li>{@link #updateEntity(UpdateCaseAssignmentRequest, CaseAssignment)} → pour la mise à jour complète (PUT)</li>
 *     <li>{@link #patchEntity(PatchCaseAssignmentRequest, CaseAssignment)} → pour la mise à jour partielle (PATCH)</li>
 *     <li>{@link #toResponse(CaseAssignment)} → pour la réponse (GET, POST, PUT, PATCH)</li>
 * </ul>
 *
 * <p>
 * Les relations {@code caseFile} et {@code user} ne sont pas mappées directement
 * depuis les requêtes car elles utilisent des IDs. Le service se chargera de
 * charger les entités correspondantes avant de les affecter à l'entité.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface CaseAssignmentMapper {

    /**
     * Convertit un {@link CreateCaseAssignmentRequest} en une entité {@link CaseAssignment}.
     *
     * <p>
     * Les relations {@code caseFile} et {@code user} sont ignorées car le service
     * doit les charger à partir des IDs {@code caseFileId} et {@code userId}.
     * Le champ {@code active} n'est pas présent dans la requête ; la valeur par défaut
     * {@code true} sera définie par JPA (via la colonne {@code DEFAULT TRUE}).
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link CaseAssignment} non persistée (sans relations)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "active", ignore = true) // géré par la base de données (DEFAULT TRUE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    CaseAssignment toEntity(CreateCaseAssignmentRequest request);

    /**
     * Met à jour une entité {@link CaseAssignment} existante avec les données
     * d'un {@link UpdateCaseAssignmentRequest} (PUT).
     *
     * <p>
     * Tous les champs de la requête sont écrasés dans l'entité.
     * Les relations {@code caseFile} et {@code user} ne sont pas modifiables
     * via cette opération.
     * </p>
     *
     * @param request DTO de mise à jour complète
     * @param entity  entité à mettre à jour (cible)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "assignedAt", ignore = true) // immuable après création
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(UpdateCaseAssignmentRequest request, @MappingTarget CaseAssignment entity);

    /**
     * Met à jour partiellement une entité {@link CaseAssignment} avec les données
     * d'un {@link PatchCaseAssignmentRequest} (PATCH).
     *
     * <p>
     * Seul le champ {@code active} est modifiable, et il est optionnel.
     * Les relations et {@code assignedAt} sont immuables.
     * </p>
     *
     * @param request DTO de mise à jour partielle
     * @param entity  entité à mettre à jour (cible)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void patchEntity(PatchCaseAssignmentRequest request, @MappingTarget CaseAssignment entity);

    /**
     * Convertit une entité {@link CaseAssignment} en un DTO {@link CaseAssignmentResponse}.
     *
     * <p>
     * Les IDs du dossier et de l'utilisateur sont extraits des entités associées.
     * Tous les champs d'audit sont exposés.
     * </p>
     *
     * @param entity entité à convertir
     * @return DTO de réponse complet
     */
    @Mapping(source = "caseFile.id", target = "caseFileId")
    @Mapping(source = "user.id", target = "userId")
    CaseAssignmentResponse toResponse(CaseAssignment entity);
}