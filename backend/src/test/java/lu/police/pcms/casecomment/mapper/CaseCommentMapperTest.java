package lu.police.pcms.casecomment.mapper;

import lu.police.pcms.casecomment.dto.CaseCommentResponse;
import lu.police.pcms.casecomment.dto.CreateCaseCommentRequest;
import lu.police.pcms.casecomment.dto.PatchCaseCommentRequest;
import lu.police.pcms.casecomment.dto.UpdateCaseCommentRequest;
import lu.police.pcms.casecomment.entity.CaseComment;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper {@link CaseCommentMapper}.
 *
 * <p>
 * Ces tests vérifient que la conversion entre les DTO et l'entité
 * fonctionne correctement, y compris pour les cas de mise à jour
 * partielle avec {@code null}.
 * </p>
 */
class CaseCommentMapperTest {

    private final CaseCommentMapper mapper = Mappers.getMapper(CaseCommentMapper.class);

    @Test
    @DisplayName("Conversion CreateCaseCommentRequest → CaseComment")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        CreateCaseCommentRequest request = new CreateCaseCommentRequest(10L, 20L, "Ceci est un commentaire");

        // Act
        CaseComment entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getContent()).isEqualTo("Ceci est un commentaire");
        assertThat(entity.getId()).isNull();          // ignoré
        assertThat(entity.getCaseFile()).isNull();    // ignoré
        assertThat(entity.getUser()).isNull();        // ignoré
        assertThat(entity.getCreatedAt()).isNull();   // ignoré
    }

    @Test
    @DisplayName("Conversion CaseComment → CaseCommentResponse")
    void shouldMapEntityToResponse() {
        // Arrange
        CaseFile caseFile = new CaseFile();
        caseFile.setId(10L);

        User user = new User();
        user.setId(20L);

        CaseComment entity = new CaseComment();
        entity.setId(1L);
        entity.setCaseFile(caseFile);
        entity.setUser(user);
        entity.setContent("Commentaire de test");

        // Act
        CaseCommentResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCaseFileId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(20L);
        assertThat(response.getContent()).isEqualTo("Commentaire de test");
    }

    @Test
    @DisplayName("Mise à jour complète UpdateCaseCommentRequest → CaseComment (PUT)")
    void shouldUpdateEntity() {
        // Arrange
        CaseComment entity = new CaseComment();
        entity.setId(1L);
        entity.setContent("Ancien contenu");

        UpdateCaseCommentRequest request = new UpdateCaseCommentRequest("Nouveau contenu");

        // Act
        mapper.updateEntity(request, entity);

        // Assert
        assertThat(entity.getContent()).isEqualTo("Nouveau contenu");
        assertThat(entity.getId()).isEqualTo(1L); // inchangé
    }

    @Test
    @DisplayName("Mise à jour partielle PatchCaseCommentRequest → CaseComment (PATCH)")
    void shouldPatchEntity() {
        // Arrange
        CaseComment entity = new CaseComment();
        entity.setId(1L);
        entity.setContent("Contenu original");

        PatchCaseCommentRequest request = new PatchCaseCommentRequest("Contenu modifié");

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getContent()).isEqualTo("Contenu modifié");
        assertThat(entity.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Patch avec content = null ne modifie pas l'entité")
    void shouldPatchWithNullContentDoNothing() {
        // Arrange
        CaseComment entity = new CaseComment();
        entity.setId(1L);
        entity.setContent("Contenu original");

        PatchCaseCommentRequest request = new PatchCaseCommentRequest(null);

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getContent()).isEqualTo("Contenu original");
        assertThat(entity.getId()).isEqualTo(1L);
    }
}