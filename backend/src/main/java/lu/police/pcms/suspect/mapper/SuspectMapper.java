package lu.police.pcms.suspect.mapper;

import lu.police.pcms.suspect.dto.CreateSuspectRequest;
import lu.police.pcms.suspect.dto.PatchSuspectRequest;
import lu.police.pcms.suspect.dto.SuspectResponse;
import lu.police.pcms.suspect.dto.UpdateSuspectRequest;
import lu.police.pcms.suspect.entity.Suspect;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link Suspect} et ses DTO.
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
 *     <li>{@link #toEntity(CreateSuspectRequest)} → pour la création (POST)</li>
 *     <li>{@link #updateEntity(UpdateSuspectRequest, Suspect)} → pour la mise à jour complète (PUT)</li>
 *     <li>{@link #patchEntity(PatchSuspectRequest, Suspect)} → pour la mise à jour partielle (PATCH)</li>
 *     <li>{@link #toResponse(Suspect)} → pour la réponse (GET, POST, PUT, PATCH)</li>
 * </ul>
 *
 * <p>
 * La relation {@code caseFile} n'est pas mappée directement depuis les requêtes,
 * car elle utilise un ID. Le service se chargera de charger l'entité
 * {@code CaseFile} correspondante avant de l'affecter au suspect.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface SuspectMapper {

    /**
     * Convertit un {@link CreateSuspectRequest} en une entité {@link Suspect}.
     *
     * <p>
     * La relation {@code caseFile} est ignorée car le service doit la charger
     * à partir de l'ID {@code caseFileId}.
     * Les champs d'audit (id, createdAt, createdBy, updatedAt, updatedBy, deleted)
     * sont également ignorés (gérés automatiquement par JPA).
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link Suspect} non persistée (sans relation)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Suspect toEntity(CreateSuspectRequest request);

    /**
     * Met à jour une entité {@link Suspect} existante avec les données
     * d'un {@link UpdateSuspectRequest} (PUT).
     *
     * <p>
     * Tous les champs de la requête sont écrasés dans l'entité.
     * La relation {@code caseFile} n'est pas modifiable via cette opération
     * (on ne peut pas changer de dossier pour un suspect).
     * Les champs d'audit sont ignorés.
     * </p>
     *
     * @param request DTO de mise à jour complète
     * @param entity  entité à mettre à jour (cible)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(UpdateSuspectRequest request, @MappingTarget Suspect entity);

    /**
     * Met à jour partiellement une entité {@link Suspect} avec les données
     * d'un {@link PatchSuspectRequest} (PATCH).
     *
     * <p>
     * Seuls les champs non-nuls de la requête sont copiés dans l'entité.
     * La relation {@code caseFile} n'est pas modifiable.
     * Les champs d'audit sont ignorés.
     * </p>
     *
     * @param request DTO de mise à jour partielle
     * @param entity  entité à mettre à jour (cible)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseFile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void patchEntity(PatchSuspectRequest request, @MappingTarget Suspect entity);

    /**
     * Convertit une entité {@link Suspect} en un DTO {@link SuspectResponse}.
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
    SuspectResponse toResponse(Suspect entity);
}