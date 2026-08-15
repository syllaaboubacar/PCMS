package lu.police.pcms.casecomment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lu.police.pcms.casecomment.dto.CaseCommentResponse;
import lu.police.pcms.casecomment.dto.CreateCaseCommentRequest;
import lu.police.pcms.casecomment.dto.PatchCaseCommentRequest;
import lu.police.pcms.casecomment.dto.UpdateCaseCommentRequest;
import lu.police.pcms.casecomment.service.CaseCommentService;
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
 * Tests d'intégration du contrôleur CaseCommentController.
 *
 * <p>
 * Ce test vérifie les 8 endpoints REST de gestion des commentaires
 * en utilisant MockMvc avec un contexte Spring Boot complet.
 * Le service est mocké pour isoler la couche contrôleur.
 * </p>
 *
 * <p>
 * Spécificités du module CaseComment :
 * </p>
 * <ul>
 *     <li>Un commentaire lie un dossier ({@code caseFileId}) et un
 *         utilisateur ({@code userId}) à un contenu textuel.</li>
 *     <li>Seul le champ {@code content} est modifiable après création.</li>
 *     <li>Les relations (dossier, utilisateur) sont immuables.</li>
 *     <li>Pas de contrainte d'unicité : un utilisateur peut commenter
 *         plusieurs fois le même dossier.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(CaseCommentControllerTest.TestConfig.class)
@DisplayName("Tests du contrôleur CaseCommentController")
class CaseCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper local pour rester indépendant du contexte Spring
     * et éviter les écrasements par les beans d'autres tests.
     */
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaseCommentService commentService;


    // ============================================================
    // CONSTANTES DE TEST
    // ============================================================

    private static final Long COMMENT_ID = 1L;
    private static final Long CASE_FILE_ID = 10L;
    private static final Long USER_ID = 5L;
    private static final String CONTENT = "Ceci est un commentaire d'enquête.";
    private static final String UPDATED_CONTENT = "Commentaire mis à jour.";
    private static final Long CASE_FILE_ID_2 = 20L;
    private static final Long USER_ID_2 = 6L;


    // ============================================================
    // OBJETS DE TEST
    // ============================================================

    private CreateCaseCommentRequest createRequest;
    private UpdateCaseCommentRequest updateRequest;
    private PatchCaseCommentRequest patchRequest;
    private CaseCommentResponse commentResponse;


    // ============================================================
    // INITIALISATION
    // ============================================================

    @BeforeEach
    void setUp() {

        // ObjectMapper local avec support JSR-310 (cohérence avec les autres tests)
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Création d'un commentaire
        createRequest = new CreateCaseCommentRequest(
                CASE_FILE_ID, USER_ID, CONTENT
        );

        // Mise à jour complète du contenu
        updateRequest = new UpdateCaseCommentRequest(UPDATED_CONTENT);

        // Mise à jour partielle du contenu
        patchRequest = new PatchCaseCommentRequest(UPDATED_CONTENT);

        // Réponse de référence
        commentResponse = buildResponse(
                COMMENT_ID, CASE_FILE_ID, USER_ID, CONTENT
        );
    }


    // ============================================================
    // UTILITAIRES
    // ============================================================

    /**
     * Construit un DTO de réponse pour les tests.
     *
     * @param id         identifiant du commentaire
     * @param caseFileId identifiant du dossier
     * @param userId     identifiant de l'utilisateur
     * @param content    contenu du commentaire
     * @return DTO de réponse prêt à l'emploi
     */
    private CaseCommentResponse buildResponse(
            Long id, Long caseFileId, Long userId, String content) {

        return new CaseCommentResponse(
                id, caseFileId, userId, content,
                Instant.now(), "system",
                Instant.now(), "system",
                false
        );
    }


    // ============================================================
    // TESTS : POST /api/comments (Création)
    // ============================================================

    /**
     * Tests de l'endpoint POST /api/comments.
     *
     * <p>
     * Vérifie :
     * <ul>
     *     <li>La création réussie (201)</li>
     *     <li>Le rejet si le dossier n'existe pas (404)</li>
     *     <li>Le rejet si l'utilisateur n'existe pas (404)</li>
     *     <li>Le rejet si le contenu est vide (400)</li>
     *     <li>Le rejet si caseFileId est null (400)</li>
     * </ul>
     */
    @Nested
    @DisplayName("POST /api/comments - Création d'un commentaire")
    class CreateCommentTests {

        @Test
        @DisplayName("✅ Création réussie → 201 Created")
        void shouldCreateCommentSuccessfully() throws Exception {

            when(commentService.createComment(any(CreateCaseCommentRequest.class)))
                    .thenReturn(commentResponse);

            mockMvc.perform(
                            post("/api/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Commentaire créé avec succès"))
                    .andExpect(jsonPath("$.data.id").value(COMMENT_ID))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID))
                    .andExpect(jsonPath("$.data.content").value(CONTENT));

            verify(commentService).createComment(any(CreateCaseCommentRequest.class));
        }

        @Test
        @DisplayName("❌ Dossier inexistant → 404 Not Found")
        void shouldReturn404WhenCaseFileNotFound() throws Exception {

            when(commentService.createComment(any(CreateCaseCommentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Dossier", CASE_FILE_ID));

            mockMvc.perform(
                            post("/api/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Dossier introuvable")));
        }

        @Test
        @DisplayName("❌ Utilisateur inexistant → 404 Not Found")
        void shouldReturn404WhenUserNotFound() throws Exception {

            when(commentService.createComment(any(CreateCaseCommentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Utilisateur", USER_ID));

            mockMvc.perform(
                            post("/api/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Utilisateur introuvable")));
        }

        @Test
        @DisplayName("❌ Validation échouée (contenu vide) → 400 Bad Request")
        void shouldReturn400WhenContentIsBlank() throws Exception {

            CreateCaseCommentRequest invalid = new CreateCaseCommentRequest(
                    CASE_FILE_ID, USER_ID, ""
            );

            mockMvc.perform(
                            post("/api/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.content").exists());

            verify(commentService, never()).createComment(any());
        }

        @Test
        @DisplayName("❌ Validation échouée (caseFileId null) → 400 Bad Request")
        void shouldReturn400WhenCaseFileIdIsNull() throws Exception {

            CreateCaseCommentRequest invalid = new CreateCaseCommentRequest(
                    null, USER_ID, CONTENT
            );

            mockMvc.perform(
                            post("/api/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.caseFileId").exists());

            verify(commentService, never()).createComment(any());
        }
    }


    // ============================================================
    // TESTS : GET /api/comments (Liste)
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/comments.
     */
    @Nested
    @DisplayName("GET /api/comments - Liste des commentaires")
    class GetAllCommentsTests {

        @Test
        @DisplayName("✅ Récupération de la liste → 200 OK")
        void shouldGetAllCommentsSuccessfully() throws Exception {

            CaseCommentResponse comment2 = buildResponse(
                    2L, CASE_FILE_ID_2, USER_ID_2, "Autre commentaire"
            );

            when(commentService.getAllComments())
                    .thenReturn(List.of(commentResponse, comment2));

            mockMvc.perform(get("/api/comments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Commentaires récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].content").value(CONTENT))
                    .andExpect(jsonPath("$.data[1].content").value("Autre commentaire"));
        }

        @Test
        @DisplayName("✅ Liste vide → 200 OK avec data = []")
        void shouldReturnEmptyListWhenNoComments() throws Exception {

            when(commentService.getAllComments()).thenReturn(List.of());

            mockMvc.perform(get("/api/comments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }


    // ============================================================
    // TESTS : GET /api/comments/{id} (Détail)
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/comments/{id}.
     */
    @Nested
    @DisplayName("GET /api/comments/{id} - Détail d'un commentaire")
    class GetCommentByIdTests {

        @Test
        @DisplayName("✅ Commentaire trouvé → 200 OK")
        void shouldGetCommentByIdSuccessfully() throws Exception {

            when(commentService.getCommentById(COMMENT_ID))
                    .thenReturn(commentResponse);

            mockMvc.perform(get("/api/comments/{id}", COMMENT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Commentaire récupéré avec succès"))
                    .andExpect(jsonPath("$.data.id").value(COMMENT_ID))
                    .andExpect(jsonPath("$.data.content").value(CONTENT))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID));
        }

        @Test
        @DisplayName("❌ Commentaire inexistant → 404 Not Found")
        void shouldReturn404WhenCommentNotFound() throws Exception {

            when(commentService.getCommentById(COMMENT_ID))
                    .thenThrow(new ResourceNotFoundException("Commentaire", COMMENT_ID));

            mockMvc.perform(get("/api/comments/{id}", COMMENT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }


    // ============================================================
    // TESTS : GET /api/comments/case/{caseFileId}
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/comments/case/{caseFileId}.
     *
     * <p>
     * Vérifie la récupération des commentaires filtrés par dossier.
     * </p>
     */
    @Nested
    @DisplayName("GET /api/comments/case/{caseFileId} - Commentaires d'un dossier")
    class GetCommentsByCaseFileTests {

        @Test
        @DisplayName("✅ Commentaires d'un dossier trouvés → 200 OK")
        void shouldGetCommentsByCaseFileSuccessfully() throws Exception {

            when(commentService.getCommentsByCaseFile(CASE_FILE_ID))
                    .thenReturn(List.of(commentResponse));

            mockMvc.perform(get("/api/comments/case/{caseFileId}", CASE_FILE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Commentaires du dossier récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data[0].content").value(CONTENT));
        }

        @Test
        @DisplayName("❌ Dossier inexistant → 404 Not Found")
        void shouldReturn404WhenCaseFileNotFound() throws Exception {

            when(commentService.getCommentsByCaseFile(CASE_FILE_ID))
                    .thenThrow(new ResourceNotFoundException("Dossier", CASE_FILE_ID));

            mockMvc.perform(get("/api/comments/case/{caseFileId}", CASE_FILE_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Dossier introuvable")));
        }
    }


    // ============================================================
    // TESTS : GET /api/comments/user/{userId}
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/comments/user/{userId}.
     *
     * <p>
     * Vérifie la récupération des commentaires filtrés par utilisateur.
     * </p>
     */
    @Nested
    @DisplayName("GET /api/comments/user/{userId} - Commentaires d'un utilisateur")
    class GetCommentsByUserTests {

        @Test
        @DisplayName("✅ Commentaires d'un utilisateur trouvés → 200 OK")
        void shouldGetCommentsByUserSuccessfully() throws Exception {

            when(commentService.getCommentsByUser(USER_ID))
                    .thenReturn(List.of(commentResponse));

            mockMvc.perform(get("/api/comments/user/{userId}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Commentaires de l'utilisateur récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].userId").value(USER_ID))
                    .andExpect(jsonPath("$.data[0].content").value(CONTENT));
        }

        @Test
        @DisplayName("❌ Utilisateur inexistant → 404 Not Found")
        void shouldReturn404WhenUserNotFound() throws Exception {

            when(commentService.getCommentsByUser(USER_ID))
                    .thenThrow(new ResourceNotFoundException("Utilisateur", USER_ID));

            mockMvc.perform(get("/api/comments/user/{userId}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Utilisateur introuvable")));
        }
    }


    // ============================================================
    // TESTS : PUT /api/comments/{id} (Mise à jour complète)
    // ============================================================

    /**
     * Tests de l'endpoint PUT /api/comments/{id}.
     *
     * <p>
     * Seul le champ {@code content} est modifiable.
     * </p>
     */
    @Nested
    @DisplayName("PUT /api/comments/{id} - Mise à jour complète")
    class UpdateCommentTests {

        @Test
        @DisplayName("✅ Mise à jour du contenu réussie → 200 OK")
        void shouldUpdateCommentSuccessfully() throws Exception {

            CaseCommentResponse updated = buildResponse(
                    COMMENT_ID, CASE_FILE_ID, USER_ID, UPDATED_CONTENT
            );

            when(commentService.updateComment(eq(COMMENT_ID), any(UpdateCaseCommentRequest.class)))
                    .thenReturn(updated);

            mockMvc.perform(
                            put("/api/comments/{id}", COMMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Commentaire mis à jour avec succès"))
                    .andExpect(jsonPath("$.data.id").value(COMMENT_ID))
                    .andExpect(jsonPath("$.data.content").value(UPDATED_CONTENT))
                    // caseFileId et userId doivent rester inchangés
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID));
        }

        @Test
        @DisplayName("❌ Commentaire inexistant → 404 Not Found")
        void shouldReturn404WhenUpdatingNonExistentComment() throws Exception {

            when(commentService.updateComment(eq(COMMENT_ID), any(UpdateCaseCommentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Commentaire", COMMENT_ID));

            mockMvc.perform(
                            put("/api/comments/{id}", COMMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Validation échouée (contenu vide) → 400 Bad Request")
        void shouldReturn400WhenContentIsBlank() throws Exception {

            UpdateCaseCommentRequest invalid = new UpdateCaseCommentRequest("");

            mockMvc.perform(
                            put("/api/comments/{id}", COMMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.content").exists());

            verify(commentService, never()).updateComment(eq(COMMENT_ID), any());
        }
    }


    // ============================================================
    // TESTS : PATCH /api/comments/{id} (Mise à jour partielle)
    // ============================================================

    /**
     * Tests de l'endpoint PATCH /api/comments/{id}.
     *
     * <p>
     * Seul le champ {@code content} est modifiable et optionnel.
     * </p>
     */
    @Nested
    @DisplayName("PATCH /api/comments/{id} - Mise à jour partielle")
    class PatchCommentTests {

        @Test
        @DisplayName("✅ Mise à jour partielle réussie → 200 OK")
        void shouldPatchCommentSuccessfully() throws Exception {

            CaseCommentResponse patched = buildResponse(
                    COMMENT_ID, CASE_FILE_ID, USER_ID, UPDATED_CONTENT
            );

            when(commentService.patchComment(eq(COMMENT_ID), any(PatchCaseCommentRequest.class)))
                    .thenReturn(patched);

            mockMvc.perform(
                            patch("/api/comments/{id}", COMMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(patchRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Commentaire partiellement mis à jour"))
                    .andExpect(jsonPath("$.data.content").value(UPDATED_CONTENT))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID));
        }

        @Test
        @DisplayName("❌ Commentaire inexistant → 404 Not Found")
        void shouldReturn404WhenPatchingNonExistentComment() throws Exception {

            when(commentService.patchComment(eq(COMMENT_ID), any(PatchCaseCommentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Commentaire", COMMENT_ID));

            mockMvc.perform(
                            patch("/api/comments/{id}", COMMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(patchRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }


    // ============================================================
    // TESTS : DELETE /api/comments/{id} (Suppression logique)
    // ============================================================

    /**
     * Tests de l'endpoint DELETE /api/comments/{id}.
     *
     * <p>
     * La suppression est logique (deleted = true).
     * Le contrôleur retourne 204 No Content sans corps de réponse.
     * </p>
     */
    @Nested
    @DisplayName("DELETE /api/comments/{id} - Suppression logique")
    class DeleteCommentTests {

        @Test
        @DisplayName("✅ Suppression réussie → 204 No Content")
        void shouldDeleteCommentSuccessfully() throws Exception {

            mockMvc.perform(delete("/api/comments/{id}", COMMENT_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

            verify(commentService).deleteComment(COMMENT_ID);
        }

        @Test
        @DisplayName("❌ Commentaire inexistant → 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentComment() throws Exception {

            doThrow(new ResourceNotFoundException("Commentaire", COMMENT_ID))
                    .when(commentService).deleteComment(COMMENT_ID);

            mockMvc.perform(delete("/api/comments/{id}", COMMENT_ID))
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
     * Fournit un bean {@link AuditorAware} pour satisfaire
     * l'infrastructure d'audit de Spring Data JPA.
     * Pas de bean {@code objectMapper} : on utilise l'instance
     * locale dans {@link #setUp()}.
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