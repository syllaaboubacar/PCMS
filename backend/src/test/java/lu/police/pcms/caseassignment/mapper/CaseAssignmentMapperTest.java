package lu.police.pcms.caseassignment.mapper;

import lu.police.pcms.caseassignment.dto.CaseAssignmentResponse;
import lu.police.pcms.caseassignment.dto.CreateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.PatchCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.UpdateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.entity.CaseAssignment;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper {@link CaseAssignmentMapper}.
 *
 * <p>
 * Ces tests vérifient que la conversion entre les DTO et l'entité
 * fonctionne correctement, y compris pour les cas de mise à jour
 * partielle avec {@code null}.
 * </p>
 */
class CaseAssignmentMapperTest {

    private final CaseAssignmentMapper mapper = Mappers.getMapper(CaseAssignmentMapper.class);

    @Test
    @DisplayName("Conversion CreateCaseAssignmentRequest → CaseAssignment")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        Instant now = Instant.now();
        CreateCaseAssignmentRequest request = new CreateCaseAssignmentRequest(1L, 2L, now);

        // Act
        CaseAssignment entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getAssignedAt()).isEqualTo(now);
        assertThat(entity.getId()).isNull();          // ignoré
        assertThat(entity.getCaseFile()).isNull();    // ignoré
        assertThat(entity.getUser()).isNull();        // ignoré
        assertThat(entity.getActive()).isTrue();      // valeur par défaut conservée (true)
        assertThat(entity.getCreatedAt()).isNull();   // ignoré
    }

    @Test
    @DisplayName("Conversion CaseAssignment → CaseAssignmentResponse")
    void shouldMapEntityToResponse() {
        // Arrange
        CaseFile caseFile = new CaseFile();
        caseFile.setId(10L);

        User user = new User();
        user.setId(20L);

        Instant now = Instant.now();

        CaseAssignment entity = new CaseAssignment();
        entity.setId(1L);
        entity.setCaseFile(caseFile);
        entity.setUser(user);
        entity.setAssignedAt(now);
        entity.setActive(true);

        // Act
        CaseAssignmentResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCaseFileId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(20L);
        assertThat(response.getAssignedAt()).isEqualTo(now);
        assertThat(response.getActive()).isTrue();
    }

    @Test
    @DisplayName("Mise à jour complète UpdateCaseAssignmentRequest → CaseAssignment (PUT)")
    void shouldUpdateEntity() {
        // Arrange
        CaseAssignment entity = new CaseAssignment();
        entity.setId(1L);
        entity.setActive(false);
        entity.setAssignedAt(Instant.now().minus(5, ChronoUnit.DAYS));

        UpdateCaseAssignmentRequest request = new UpdateCaseAssignmentRequest(true);

        // Act
        mapper.updateEntity(request, entity);

        // Assert
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getAssignedAt()).isNotNull(); // inchangé
        assertThat(entity.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Mise à jour partielle PatchCaseAssignmentRequest → CaseAssignment (PATCH)")
    void shouldPatchEntity() {
        // Arrange
        CaseAssignment entity = new CaseAssignment();
        entity.setId(1L);
        entity.setActive(true);

        PatchCaseAssignmentRequest request = new PatchCaseAssignmentRequest(false);

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getActive()).isFalse();
        assertThat(entity.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Patch avec active = null ne modifie pas l'entité")
    void shouldPatchWithNullActiveDoNothing() {
        // Arrange
        CaseAssignment entity = new CaseAssignment();
        entity.setId(1L);
        entity.setActive(true);

        PatchCaseAssignmentRequest request = new PatchCaseAssignmentRequest(null);

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getActive()).isTrue(); // inchangé
        assertThat(entity.getId()).isEqualTo(1L);
    }
}