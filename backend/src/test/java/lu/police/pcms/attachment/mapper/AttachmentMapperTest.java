package lu.police.pcms.attachment.mapper;

import lu.police.pcms.attachment.dto.AttachmentResponse;
import lu.police.pcms.attachment.dto.CreateAttachmentRequest;
import lu.police.pcms.attachment.dto.PatchAttachmentRequest;
import lu.police.pcms.attachment.dto.UpdateAttachmentRequest;
import lu.police.pcms.attachment.entity.Attachment;
import lu.police.pcms.casefile.entity.CaseFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper {@link AttachmentMapper}.
 *
 * <p>
 * Ces tests vérifient que la conversion entre les DTO et l'entité
 * fonctionne correctement, y compris pour les cas de mise à jour
 * partielle avec {@code null}.
 * </p>
 */
class AttachmentMapperTest {

    private final AttachmentMapper mapper = Mappers.getMapper(AttachmentMapper.class);

    @Test
    @DisplayName("Conversion CreateAttachmentRequest → Attachment")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        CreateAttachmentRequest request = new CreateAttachmentRequest(
                10L,
                "photo.jpg",
                "image/jpeg",
                2048L,
                "PHOTO"
        );

        // Act
        Attachment entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getOriginalFilename()).isEqualTo("photo.jpg");
        assertThat(entity.getMimeType()).isEqualTo("image/jpeg");
        assertThat(entity.getFileSize()).isEqualTo(2048L);
        assertThat(entity.getType()).isEqualTo("PHOTO");
        assertThat(entity.getId()).isNull();          // ignoré
        assertThat(entity.getCaseFile()).isNull();    // ignoré
        assertThat(entity.getFilename()).isNull();    // généré par le service
        assertThat(entity.getStoragePath()).isNull(); // généré par le service
        assertThat(entity.getUploadedAt()).isNull();  // généré par le service
        assertThat(entity.getCreatedAt()).isNull();   // ignoré
    }

    @Test
    @DisplayName("Conversion Attachment → AttachmentResponse")
    void shouldMapEntityToResponse() {
        // Arrange
        CaseFile caseFile = new CaseFile();
        caseFile.setId(10L);

        Instant now = Instant.now();

        Attachment entity = new Attachment();
        entity.setId(1L);
        entity.setCaseFile(caseFile);
        entity.setFilename("uuid-photo.jpg");
        entity.setOriginalFilename("photo.jpg");
        entity.setMimeType("image/jpeg");
        entity.setFileSize(2048L);
        entity.setStoragePath("/uploads/uuid-photo.jpg");
        entity.setType("PHOTO");
        entity.setUploadedAt(now);

        // Act
        AttachmentResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCaseFileId()).isEqualTo(10L);
        assertThat(response.getFilename()).isEqualTo("uuid-photo.jpg");
        assertThat(response.getOriginalFilename()).isEqualTo("photo.jpg");
        assertThat(response.getMimeType()).isEqualTo("image/jpeg");
        assertThat(response.getFileSize()).isEqualTo(2048L);
        assertThat(response.getStoragePath()).isEqualTo("/uploads/uuid-photo.jpg");
        assertThat(response.getType()).isEqualTo("PHOTO");
        assertThat(response.getUploadedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Mise à jour complète UpdateAttachmentRequest → Attachment (PUT)")
    void shouldUpdateEntity() {
        // Arrange
        Attachment entity = new Attachment();
        entity.setId(1L);
        entity.setMimeType("old/type");
        entity.setType("OLD");

        UpdateAttachmentRequest request = new UpdateAttachmentRequest("image/png", "PHOTO");

        // Act
        mapper.updateEntity(request, entity);

        // Assert
        assertThat(entity.getMimeType()).isEqualTo("image/png");
        assertThat(entity.getType()).isEqualTo("PHOTO");
        assertThat(entity.getId()).isEqualTo(1L); // inchangé
        // Les autres champs doivent rester inchangés (ici ils sont nuls car non initialisés)
    }

    @Test
    @DisplayName("Mise à jour partielle PatchAttachmentRequest → Attachment (PATCH)")
    void shouldPatchEntity() {
        // Arrange
        Attachment entity = new Attachment();
        entity.setId(1L);
        entity.setMimeType("image/jpeg");
        entity.setType("PHOTO");

        PatchAttachmentRequest request = new PatchAttachmentRequest(
                "image/png",    // mimeType modifié
                null            // type inchangé
        );

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getMimeType()).isEqualTo("image/png");
        assertThat(entity.getType()).isEqualTo("PHOTO"); // inchangé
        assertThat(entity.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Patch avec tous les champs à null ne modifie rien")
    void shouldPatchWithNullsDoNothing() {
        // Arrange
        Attachment entity = new Attachment();
        entity.setId(1L);
        entity.setMimeType("image/jpeg");
        entity.setType("PHOTO");

        PatchAttachmentRequest request = new PatchAttachmentRequest(null, null);

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getMimeType()).isEqualTo("image/jpeg");
        assertThat(entity.getType()).isEqualTo("PHOTO");
        assertThat(entity.getId()).isEqualTo(1L);
    }
}