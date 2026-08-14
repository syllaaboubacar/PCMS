package lu.police.pcms.casefile.mapper;

import lu.police.pcms.casefile.dto.CaseFileResponse;
import lu.police.pcms.casefile.dto.CreateCaseFileRequest;
import lu.police.pcms.casefile.dto.PatchCaseFileRequest;
import lu.police.pcms.casefile.dto.UpdateCaseFileRequest;
import lu.police.pcms.casefile.entity.CaseFile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link CaseFile} et ses DTO.
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
 *     <li>{@link #toEntity(CreateCaseFileRequest)} → pour la création (POST)</li>
 *     <li>{@link #updateEntity(UpdateCaseFileRequest, CaseFile)} → pour la mise à jour complète (PUT)</li>
 *     <li>{@link #patchEntity(PatchCaseFileRequest, CaseFile)} → pour la mise à jour partielle (PATCH)</li>
 *     <li>{@link #toResponse(CaseFile)} → pour la réponse (GET, POST, PUT, PATCH)</li>
 * </ul>
 *
 * <p>
 * Les relations {@code caseAssignments}, {@code suspects}, {@code attachments}
 * et {@code caseComments} ne sont pas exposées dans les DTO pour éviter
 * les cycles et les surcharges de données.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface CaseFileMapper {

    /**
     * Convertit un {@link CreateCaseFileRequest} en une entité {@link CaseFile}.
     *
     * <p>
     * Le champ {@code closedAt} n'est pas présent dans la requête de création,
     * il est donc ignoré. Les champs d'audit et les relations sont également ignorés.
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link CaseFile} non persistée
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "closedAt", ignore = true)          // Absent dans CreateCaseFileRequest
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "caseAssignments", ignore = true)
    @Mapping(target = "suspects", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "caseComments", ignore = true)
    CaseFile toEntity(CreateCaseFileRequest request);

    /**
     * Met à jour une entité {@link CaseFile} existante avec les données
     * d'un {@link UpdateCaseFileRequest} (PUT).
     *
     * <p>
     * Le {@code caseNumber} est immuable et n'est pas présent dans la requête,
     * donc ignoré. Le {@code openedAt} n'est pas modifiable et absent de la requête,
     * donc ignoré. Les champs d'audit et les relations sont également ignorés.
     * </p>
     *
     * @param request DTO de mise à jour complète
     * @param entity  entité à mettre à jour (cible)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseNumber", ignore = true)          // Immuable
    @Mapping(target = "openedAt", ignore = true)            // Absent dans UpdateCaseFileRequest
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "caseAssignments", ignore = true)
    @Mapping(target = "suspects", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "caseComments", ignore = true)
    void updateEntity(UpdateCaseFileRequest request, @MappingTarget CaseFile entity);

    /**
     * Met à jour partiellement une entité {@link CaseFile} avec les données
     * d'un {@link PatchCaseFileRequest} (PATCH).
     *
     * <p>
     * Seuls les champs non-nuls de la requête sont copiés dans l'entité.
     * Le {@code caseNumber} est immuable, le {@code openedAt} est absent,
     * les champs d'audit et les relations sont ignorés.
     * </p>
     *
     * @param request DTO de mise à jour partielle
     * @param entity  entité à mettre à jour (cible)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseNumber", ignore = true)          // Immuable
    @Mapping(target = "openedAt", ignore = true)            // Absent dans PatchCaseFileRequest
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "caseAssignments", ignore = true)
    @Mapping(target = "suspects", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "caseComments", ignore = true)
    void patchEntity(PatchCaseFileRequest request, @MappingTarget CaseFile entity);

    /**
     * Convertit une entité {@link CaseFile} en un DTO {@link CaseFileResponse}.
     *
     * <p>
     * Tous les champs de l'entité sont exposés dans la réponse :
     * les données métier (id, caseNumber, title, description, status,
     * priority, openedAt, closedAt, incidentDate, location) ainsi que
     * les champs d'audit (createdAt, createdBy, updatedAt, updatedBy, deleted).
     * </p>
     *
     * @param entity entité à convertir
     * @return DTO de réponse complet
     */
    CaseFileResponse toResponse(CaseFile entity);
}