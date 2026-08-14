package lu.police.pcms.casecomment.mapper;

import lu.police.pcms.casecomment.dto.CaseCommentResponse;
import lu.police.pcms.casecomment.dto.CreateCaseCommentRequest;
import lu.police.pcms.casecomment.dto.PatchCaseCommentRequest;
import lu.police.pcms.casecomment.dto.UpdateCaseCommentRequest;
import lu.police.pcms.casecomment.entity.CaseComment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link CaseComment} et ses DTO.
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
 *     <li>{@link #toEntity(CreateCaseCommentRequest)} → pour la création (POST)</li>
 *     <li>{@link #updateEntity(UpdateCaseCommentRequest, CaseComment)} → pour la mise à jour complète (PUT)</li>
 *     <li>{@link #patchEntity(PatchCaseCommentRequest, CaseComment)} → pour la mise à jour partielle (PATCH)</li>
 *     <li>{@link #toResponse(CaseComment)} → pour la réponse (GET, POST, PUT, PATCH)</li>
 * </ul>
 *
 * <p>
 * Les relations {@code caseFile} et {@code user} ne sont pas mappées directement
 * depuis les requêtes car elles utilisent des IDs. Le service se chargera de
 * charger les entités correspondantes avant de les affecter au commentaire.
 * </p>
 *
 * <p>
 * Seul le champ {@code content} est modifiable. L'association au dossier et à
 * l'utilisateur est immuable après la création.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface CaseCommentMapper {

    /**
     * Convertit un {@link CreateCaseCommentRequest} en une entité {@link CaseComment}.
     *
     * <p>
     * Les relations {@code caseFile} et {@code user} sont ignorées car le service
     * doit les charger à partir des IDs {@code caseFileId} et {@code userId}.
     * Les champs d'audit (id, createdAt, createdBy, updatedAt, updatedBy, deleted)
     * sont également ignorés (gérés automatiquement par JPA).
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link CaseComment} non persistée (sans relations)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    CaseComment toEntity(CreateCaseCommentRequest request);

    /**
     * Met à jour une entité {@link CaseComment} existante avec les données
     * d'un {@link UpdateCaseCommentRequest} (PUT).
     *
     * <p>
     * Seul le champ {@code content} est mis à jour.
     * Les relations et les champs d'audit sont ignorés.
     * </p>
     *
     * @param request DTO de mise à jour complète
     * @param entity  entité à mettre à jour (cible)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(UpdateCaseCommentRequest request, @MappingTarget CaseComment entity);

    /**
     * Met à jour partiellement une entité {@link CaseComment} avec les données
     * d'un {@link PatchCaseCommentRequest} (PATCH).
     *
     * <p>
     * Seul le champ {@code content} est modifiable et il est optionnel.
     * Si la valeur est {@code null}, le champ n'est pas modifié.
     * </p>
     *
     * @param request DTO de mise à jour partielle
     * @param entity  entité à mettre à jour (cible)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void patchEntity(PatchCaseCommentRequest request, @MappingTarget CaseComment entity);

    /**
     * Convertit une entité {@link CaseComment} en un DTO {@link CaseCommentResponse}.
     *
     * <p>
     * Les IDs du dossier et de l'utilisateur sont extraits des entités associées.
     * Tous les champs d'audit sont exposés dans la réponse.
     * </p>
     *
     * @param entity entité à convertir
     * @return DTO de réponse complet
     */
    @Mapping(source = "caseFile.id", target = "caseFileId")
    @Mapping(source = "user.id", target = "userId")
    CaseCommentResponse toResponse(CaseComment entity);
}