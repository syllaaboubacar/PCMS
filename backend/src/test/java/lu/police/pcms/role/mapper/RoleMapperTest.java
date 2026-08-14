package lu.police.pcms.role.mapper;

import lu.police.pcms.role.dto.CreateRoleRequest;
import lu.police.pcms.role.dto.PatchRoleRequest;
import lu.police.pcms.role.dto.RoleResponse;
import lu.police.pcms.role.dto.UpdateRoleRequest;
import lu.police.pcms.role.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper {@link RoleMapper}.
 *
 * <p>
 * Ces tests vérifient que la conversion entre les DTO et l'entité
 * fonctionne correctement, y compris pour les cas de mise à jour
 * partielle avec {@code null}.
 * </p>
 */
class RoleMapperTest {

    private final RoleMapper mapper = Mappers.getMapper(RoleMapper.class);

    @Test
    @DisplayName("Conversion CreateRoleRequest → Role")
    void shouldMapCreateRequestToRole() {
        // Arrange
        CreateRoleRequest request = new CreateRoleRequest("ROLE_TEST", "Rôle de test");

        // Act
        Role role = mapper.toRole(request);

        // Assert
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo("ROLE_TEST");
        assertThat(role.getDescription()).isEqualTo("Rôle de test");
        assertThat(role.getId()).isNull();        // ignoré
        assertThat(role.getCreatedAt()).isNull(); // ignoré
        assertThat(role.getUsers()).isNull();     // ignoré
    }

    @Test
    @DisplayName("Conversion Role → RoleResponse")
    void shouldMapRoleToResponse() {
        // Arrange
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_ADMIN");
        role.setDescription("Administrateur");

        // Act
        RoleResponse response = mapper.toResponse(role);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getDescription()).isEqualTo("Administrateur");
    }

    @Test
    @DisplayName("Mise à jour complète UpdateRoleRequest → Role (PUT)")
    void shouldUpdateRole() {
        // Arrange
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_OLD");
        role.setDescription("Ancienne description");

        UpdateRoleRequest request = new UpdateRoleRequest("ROLE_NEW", "Nouvelle description");

        // Act
        mapper.updateRole(request, role);

        // Assert
        assertThat(role.getName()).isEqualTo("ROLE_NEW");
        assertThat(role.getDescription()).isEqualTo("Nouvelle description");
        assertThat(role.getId()).isEqualTo(1L);  // inchangé
    }

    @Test
    @DisplayName("Mise à jour partielle PatchRoleRequest → Role (PATCH)")
    void shouldPatchRole() {
        // Arrange
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_EXISTING");
        role.setDescription("Description existante");

        PatchRoleRequest request = new PatchRoleRequest("ROLE_UPDATED", null); // seul le nom change

        // Act
        mapper.patchRole(request, role);

        // Assert
        assertThat(role.getName()).isEqualTo("ROLE_UPDATED");
        assertThat(role.getDescription()).isEqualTo("Description existante"); // inchangé
        assertThat(role.getId()).isEqualTo(1L); // inchangé
    }

    @Test
    @DisplayName("Patch avec tous les champs à null ne modifie rien")
    void shouldPatchWithNullsDoNothing() {
        // Arrange
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_ORIGINAL");
        role.setDescription("Original");

        PatchRoleRequest request = new PatchRoleRequest(null, null);

        // Act
        mapper.patchRole(request, role);

        // Assert
        assertThat(role.getName()).isEqualTo("ROLE_ORIGINAL");
        assertThat(role.getDescription()).isEqualTo("Original");
    }
}