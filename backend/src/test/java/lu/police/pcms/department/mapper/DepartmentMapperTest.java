package lu.police.pcms.department.mapper;

import lu.police.pcms.department.dto.CreateDepartmentRequest;
import lu.police.pcms.department.dto.DepartmentResponse;
import lu.police.pcms.department.dto.PatchDepartmentRequest;
import lu.police.pcms.department.dto.UpdateDepartmentRequest;
import lu.police.pcms.department.entity.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper {@link DepartmentMapper}.
 *
 * <p>
 * Ces tests vérifient que la conversion entre les DTO et l'entité
 * fonctionne correctement, y compris pour les cas de mise à jour
 * partielle avec {@code null}.
 * </p>
 */
class DepartmentMapperTest {

    private final DepartmentMapper mapper = Mappers.getMapper(DepartmentMapper.class);

    @Test
    @DisplayName("Conversion CreateDepartmentRequest → Department")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        CreateDepartmentRequest request = new CreateDepartmentRequest("IT", "Informatique");

        // Act
        Department entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getCode()).isEqualTo("IT");
        assertThat(entity.getName()).isEqualTo("Informatique");
        assertThat(entity.getId()).isNull();        // ignoré
        assertThat(entity.getCreatedAt()).isNull(); // ignoré
        assertThat(entity.getUsers()).isNull();     // ignoré
    }

    @Test
    @DisplayName("Conversion Department → DepartmentResponse")
    void shouldMapEntityToResponse() {
        // Arrange
        Department entity = new Department();
        entity.setId(1L);
        entity.setCode("HR");
        entity.setName("Ressources Humaines");

        // Act
        DepartmentResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("HR");
        assertThat(response.getName()).isEqualTo("Ressources Humaines");
    }

    @Test
    @DisplayName("Mise à jour complète UpdateDepartmentRequest → Department (PUT)")
    void shouldUpdateEntity() {
        // Arrange
        Department entity = new Department();
        entity.setId(1L);
        entity.setCode("OLD");
        entity.setName("Ancien nom");

        UpdateDepartmentRequest request = new UpdateDepartmentRequest("NEW", "Nouveau nom");

        // Act
        mapper.updateEntity(request, entity);

        // Assert
        assertThat(entity.getCode()).isEqualTo("NEW");
        assertThat(entity.getName()).isEqualTo("Nouveau nom");
        assertThat(entity.getId()).isEqualTo(1L); // inchangé
    }

    @Test
    @DisplayName("Mise à jour partielle PatchDepartmentRequest → Department (PATCH)")
    void shouldPatchEntity() {
        // Arrange
        Department entity = new Department();
        entity.setId(1L);
        entity.setCode("EXISTING");
        entity.setName("Nom existant");

        PatchDepartmentRequest request = new PatchDepartmentRequest("UPDATED", null); // seul le code change

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getCode()).isEqualTo("UPDATED");
        assertThat(entity.getName()).isEqualTo("Nom existant"); // inchangé
        assertThat(entity.getId()).isEqualTo(1L); // inchangé
    }

    @Test
    @DisplayName("Patch avec tous les champs à null ne modifie rien")
    void shouldPatchWithNullsDoNothing() {
        // Arrange
        Department entity = new Department();
        entity.setId(1L);
        entity.setCode("ORIGINAL");
        entity.setName("Original");

        PatchDepartmentRequest request = new PatchDepartmentRequest(null, null);

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getCode()).isEqualTo("ORIGINAL");
        assertThat(entity.getName()).isEqualTo("Original");
    }
}