package lu.police.pcms.role.mapper;

import lu.police.pcms.role.dto.CreateRoleRequest;
import lu.police.pcms.role.dto.PatchRoleRequest;
import lu.police.pcms.role.dto.RoleResponse;
import lu.police.pcms.role.dto.UpdateRoleRequest;
import lu.police.pcms.role.entity.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link Role} et ses DTO.
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
 *     <li>{@link #toRole(CreateRoleRequest)} → pour la création (POST)</li>
 *     <li>{@link #updateRole(UpdateRoleRequest, Role)} → pour la mise à jour complète (PUT)</li>
 *     <li>{@link #patchRole(PatchRoleRequest, Role)} → pour la mise à jour partielle (PATCH)</li>
 *     <li>{@link #toResponse(Role)} → pour la réponse (GET, POST, PUT, PATCH)</li>
 * </ul>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface RoleMapper {

    /**
     * Convertit un {@link CreateRoleRequest} en une entité {@link Role}.
     *
     * <p>
     * Les champs d'audit ({@code id}, {@code createdAt}, {@code createdBy},
     * {@code updatedAt}, {@code updatedBy}, {@code deleted}) ne sont pas
     * présents dans la requête ; ils seront gérés automatiquement par JPA
     * et l'auditing Spring Data.
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link Role} non persistée
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toRole(CreateRoleRequest request);

    /**
     * Met à jour une entité {@link Role} existante avec les données
     * d'un {@link UpdateRoleRequest} (PUT).
     *
     * <p>
     * Tous les champs de la requête sont écrasés dans l'entité.
     * </p>
     *
     * @param request DTO de mise à jour complète
     * @param role    entité à mettre à jour (cible)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "users", ignore = true)
    void updateRole(UpdateRoleRequest request, @MappingTarget Role role);

    /**
     * Met à jour partiellement une entité {@link Role} avec les données
     * d'un {@link PatchRoleRequest} (PATCH).
     *
     * <p>
     * Seuls les champs non-nuls de la requête sont copiés dans l'entité.
     * </p>
     *
     * @param request DTO de mise à jour partielle
     * @param role    entité à mettre à jour (cible)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "users", ignore = true)
    void patchRole(PatchRoleRequest request, @MappingTarget Role role);

    /**
     * Convertit une entité {@link Role} en un DTO {@link RoleResponse}.
     *
     * <p>
     * Tous les champs de l'entité sont exposés dans la réponse :
     * les données métier (id, name, description) ainsi que les champs
     * d'audit (createdAt, createdBy, updatedAt, updatedBy, deleted).
     * </p>
     *
     * @param role entité à convertir
     * @return DTO de réponse complet
     */
    RoleResponse toResponse(Role role);
}