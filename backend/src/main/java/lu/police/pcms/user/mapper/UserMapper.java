package lu.police.pcms.user.mapper;

import lu.police.pcms.user.dto.CreateUserRequest;
import lu.police.pcms.user.dto.PatchUserRequest;
import lu.police.pcms.user.dto.UpdateUserRequest;
import lu.police.pcms.user.dto.UserResponse;
import lu.police.pcms.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link User} et ses DTO.
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
 *     <li>{@link #toEntity(CreateUserRequest)} → pour la création (POST)</li>
 *     <li>{@link #updateEntity(UpdateUserRequest, User)} → pour la mise à jour complète (PUT)</li>
 *     <li>{@link #patchEntity(PatchUserRequest, User)} → pour la mise à jour partielle (PATCH)</li>
 *     <li>{@link #toResponse(User)} → pour la réponse (GET, POST, PUT, PATCH)</li>
 * </ul>
 *
 * <p>
 * Le mot de passe est géré uniquement à la création. Il est absent
 * des DTO de mise à jour pour des raisons de sécurité.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface UserMapper {

    /**
     * Convertit un {@link CreateUserRequest} en une entité {@link User}.
     *
     * <p>
     * Les champs d'audit (id, createdAt, createdBy, updatedAt, updatedBy, deleted)
     * sont ignorés (gérés automatiquement par JPA).
     * Les champs {@code role} et {@code department} sont ignorés,
     * car le service doit les charger à partir des IDs {@code roleId}
     * et {@code departmentId}.
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link User} non persistée (sans relations)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "caseAssignments", ignore = true)
    @Mapping(target = "caseComments", ignore = true)
    @Mapping(target = "auditLogs", ignore = true)
    User toEntity(CreateUserRequest request);

    /**
     * Met à jour une entité {@link User} existante avec les données
     * d'un {@link UpdateUserRequest} (PUT).
     *
     * <p>
     * Tous les champs de la requête sont écrasés dans l'entité.
     * Le mot de passe n'est pas modifié par cette opération.
     * Les relations {@code role} et {@code department} doivent être
     * mises à jour par le service à partir des IDs.
     * </p>
     *
     * @param request DTO de mise à jour complète
     * @param entity  entité à mettre à jour (cible)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true) // Sécurité : ne pas modifier via ce DTO
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "caseAssignments", ignore = true)
    @Mapping(target = "caseComments", ignore = true)
    @Mapping(target = "auditLogs", ignore = true)
    void updateEntity(UpdateUserRequest request, @MappingTarget User entity);

    /**
     * Met à jour partiellement une entité {@link User} avec les données
     * d'un {@link PatchUserRequest} (PATCH).
     *
     * <p>
     * Seuls les champs non-nuls de la requête sont copiés dans l'entité.
     * Le mot de passe n'est pas modifiable via cette opération.
     * Les relations {@code role} et {@code department} doivent être
     * mises à jour par le service à partir des IDs si fournis.
     * </p>
     *
     * @param request DTO de mise à jour partielle
     * @param entity  entité à mettre à jour (cible)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "caseAssignments", ignore = true)
    @Mapping(target = "caseComments", ignore = true)
    @Mapping(target = "auditLogs", ignore = true)
    void patchEntity(PatchUserRequest request, @MappingTarget User entity);

    /**
     * Convertit une entité {@link User} en un DTO {@link UserResponse}.
     *
     * <p>
     * Le mot de passe est volontairement exclu.
     * Les informations du rôle et du département sont extraites
     * des entités associées pour enrichir la réponse.
     * </p>
     *
     * @param entity entité à convertir
     * @return DTO de réponse complet
     */
    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.code", target = "departmentCode")
    @Mapping(source = "department.name", target = "departmentName")
    UserResponse toResponse(User entity);
}