package lu.police.pcms.audit.mapper;

import lu.police.pcms.audit.dto.AuditLogResponse;
import lu.police.pcms.audit.dto.CreateAuditLogRequest;
import lu.police.pcms.audit.entity.AuditLog;
import lu.police.pcms.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper {@link AuditLogMapper}.
 *
 * <p>
 * Ce mapper ne gère que la création et la réponse (pas de mise à jour).
 * </p>
 */
class AuditLogMapperTest {

    private final AuditLogMapper mapper = Mappers.getMapper(AuditLogMapper.class);

    @Test
    @DisplayName("Conversion CreateAuditLogRequest → AuditLog")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        CreateAuditLogRequest request = new CreateAuditLogRequest(
                10L,
                "CREATE",
                "ROLE",
                1L,
                "Création du rôle ADMIN",
                "192.168.1.1"
        );

        // Act
        AuditLog entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getAction()).isEqualTo("CREATE");
        assertThat(entity.getEntityName()).isEqualTo("ROLE");
        assertThat(entity.getEntityId()).isEqualTo(1L);
        assertThat(entity.getDetails()).isEqualTo("Création du rôle ADMIN");
        assertThat(entity.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(entity.getId()).isNull();          // ignoré
        assertThat(entity.getUser()).isNull();        // ignoré
        assertThat(entity.getCreatedAt()).isNull();   // ignoré
        assertThat(entity.getCreatedBy()).isNull();   // ignoré
    }

    @Test
    @DisplayName("Conversion AuditLog → AuditLogResponse")
    void shouldMapEntityToResponse() {
        // Arrange
        User user = new User();
        user.setId(10L);

        AuditLog entity = new AuditLog();
        entity.setId(1L);
        entity.setUser(user);
        entity.setAction("CREATE");
        entity.setEntityName("ROLE");
        entity.setEntityId(1L);
        entity.setDetails("Création du rôle ADMIN");
        entity.setIpAddress("192.168.1.1");

        // Act
        AuditLogResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getAction()).isEqualTo("CREATE");
        assertThat(response.getEntityName()).isEqualTo("ROLE");
        assertThat(response.getEntityId()).isEqualTo(1L);
        assertThat(response.getDetails()).isEqualTo("Création du rôle ADMIN");
        assertThat(response.getIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("Conversion avec des champs optionnels à null")
    void shouldMapWithNullableFields() {
        // Arrange
        CreateAuditLogRequest request = new CreateAuditLogRequest(
                10L,
                "LOGIN",
                "USER",
                5L,
                null,      // details absent
                null       // ipAddress absent
        );

        // Act
        AuditLog entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getAction()).isEqualTo("LOGIN");
        assertThat(entity.getEntityName()).isEqualTo("USER");
        assertThat(entity.getEntityId()).isEqualTo(5L);
        assertThat(entity.getDetails()).isNull();
        assertThat(entity.getIpAddress()).isNull();
    }
}