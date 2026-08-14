package lu.police.pcms.attachment.service;

import lu.police.pcms.attachment.dto.AttachmentResponse;
import lu.police.pcms.attachment.dto.CreateAttachmentRequest;
import lu.police.pcms.attachment.dto.PatchAttachmentRequest;
import lu.police.pcms.attachment.dto.UpdateAttachmentRequest;
import lu.police.pcms.attachment.entity.Attachment;
import lu.police.pcms.attachment.mapper.AttachmentMapper;
import lu.police.pcms.attachment.repository.AttachmentRepository;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service AttachmentService")
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private CaseFileRepository caseFileRepository;

    @Mock
    private AttachmentMapper attachmentMapper;

    @InjectMocks
    private AttachmentService attachmentService;

    // ========== DONNÉES DE TEST ==========

    private static final Long ATTACHMENT_ID = 1L;
    private static final Long CASE_FILE_ID = 10L;
    private static final String ORIGINAL_FILENAME = "photo.jpg";
    private static final String MIME_TYPE = "image/jpeg";
    private static final String TYPE = "PHOTO";

    private CaseFile mockCaseFile() {
        CaseFile cf = new CaseFile();
        cf.setId(CASE_FILE_ID);
        cf.setCaseNumber("PCMS_CASE_001");
        return cf;
    }

    private Attachment mockAttachment(boolean deleted) {
        Attachment attachment = new Attachment();
        attachment.setId(ATTACHMENT_ID);
        attachment.setCaseFile(mockCaseFile());
        attachment.setFilename("uuid-photo.jpg");
        attachment.setOriginalFilename(ORIGINAL_FILENAME);
        attachment.setMimeType(MIME_TYPE);
        attachment.setFileSize(2048L);
        attachment.setStoragePath("/uploads/uuid-photo.jpg");
        attachment.setType(TYPE);
        attachment.setUploadedAt(Instant.now());
        attachment.setDeleted(deleted);
        return attachment;
    }

    private AttachmentResponse mockResponse(Attachment attachment) {
        AttachmentResponse response = new AttachmentResponse();
        response.setId(attachment.getId());
        response.setCaseFileId(attachment.getCaseFile().getId());
        response.setFilename(attachment.getFilename());
        response.setOriginalFilename(attachment.getOriginalFilename());
        response.setMimeType(attachment.getMimeType());
        response.setFileSize(attachment.getFileSize());
        response.setStoragePath(attachment.getStoragePath());
        response.setType(attachment.getType());
        response.setUploadedAt(attachment.getUploadedAt());
        // Les champs d'audit ne sont pas inclus dans la réponse mockée pour simplifier
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'une pièce jointe avec succès")
    void shouldCreateAttachmentSuccessfully() {
        // Arrange
        CreateAttachmentRequest request = new CreateAttachmentRequest(
                CASE_FILE_ID, ORIGINAL_FILENAME, MIME_TYPE, 2048L, TYPE
        );
        CaseFile caseFile = mockCaseFile();
        Attachment entity = new Attachment();
        Attachment saved = mockAttachment(false);
        AttachmentResponse expected = mockResponse(saved);

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(attachmentRepository.existsByFilename(any(String.class))).thenReturn(false);
        when(attachmentMapper.toEntity(request)).thenReturn(entity);
        when(attachmentRepository.save(entity)).thenReturn(saved);
        when(attachmentMapper.toResponse(saved)).thenReturn(expected);

        // Act
        AttachmentResponse actual = attachmentService.createAttachment(request);

        // Assert
        assertThat(actual).isEqualTo(expected);
        // Vérification du filename généré : il doit se terminer par "_photo.jpg" et contenir un UUID
        assertThat(entity.getFilename()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_photo.jpg$");
        assertThat(entity.getStoragePath()).startsWith("/uploads/");
        assertThat(entity.getUploadedAt()).isNotNull();
        verify(attachmentRepository).save(entity);
    }

    @Test
    @DisplayName("Création avec dossier inexistant → exception")
    void shouldThrowExceptionWhenCaseFileNotFound() {
        CreateAttachmentRequest request = new CreateAttachmentRequest(
                CASE_FILE_ID, ORIGINAL_FILENAME, MIME_TYPE, 2048L, TYPE
        );
        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.createAttachment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dossier");
    }

    @Test
    @DisplayName("Création avec nom de fichier interne déjà existant → exception")
    void shouldThrowExceptionWhenFilenameExists() {
        CreateAttachmentRequest request = new CreateAttachmentRequest(
                CASE_FILE_ID, ORIGINAL_FILENAME, MIME_TYPE, 2048L, TYPE
        );
        CaseFile caseFile = mockCaseFile();
        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(attachmentRepository.existsByFilename(any(String.class))).thenReturn(true);

        assertThatThrownBy(() -> attachmentService.createAttachment(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("filename");
    }

    @Test
    @DisplayName("Récupération d'une pièce jointe par ID avec succès")
    void shouldGetAttachmentByIdSuccessfully() {
        Attachment attachment = mockAttachment(false);
        AttachmentResponse expected = mockResponse(attachment);
        when(attachmentRepository.findById(ATTACHMENT_ID)).thenReturn(Optional.of(attachment));
        when(attachmentMapper.toResponse(attachment)).thenReturn(expected);

        AttachmentResponse actual = attachmentService.getAttachmentById(ATTACHMENT_ID);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'une pièce jointe supprimée → exception")
    void shouldThrowExceptionWhenAttachmentDeleted() {
        Attachment attachment = mockAttachment(true);
        when(attachmentRepository.findById(ATTACHMENT_ID)).thenReturn(Optional.of(attachment));

        assertThatThrownBy(() -> attachmentService.getAttachmentById(ATTACHMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération de toutes les pièces jointes actives")
    void shouldGetAllAttachments() {
        Attachment a1 = mockAttachment(false);
        Attachment a2 = mockAttachment(false);
        a2.setId(2L);
        List<Attachment> list = List.of(a1, a2);

        when(attachmentRepository.findByDeletedFalse()).thenReturn(list);
        when(attachmentMapper.toResponse(any(Attachment.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<AttachmentResponse> responses = attachmentService.getAllAttachments();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(AttachmentResponse::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Récupération des pièces jointes d'un dossier")
    void shouldGetAttachmentsByCaseFile() {
        CaseFile caseFile = mockCaseFile();
        Attachment a1 = mockAttachment(false);
        Attachment a2 = mockAttachment(false);
        a2.setId(2L);
        List<Attachment> list = List.of(a1, a2);

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(attachmentRepository.findByCaseFile(caseFile)).thenReturn(list);
        when(attachmentMapper.toResponse(any(Attachment.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<AttachmentResponse> responses = attachmentService.getAttachmentsByCaseFile(CASE_FILE_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(AttachmentResponse::getCaseFileId)
                .containsOnly(CASE_FILE_ID);
    }

    @Test
    @DisplayName("Mise à jour complète (PUT) avec succès")
    void shouldUpdateAttachmentSuccessfully() {
        // Arrange
        Attachment existing = mockAttachment(false);
        UpdateAttachmentRequest request = new UpdateAttachmentRequest("image/png", "ICON");

        when(attachmentRepository.findById(ATTACHMENT_ID)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            Attachment a = inv.getArgument(1);
            a.setMimeType("image/png");
            a.setType("ICON");
            return null;
        }).when(attachmentMapper).updateEntity(request, existing);

        when(attachmentRepository.save(existing)).thenReturn(existing);
        when(attachmentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        AttachmentResponse actual = attachmentService.updateAttachment(ATTACHMENT_ID, request);

        // Assert
        assertThat(actual.getMimeType()).isEqualTo("image/png");
        assertThat(actual.getType()).isEqualTo("ICON");
        verify(attachmentRepository).save(existing);
    }

    @Test
    @DisplayName("Mise à jour partielle (PATCH) avec succès")
    void shouldPatchAttachmentSuccessfully() {
        // Arrange
        Attachment existing = mockAttachment(false);
        PatchAttachmentRequest request = new PatchAttachmentRequest("video/mp4", null); // seul mimeType change

        when(attachmentRepository.findById(ATTACHMENT_ID)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            Attachment a = inv.getArgument(1);
            a.setMimeType("video/mp4");
            return null;
        }).when(attachmentMapper).patchEntity(request, existing);

        when(attachmentRepository.save(existing)).thenReturn(existing);
        when(attachmentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        AttachmentResponse actual = attachmentService.patchAttachment(ATTACHMENT_ID, request);

        // Assert
        assertThat(actual.getMimeType()).isEqualTo("video/mp4");
        assertThat(actual.getType()).isEqualTo(TYPE); // inchangé
        verify(attachmentRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression logique avec succès")
    void shouldDeleteAttachmentSuccessfully() {
        Attachment existing = mockAttachment(false);
        when(attachmentRepository.findById(ATTACHMENT_ID)).thenReturn(Optional.of(existing));

        attachmentService.deleteAttachment(ATTACHMENT_ID);

        assertThat(existing.getDeleted()).isTrue();
        verify(attachmentRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression d'une pièce jointe déjà supprimée ne fait rien")
    void shouldDoNothingWhenDeletingAlreadyDeleted() {
        Attachment existing = mockAttachment(true);
        when(attachmentRepository.findById(ATTACHMENT_ID)).thenReturn(Optional.of(existing));

        attachmentService.deleteAttachment(ATTACHMENT_ID);

        verify(attachmentRepository, never()).save(existing);
    }
}