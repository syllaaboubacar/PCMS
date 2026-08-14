package lu.police.pcms.attachment.mapper;

import lu.police.pcms.attachment.dto.AttachmentResponse;
import lu.police.pcms.attachment.dto.CreateAttachmentRequest;
import lu.police.pcms.attachment.dto.PatchAttachmentRequest;
import lu.police.pcms.attachment.dto.UpdateAttachmentRequest;
import lu.police.pcms.attachment.entity.Attachment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link Attachment} et ses DTO.
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
 *     <li>{@link #toEntity(CreateAttachmentRequest)} → pour la création (POST)</li>
 *     <li>{@link #updateEntity(UpdateAttachmentRequest, Attachment)} → pour la mise à jour complète (PUT)</li>
 *     <li>{@link #patchEntity(PatchAttachmentRequest, Attachment)} → pour la mise à jour partielle (PATCH)</li>
 *     <li>{@link #toResponse(Attachment)} → pour la réponse (GET, POST, PUT, PATCH)</li>
 * </ul>
 *
 * <p>
 * La relation {@code caseFile} n'est pas mappée directement depuis les requêtes,
 * car elle utilise un ID. Le service se chargera de charger l'entité
 * {@code CaseFile} correspondante avant de l'affecter à la pièce jointe.
 * </p>
 *
 * <p>
 * Les champs {@code filename} et {@code storagePath} sont générés
 * automatiquement par le service et ne sont donc pas présents dans les DTO
 * de création ou de mise à jour.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface AttachmentMapper {

    /**
     * Convertit un {@link CreateAttachmentRequest} en une entité {@link Attachment}.
     *
     * <p>
     * La relation {@code caseFile} est ignorée car le service doit la charger
     * à partir de l'ID {@code caseFileId}.
     * Les champs {@code filename} et {@code storagePath} sont ignorés car
     * ils sont générés automatiquement par le service.
     * Le champ {@code uploadedAt} est ignoré car il est géré par le service
     * (soit fourni par le client, soit généré).
     * Les champs d'audit (id, createdAt, createdBy, updatedAt, updatedBy, deleted)
     * sont également ignorés (gérés automatiquement par JPA).
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link Attachment} non persistée (sans relation et sans champs générés)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "filename", ignore = true)          // Généré par le service
    @Mapping(target = "storagePath", ignore = true)       // Généré par le service
    @Mapping(target = "uploadedAt", ignore = true)        // Géré par le service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Attachment toEntity(CreateAttachmentRequest request);

    /**
     * Met à jour une entité {@link Attachment} existante avec les données
     * d'un {@link UpdateAttachmentRequest} (PUT).
     *
     * <p>
     * Seuls les champs {@code mimeType} et {@code type} sont modifiables.
     * Tous les autres champs sont ignorés.
     * </p>
     *
     * @param request DTO de mise à jour complète
     * @param entity  entité à mettre à jour (cible)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "filename", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "storagePath", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(UpdateAttachmentRequest request, @MappingTarget Attachment entity);

    /**
     * Met à jour partiellement une entité {@link Attachment} avec les données
     * d'un {@link PatchAttachmentRequest} (PATCH).
     *
     * <p>
     * Seuls les champs non-nuls de la requête sont copiés dans l'entité.
     * Les champs modifiables sont {@code mimeType} et {@code type}.
     * </p>
     *
     * @param request DTO de mise à jour partielle
     * @param entity  entité à mettre à jour (cible)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "filename", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "storagePath", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void patchEntity(PatchAttachmentRequest request, @MappingTarget Attachment entity);

    /**
     * Convertit une entité {@link Attachment} en un DTO {@link AttachmentResponse}.
     *
     * <p>
     * L'identifiant du dossier est extrait de la relation {@code caseFile}.
     * Tous les champs d'audit sont exposés dans la réponse.
     * </p>
     *
     * @param entity entité à convertir
     * @return DTO de réponse complet
     */
    @Mapping(source = "caseFile.id", target = "caseFileId")
    AttachmentResponse toResponse(Attachment entity);
}