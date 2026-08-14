package lu.police.pcms.department.mapper;

import lu.police.pcms.department.dto.CreateDepartmentRequest;
import lu.police.pcms.department.dto.DepartmentResponse;
import lu.police.pcms.department.dto.PatchDepartmentRequest;
import lu.police.pcms.department.dto.UpdateDepartmentRequest;
import lu.police.pcms.department.entity.Department;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link Department} et ses DTO.
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
 *     <li>{@link #toEntity(CreateDepartmentRequest)} → pour la création (POST)</li>
 *     <li>{@link #updateEntity(UpdateDepartmentRequest, Department)} → pour la mise à jour complète (PUT)</li>
 *     <li>{@link #patchEntity(PatchDepartmentRequest, Department)} → pour la mise à jour partielle (PATCH)</li>
 *     <li>{@link #toResponse(Department)} → pour la réponse (GET, POST, PUT, PATCH)</li>
 * </ul>
 *
 * <p>
 * La relation {@code users} est ignorée dans les DTO pour éviter
 * les cycles et les surcharges de données.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface DepartmentMapper {

    /**
     * Convertit un {@link CreateDepartmentRequest} en une entité {@link Department}.
     *
     * <p>
     * Les champs d'audit (id, createdAt, createdBy, updatedAt, updatedBy, deleted)
     * et la relation {@code users} sont ignorés, car ils sont gérés
     * automatiquement par JPA et l'auditing Spring Data.
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link Department} non persistée
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "users", ignore = true)
    Department toEntity(CreateDepartmentRequest request);

    /**
     * Met à jour une entité {@link Department} existante avec les données
     * d'un {@link UpdateDepartmentRequest} (PUT).
     *
     * <p>
     * Tous les champs de la requête sont écrasés dans l'entité.
     * L'identifiant, l'audit et la relation {@code users} sont ignorés.
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
    @Mapping(target = "users", ignore = true)
    void updateEntity(UpdateDepartmentRequest request, @MappingTarget Department entity);

    /**
     * Met à jour partiellement une entité {@link Department} avec les données
     * d'un {@link PatchDepartmentRequest} (PATCH).
     *
     * <p>
     * Seuls les champs non-nuls de la requête sont copiés dans l'entité.
     * Les champs audit et la relation {@code users} sont ignorés.
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
    @Mapping(target = "users", ignore = true)
    void patchEntity(PatchDepartmentRequest request, @MappingTarget Department entity);

    /**
     * Convertit une entité {@link Department} en un DTO {@link DepartmentResponse}.
     *
     * <p>
     * Tous les champs de l'entité sont exposés dans la réponse :
     * les données métier (id, code, name) ainsi que les champs d'audit
     * (createdAt, createdBy, updatedAt, updatedBy, deleted).
     * </p>
     *
     * @param entity entité à convertir
     * @return DTO de réponse complet
     */
    DepartmentResponse toResponse(Department entity);
}