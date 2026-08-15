package lu.police.pcms.attachment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lu.police.pcms.attachment.dto.AttachmentResponse;
import lu.police.pcms.attachment.dto.CreateAttachmentRequest;
import lu.police.pcms.attachment.dto.PatchAttachmentRequest;
import lu.police.pcms.attachment.dto.UpdateAttachmentRequest;
import lu.police.pcms.attachment.service.AttachmentService;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Tests d'intégration du contrôleur AttachmentController.
 *
 * <p>
 * Ce test vérifie les 7 endpoints REST de gestion des pièces jointes
 * en utilisant MockMvc avec un contexte Spring Boot complet.
 * Le service est mocké pour isoler la couche contrôleur.
 * </p>
 *
 * <p>
 * Spécificités du module Attachment :
 * </p>
 * <ul>
 *     <li>Les champs {@code filename} et {@code storagePath} sont générés
 *         par le service et immuables.</li>
 *     <li>Seuls {@code mimeType} et {@code type} sont modifiables
 *         après création.</li>
 *     <li>Pas de contrainte d'unicité sur les pièces jointes.</li>
 *     <li>Le DTO de création contient des métadonnées de fichier
 *         mais pas le fichier physique lui-même.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(AttachmentControllerTest.TestConfig.class)
@DisplayName("Tests du contrôleur AttachmentController")
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper local avec JavaTimeModule pour sérialiser
     * correctement les Instant (uploadedAt, createdAt, updatedAt).
     */
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttachmentService attachmentService;


    // ============================================================
    // CONSTANTES DE TEST
    // ============================================================

    private static final Long ATTACHMENT_ID = 1L;
    private static final Long CASE_FILE_ID = 10L;
    private static final String ORIGINAL_FILENAME = "photo_scene.jpg";
    private static final String MIME_TYPE = "image/jpeg";
    private static final Long FILE_SIZE = 2048000L;
    private static final String TYPE = "PHOTO";
    private static final String GENERATED_FILENAME = "20260815_photo_scene.jpg";
    private static final String STORAGE_PATH = "/uploads/cases/10/20260815_photo_scene.jpg";

    private static final String UPDATED_MIME_TYPE = "image/png";
    private static final String UPDATED_TYPE = "EVIDENCE";

    private static final Long CASE_FILE_ID_2 = 20L;


    // ============================================================
    // OBJETS DE TEST
    // ============================================================

    private CreateAttachmentRequest createRequest;
    private UpdateAttachmentRequest updateRequest;
    private PatchAttachmentRequest patchRequest;
    private AttachmentResponse attachmentResponse;


    // ============================================================
    // INITIALISATION
    // ============================================================

    @BeforeEach
    void setUp() {

        // ObjectMapper local avec support JSR-310
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Création : métadonnées du fichier (pas le fichier physique)
        createRequest = new CreateAttachmentRequest(
                CASE_FILE_ID, ORIGINAL_FILENAME, MIME_TYPE, FILE_SIZE, TYPE
        );

        // Mise à jour complète : seuls mimeType et type sont modifiables
        updateRequest = new UpdateAttachmentRequest(UPDATED_MIME_TYPE, UPDATED_TYPE);

        // Mise à jour partielle : seul le type change
        patchRequest = new PatchAttachmentRequest(null, UPDATED_TYPE);

        // Réponse de référence (filename et storagePath sont générés par le service)
        attachmentResponse = buildResponse(
                ATTACHMENT_ID, CASE_FILE_ID, GENERATED_FILENAME,
                ORIGINAL_FILENAME, MIME_TYPE, FILE_SIZE,
                STORAGE_PATH, TYPE
        );
    }


    // ============================================================
    // UTILITAIRES
    // ============================================================

    /**
     * Construit un DTO de réponse pour les tests via setters.
     *
     * <p>
     * Utilise des setters car l'ordre exact des paramètres
     * du constructeur de {@link AttachmentResponse} n'est pas garanti.
     * </p>
     *
     * @param id             identifiant technique
     * @param caseFileId     identifiant du dossier
     * @param filename       nom interne généré
     * @param originalFilename nom d'origine du fichier
     * @param mimeType       type MIME
     * @param fileSize       taille en octets
     * @param storagePath    chemin de stockage
     * @param type           type métier (PHOTO, VIDEO, etc.)
     * @return DTO de réponse prêt à l'emploi
     */
    private AttachmentResponse buildResponse(
            Long id, Long caseFileId,
            String filename, String originalFilename,
            String mimeType, Long fileSize,
            String storagePath, String type) {

        Instant now = Instant.now();

        AttachmentResponse r = new AttachmentResponse();
        r.setId(id);
        r.setCaseFileId(caseFileId);
        r.setFilename(filename);
        r.setOriginalFilename(originalFilename);
        r.setMimeType(mimeType);
        r.setFileSize(fileSize);
        r.setStoragePath(storagePath);
        r.setType(type);
        r.setUploadedAt(now);
        r.setCreatedAt(now);
        r.setCreatedBy("system");
        r.setUpdatedAt(now);
        r.setUpdatedBy("system");
        return r;
    }


    // ============================================================
    // TESTS : POST /api/attachments (Création)
    // ============================================================

    /**
     * Tests de l'endpoint POST /api/attachments.
     *
     * <p>
     * Vérifie la création réussie, le rejet pour dossier introuvable,
     * et les validations des champs obligatoires.
     * Pas de test 409 car il n'y a pas de contrainte d'unicité
     * sur les pièces jointes.
     * </p>
     */
    @Nested
    @DisplayName("POST /api/attachments - Création d'une pièce jointe")
    class CreateAttachmentTests {

        @Test
        @DisplayName("✅ Création réussie → 201 Created")
        void shouldCreateAttachmentSuccessfully() throws Exception {

            when(attachmentService.createAttachment(any(CreateAttachmentRequest.class)))
                    .thenReturn(attachmentResponse);

            mockMvc.perform(
                            post("/api/attachments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Pièce jointe créée avec succès"))
                    .andExpect(jsonPath("$.data.id").value(ATTACHMENT_ID))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.originalFilename").value(ORIGINAL_FILENAME))
                    .andExpect(jsonPath("$.data.mimeType").value(MIME_TYPE))
                    .andExpect(jsonPath("$.data.fileSize").value(FILE_SIZE))
                    .andExpect(jsonPath("$.data.type").value(TYPE))
                    // Champs générés par le service
                    .andExpect(jsonPath("$.data.filename").value(GENERATED_FILENAME))
                    .andExpect(jsonPath("$.data.storagePath").value(STORAGE_PATH));

            verify(attachmentService).createAttachment(any(CreateAttachmentRequest.class));
        }

        @Test
        @DisplayName("❌ Dossier inexistant → 404 Not Found")
        void shouldReturn404WhenCaseFileNotFound() throws Exception {

            when(attachmentService.createAttachment(any(CreateAttachmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Dossier", CASE_FILE_ID));

            mockMvc.perform(
                            post("/api/attachments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Dossier introuvable")));
        }

        @Test
        @DisplayName("❌ Validation échouée (caseFileId null) → 400 Bad Request")
        void shouldReturn400WhenCaseFileIdIsNull() throws Exception {

            CreateAttachmentRequest invalid = new CreateAttachmentRequest(
                    null, ORIGINAL_FILENAME, MIME_TYPE, FILE_SIZE, TYPE
            );

            mockMvc.perform(
                            post("/api/attachments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.caseFileId").exists());

            verify(attachmentService, never()).createAttachment(any());
        }

        @Test
        @DisplayName("❌ Validation échouée (mimeType vide) → 400 Bad Request")
        void shouldReturn400WhenMimeTypeIsBlank() throws Exception {

            CreateAttachmentRequest invalid = new CreateAttachmentRequest(
                    CASE_FILE_ID, ORIGINAL_FILENAME, "", FILE_SIZE, TYPE
            );

            mockMvc.perform(
                            post("/api/attachments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.mimeType").exists());

            verify(attachmentService, never()).createAttachment(any());
        }
    }


    // ============================================================
    // TESTS : GET /api/attachments (Liste)
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/attachments.
     */
    @Nested
    @DisplayName("GET /api/attachments - Liste des pièces jointes")
    class GetAllAttachmentsTests {

        @Test
        @DisplayName("✅ Récupération de la liste → 200 OK")
        void shouldGetAllAttachmentsSuccessfully() throws Exception {

            AttachmentResponse attachment2 = buildResponse(
                    2L, CASE_FILE_ID_2, "report.pdf",
                    "rapport_enquete.pdf", "application/pdf", 512000L,
                    "/uploads/cases/20/report.pdf", "REPORT"
            );

            when(attachmentService.getAllAttachments())
                    .thenReturn(List.of(attachmentResponse, attachment2));

            mockMvc.perform(get("/api/attachments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Pièces jointes récupérées avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].type").value(TYPE))
                    .andExpect(jsonPath("$.data[1].type").value("REPORT"));
        }

        @Test
        @DisplayName("✅ Liste vide → 200 OK avec data = []")
        void shouldReturnEmptyListWhenNoAttachments() throws Exception {

            when(attachmentService.getAllAttachments()).thenReturn(List.of());

            mockMvc.perform(get("/api/attachments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }


    // ============================================================
    // TESTS : GET /api/attachments/{id} (Détail)
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/attachments/{id}.
     */
    @Nested
    @DisplayName("GET /api/attachments/{id} - Détail d'une pièce jointe")
    class GetAttachmentByIdTests {

        @Test
        @DisplayName("✅ Pièce jointe trouvée → 200 OK")
        void shouldGetAttachmentByIdSuccessfully() throws Exception {

            when(attachmentService.getAttachmentById(ATTACHMENT_ID))
                    .thenReturn(attachmentResponse);

            mockMvc.perform(get("/api/attachments/{id}", ATTACHMENT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Pièce jointe récupérée avec succès"))
                    .andExpect(jsonPath("$.data.id").value(ATTACHMENT_ID))
                    .andExpect(jsonPath("$.data.originalFilename").value(ORIGINAL_FILENAME))
                    .andExpect(jsonPath("$.data.mimeType").value(MIME_TYPE))
                    .andExpect(jsonPath("$.data.storagePath").value(STORAGE_PATH));
        }

        @Test
        @DisplayName("❌ Pièce jointe inexistante → 404 Not Found")
        void shouldReturn404WhenAttachmentNotFound() throws Exception {

            when(attachmentService.getAttachmentById(ATTACHMENT_ID))
                    .thenThrow(new ResourceNotFoundException("Pièce jointe", ATTACHMENT_ID));

            mockMvc.perform(get("/api/attachments/{id}", ATTACHMENT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }


    // ============================================================
    // TESTS : GET /api/attachments/case/{caseFileId}
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/attachments/case/{caseFileId}.
     *
     * <p>
     * Vérifie la récupération des pièces jointes filtrées par dossier.
     * </p>
     */
    @Nested
    @DisplayName("GET /api/attachments/case/{caseFileId} - Pièces jointes d'un dossier")
    class GetAttachmentsByCaseFileTests {

        @Test
        @DisplayName("✅ Pièces jointes d'un dossier trouvées → 200 OK")
        void shouldGetAttachmentsByCaseFileSuccessfully() throws Exception {

            when(attachmentService.getAttachmentsByCaseFile(CASE_FILE_ID))
                    .thenReturn(List.of(attachmentResponse));

            mockMvc.perform(get("/api/attachments/case/{caseFileId}", CASE_FILE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Pièces jointes du dossier récupérées avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data[0].type").value(TYPE));
        }

        @Test
        @DisplayName("❌ Dossier inexistant → 404 Not Found")
        void shouldReturn404WhenCaseFileNotFound() throws Exception {

            when(attachmentService.getAttachmentsByCaseFile(CASE_FILE_ID))
                    .thenThrow(new ResourceNotFoundException("Dossier", CASE_FILE_ID));

            mockMvc.perform(get("/api/attachments/case/{caseFileId}", CASE_FILE_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Dossier introuvable")));
        }
    }


    // ============================================================
    // TESTS : PUT /api/attachments/{id} (Mise à jour complète)
    // ============================================================

    /**
     * Tests de l'endpoint PUT /api/attachments/{id}.
     *
     * <p>
     * Seuls {@code mimeType} et {@code type} sont modifiables.
     * Les champs de fichier (filename, storagePath, etc.) restent inchangés.
     * </p>
     */
    @Nested
    @DisplayName("PUT /api/attachments/{id} - Mise à jour complète")
    class UpdateAttachmentTests {

        @Test
        @DisplayName("✅ Mise à jour des métadonnées réussie → 200 OK")
        void shouldUpdateAttachmentSuccessfully() throws Exception {

            AttachmentResponse updated = buildResponse(
                    ATTACHMENT_ID, CASE_FILE_ID, GENERATED_FILENAME,
                    ORIGINAL_FILENAME, UPDATED_MIME_TYPE, FILE_SIZE,
                    STORAGE_PATH, UPDATED_TYPE
            );

            when(attachmentService.updateAttachment(eq(ATTACHMENT_ID), any(UpdateAttachmentRequest.class)))
                    .thenReturn(updated);

            mockMvc.perform(
                            put("/api/attachments/{id}", ATTACHMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Pièce jointe mise à jour avec succès"))
                    .andExpect(jsonPath("$.data.id").value(ATTACHMENT_ID))
                    .andExpect(jsonPath("$.data.mimeType").value(UPDATED_MIME_TYPE))
                    .andExpect(jsonPath("$.data.type").value(UPDATED_TYPE))
                    // Champs immuables restent inchangés
                    .andExpect(jsonPath("$.data.originalFilename").value(ORIGINAL_FILENAME))
                    .andExpect(jsonPath("$.data.filename").value(GENERATED_FILENAME))
                    .andExpect(jsonPath("$.data.storagePath").value(STORAGE_PATH));
        }

        @Test
        @DisplayName("❌ Pièce jointe inexistante → 404 Not Found")
        void shouldReturn404WhenUpdatingNonExistentAttachment() throws Exception {

            when(attachmentService.updateAttachment(eq(ATTACHMENT_ID), any(UpdateAttachmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Pièce jointe", ATTACHMENT_ID));

            mockMvc.perform(
                            put("/api/attachments/{id}", ATTACHMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Validation échouée (mimeType null) → 400 Bad Request")
        void shouldReturn400WhenMimeTypeIsNull() throws Exception {

            UpdateAttachmentRequest invalid = new UpdateAttachmentRequest(null, UPDATED_TYPE);

            mockMvc.perform(
                            put("/api/attachments/{id}", ATTACHMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.mimeType").exists());

            verify(attachmentService, never()).updateAttachment(eq(ATTACHMENT_ID), any());
        }

        @Test
        @DisplayName("❌ Validation échouée (type null) → 400 Bad Request")
        void shouldReturn400WhenTypeIsNull() throws Exception {

            UpdateAttachmentRequest invalid = new UpdateAttachmentRequest(UPDATED_MIME_TYPE, null);

            mockMvc.perform(
                            put("/api/attachments/{id}", ATTACHMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").exists());

            verify(attachmentService, never()).updateAttachment(eq(ATTACHMENT_ID), any());
        }
    }


    // ============================================================
    // TESTS : PATCH /api/attachments/{id} (Mise à jour partielle)
    // ============================================================

    /**
     * Tests de l'endpoint PATCH /api/attachments/{id}.
     *
     * <p>
     * Seuls {@code mimeType} et {@code type} sont modifiables et optionnels.
     * </p>
     */
    @Nested
    @DisplayName("PATCH /api/attachments/{id} - Mise à jour partielle")
    class PatchAttachmentTests {

        @Test
        @DisplayName("✅ Mise à jour partielle réussie (seul le type change) → 200 OK")
        void shouldPatchAttachmentSuccessfully() throws Exception {

            AttachmentResponse patched = buildResponse(
                    ATTACHMENT_ID, CASE_FILE_ID, GENERATED_FILENAME,
                    ORIGINAL_FILENAME, MIME_TYPE, FILE_SIZE,
                    STORAGE_PATH, UPDATED_TYPE
            );

            when(attachmentService.patchAttachment(eq(ATTACHMENT_ID), any(PatchAttachmentRequest.class)))
                    .thenReturn(patched);

            mockMvc.perform(
                            patch("/api/attachments/{id}", ATTACHMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(patchRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Pièce jointe partiellement mise à jour"))
                    .andExpect(jsonPath("$.data.type").value(UPDATED_TYPE))
                    // mimeType et champs de fichier inchangés
                    .andExpect(jsonPath("$.data.mimeType").value(MIME_TYPE))
                    .andExpect(jsonPath("$.data.filename").value(GENERATED_FILENAME));
        }

        @Test
        @DisplayName("❌ Pièce jointe inexistante → 404 Not Found")
        void shouldReturn404WhenPatchingNonExistentAttachment() throws Exception {

            when(attachmentService.patchAttachment(eq(ATTACHMENT_ID), any(PatchAttachmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Pièce jointe", ATTACHMENT_ID));

            mockMvc.perform(
                            patch("/api/attachments/{id}", ATTACHMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(patchRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }


    // ============================================================
    // TESTS : DELETE /api/attachments/{id} (Suppression logique)
    // ============================================================

    /**
     * Tests de l'endpoint DELETE /api/attachments/{id}.
     *
     * <p>
     * La suppression est logique (deleted = true).
     * Le contrôleur retourne 204 No Content.
     * </p>
     */
    @Nested
    @DisplayName("DELETE /api/attachments/{id} - Suppression logique")
    class DeleteAttachmentTests {

        @Test
        @DisplayName("✅ Suppression réussie → 204 No Content")
        void shouldDeleteAttachmentSuccessfully() throws Exception {

            mockMvc.perform(delete("/api/attachments/{id}", ATTACHMENT_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

            verify(attachmentService).deleteAttachment(ATTACHMENT_ID);
        }

        @Test
        @DisplayName("❌ Pièce jointe inexistante → 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentAttachment() throws Exception {

            doThrow(new ResourceNotFoundException("Pièce jointe", ATTACHMENT_ID))
                    .when(attachmentService).deleteAttachment(ATTACHMENT_ID);

            mockMvc.perform(delete("/api/attachments/{id}", ATTACHMENT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }
    }


    // ============================================================
    // CONFIGURATION DE TEST
    // ============================================================

    /**
     * Configuration locale pour ce test.
     *
     * <p>
     * Fournit un bean {@link AuditorAware} pour l'audit.
     * Pas de bean {@code objectMapper} : instance locale
     * dans {@link #setUp()}.
     * </p>
     */
    @Configuration
    static class TestConfig {

        @Bean
        @Primary
        public AuditorAware<String> auditorProvider() {
            return () -> Optional.of("test");
        }
    }
}