package lu.police.pcms.audit.mapper;

import lu.police.pcms.audit.dto.AuditLogResponse;
import lu.police.pcms.audit.dto.CreateAuditLogRequest;
import lu.police.pcms.audit.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct pour la conversion entre l'entité {@link AuditLog} et ses DTO.
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
 *     <li>{@link #toEntity(CreateAuditLogRequest)} → pour la création (POST)</li>
 *     <li>{@link #toResponse(AuditLog)} → pour la réponse (GET, POST)</li>
 * </ul>
 *
 * <p>
 * ⚠️ Ce module ne possède pas de DTO de mise à jour (PUT/PATCH)
 * car un journal d'audit est immuable par conception.
 * </p>
 *
 * <p>
 * La relation {@code user} n'est pas mappée directement depuis la requête,
 * car elle utilise un ID. Le service se chargera de charger l'entité
 * {@code User} correspondante avant de l'affecter au log.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        uses = {}
)
public interface AuditLogMapper {

    /**
     * Convertit un {@link CreateAuditLogRequest} en une entité {@link AuditLog}.
     *
     * <p>
     * La relation {@code user} est ignorée car le service doit la charger
     * à partir de l'ID {@code userId}.
     * Les champs {@code id}, {@code createdAt} et {@code createdBy} sont
     * ignorés car ils sont gérés automatiquement par JPA et l'auditing.
     * </p>
     *
     * @param request DTO de création
     * @return entité {@link AuditLog} non persistée (sans relation)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)          // Le service chargera l'entité User
    @Mapping(target = "createdAt", ignore = true)     // Géré par JPA
    @Mapping(target = "createdBy", ignore = true)     // Géré par JPA
    AuditLog toEntity(CreateAuditLogRequest request);

    /**
     * Convertit une entité {@link AuditLog} en un DTO {@link AuditLogResponse}.
     *
     * <p>
     * L'identifiant de l'utilisateur est extrait de la relation {@code user}.
     * Tous les champs d'audit sont exposés dans la réponse.
     * </p>
     *
     * @param entity entité à convertir
     * @return DTO de réponse complet
     */
    @Mapping(source = "user.id", target = "userId")
    AuditLogResponse toResponse(AuditLog entity);
}