package lu.police.pcms.caseassignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lu.police.pcms.caseassignment.dto.CaseAssignmentResponse;
import lu.police.pcms.caseassignment.dto.CreateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.PatchCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.UpdateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.service.CaseAssignmentService;
import lu.police.pcms.common.exception.DuplicateResourceException;
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
 * Tests d'intégration du contrôleur CaseAssignmentController.
 *
 * <p>
 * Ce test vérifie les 8 endpoints REST de gestion des affectations
 * en utilisant MockMvc avec un contexte Spring Boot complet.
 * Le service est mocké pour isoler la couche contrôleur.
 * </p>
 *
 * <p>
 * Points de vigilance spécifiques à ce module :
 * </p>
 * <ul>
 *     <li>ObjectMapper local avec JavaTimeModule pour éviter les
 *         conflits de beans entre les différents tests du projet.</li>
 *     <li>La contrainte d'unicité porte sur le couple (dossier, utilisateur).</li>
 *     <li>Seul le champ {@code active} est modifiable après création.</li>
 *     <li>Les endpoints de filtrage retournent des listes.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(CaseAssignmentControllerTest.TestConfig.class)
@DisplayName("Tests du contrôleur CaseAssignmentController")
class CaseAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper local avec JavaTimeModule pour sérialiser
     * correctement les Instant sans dépendre du contexte Spring
     * (qui peut être écrasé par les beans d'autres tests).
     */
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaseAssignmentService assignmentService;


    // ============================================================
    // CONSTANTES DE TEST
    // ============================================================

    private static final Long ASSIGNMENT_ID = 1L;
    private static final Long CASE_FILE_ID = 10L;
    private static final Long USER_ID = 5L;
    private static final Instant ASSIGNED_AT = Instant.parse("2026-08-15T10:00:00Z");
    private static final Long CASE_FILE_ID_2 = 20L;
    private static final Long USER_ID_2 = 6L;


    // ============================================================
    // OBJETS DE TEST
    // ============================================================

    private CreateCaseAssignmentRequest createRequest;
    private CreateCaseAssignmentRequest createRequestWithoutDate;
    private UpdateCaseAssignmentRequest updateRequest;
    private PatchCaseAssignmentRequest patchRequest;
    private CaseAssignmentResponse assignmentResponse;


    // ============================================================
    // INITIALISATION
    // ============================================================

    @BeforeEach
    void setUp() {

        // ObjectMapper local avec support des types Java 8 Time
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Création avec date fournie
        createRequest = new CreateCaseAssignmentRequest(
                CASE_FILE_ID, USER_ID, ASSIGNED_AT
        );

        // Création sans date (le service utilisera Instant.now())
        createRequestWithoutDate = new CreateCaseAssignmentRequest(
                CASE_FILE_ID, USER_ID, null
        );

        // Désactivation via PUT
        updateRequest = new UpdateCaseAssignmentRequest(false);

        // Désactivation via PATCH
        patchRequest = new PatchCaseAssignmentRequest(false);

        // Réponse de référence
        assignmentResponse = buildResponse(
                ASSIGNMENT_ID, CASE_FILE_ID, USER_ID, ASSIGNED_AT, true
        );
    }


    // ============================================================
    // UTILITAIRES
    // ============================================================

    /**
     * Construit un DTO de réponse pour les tests.
     */
    private CaseAssignmentResponse buildResponse(
            Long id, Long caseFileId, Long userId,
            Instant assignedAt, Boolean active) {

        return new CaseAssignmentResponse(
                id, caseFileId, userId, assignedAt, active,
                Instant.now(), "system",
                Instant.now(), "system",
                false
        );
    }


    // ============================================================
    // TESTS : POST /api/assignments
    // ============================================================

    @Nested
    @DisplayName("POST /api/assignments - Création d'une affectation")
    class CreateAssignmentTests {

        @Test
        @DisplayName("✅ Création réussie (avec date fournie) → 201 Created")
        void shouldCreateAssignmentSuccessfully() throws Exception {

            when(assignmentService.createAssignment(any(CreateCaseAssignmentRequest.class)))
                    .thenReturn(assignmentResponse);

            mockMvc.perform(
                            post("/api/assignments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Affectation créée avec succès"))
                    .andExpect(jsonPath("$.data.id").value(ASSIGNMENT_ID))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID))
                    .andExpect(jsonPath("$.data.active").value(true));

            verify(assignmentService).createAssignment(any(CreateCaseAssignmentRequest.class));
        }

        @Test
        @DisplayName("✅ Création réussie (sans date) → 201 Created")
        void shouldCreateAssignmentWithoutDate() throws Exception {

            CaseAssignmentResponse response = buildResponse(
                    ASSIGNMENT_ID, CASE_FILE_ID, USER_ID, Instant.now(), true
            );

            when(assignmentService.createAssignment(any(CreateCaseAssignmentRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(
                            post("/api/assignments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequestWithoutDate))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID));
        }

        @Test
        @DisplayName("❌ Dossier inexistant → 404 Not Found")
        void shouldReturn404WhenCaseFileNotFound() throws Exception {

            when(assignmentService.createAssignment(any(CreateCaseAssignmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Dossier", CASE_FILE_ID));

            mockMvc.perform(
                            post("/api/assignments")
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

            when(assignmentService.createAssignment(any(CreateCaseAssignmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Utilisateur", USER_ID));

            mockMvc.perform(
                            post("/api/assignments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Utilisateur introuvable")));
        }

        @Test
        @DisplayName("❌ Affectation déjà existante → 409 Conflict")
        void shouldReturn409WhenAssignmentAlreadyExists() throws Exception {

            when(assignmentService.createAssignment(any(CreateCaseAssignmentRequest.class)))
                    .thenThrow(new DuplicateResourceException(
                            "Affectation",
                            "dossier " + CASE_FILE_ID + " et utilisateur " + USER_ID,
                            ""
                    ));

            mockMvc.perform(
                            post("/api/assignments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value(containsString("existe déjà")));
        }

        @Test
        @DisplayName("❌ Validation échouée (caseFileId null) → 400 Bad Request")
        void shouldReturn400WhenCaseFileIdIsNull() throws Exception {

            CreateCaseAssignmentRequest invalid = new CreateCaseAssignmentRequest(
                    null, USER_ID, ASSIGNED_AT
            );

            mockMvc.perform(
                            post("/api/assignments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.caseFileId").exists());

            verify(assignmentService, never()).createAssignment(any());
        }

        @Test
        @DisplayName("❌ Validation échouée (userId null) → 400 Bad Request")
        void shouldReturn400WhenUserIdIsNull() throws Exception {

            CreateCaseAssignmentRequest invalid = new CreateCaseAssignmentRequest(
                    CASE_FILE_ID, null, ASSIGNED_AT
            );

            mockMvc.perform(
                            post("/api/assignments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.userId").exists());

            verify(assignmentService, never()).createAssignment(any());
        }
    }


    // ============================================================
    // TESTS : GET /api/assignments
    // ============================================================

    @Nested
    @DisplayName("GET /api/assignments - Liste des affectations")
    class GetAllAssignmentsTests {

        @Test
        @DisplayName("✅ Récupération de la liste → 200 OK")
        void shouldGetAllAssignmentsSuccessfully() throws Exception {

            CaseAssignmentResponse assignment2 = buildResponse(
                    2L, CASE_FILE_ID_2, USER_ID_2, Instant.now(), true
            );

            when(assignmentService.getAllAssignments())
                    .thenReturn(List.of(assignmentResponse, assignment2));

            mockMvc.perform(get("/api/assignments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Affectations récupérées avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data[1].caseFileId").value(CASE_FILE_ID_2));
        }

        @Test
        @DisplayName("✅ Liste vide → 200 OK avec data = []")
        void shouldReturnEmptyListWhenNoAssignments() throws Exception {

            when(assignmentService.getAllAssignments()).thenReturn(List.of());

            mockMvc.perform(get("/api/assignments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }


    // ============================================================
    // TESTS : GET /api/assignments/{id}
    // ============================================================

    @Nested
    @DisplayName("GET /api/assignments/{id} - Détail d'une affectation")
    class GetAssignmentByIdTests {

        @Test
        @DisplayName("✅ Affectation trouvée → 200 OK")
        void shouldGetAssignmentByIdSuccessfully() throws Exception {

            when(assignmentService.getAssignmentById(ASSIGNMENT_ID))
                    .thenReturn(assignmentResponse);

            mockMvc.perform(get("/api/assignments/{id}", ASSIGNMENT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Affectation récupérée avec succès"))
                    .andExpect(jsonPath("$.data.id").value(ASSIGNMENT_ID))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID))
                    .andExpect(jsonPath("$.data.active").value(true));
        }

        @Test
        @DisplayName("❌ Affectation inexistante → 404 Not Found")
        void shouldReturn404WhenAssignmentNotFound() throws Exception {

            when(assignmentService.getAssignmentById(ASSIGNMENT_ID))
                    .thenThrow(new ResourceNotFoundException("Affectation", ASSIGNMENT_ID));

            mockMvc.perform(get("/api/assignments/{id}", ASSIGNMENT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }


    // ============================================================
    // TESTS : GET /api/assignments/case/{caseFileId}
    // ============================================================

    @Nested
    @DisplayName("GET /api/assignments/case/{caseFileId} - Affectations d'un dossier")
    class GetAssignmentsByCaseFileTests {

        @Test
        @DisplayName("✅ Affectations d'un dossier trouvées → 200 OK")
        void shouldGetAssignmentsByCaseFileSuccessfully() throws Exception {

            when(assignmentService.getAssignmentsByCaseFile(CASE_FILE_ID))
                    .thenReturn(List.of(assignmentResponse));

            mockMvc.perform(get("/api/assignments/case/{caseFileId}", CASE_FILE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Affectations du dossier récupérées avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].caseFileId").value(CASE_FILE_ID));
        }

        @Test
        @DisplayName("❌ Dossier inexistant → 404 Not Found")
        void shouldReturn404WhenCaseFileNotFound() throws Exception {

            when(assignmentService.getAssignmentsByCaseFile(CASE_FILE_ID))
                    .thenThrow(new ResourceNotFoundException("Dossier", CASE_FILE_ID));

            mockMvc.perform(get("/api/assignments/case/{caseFileId}", CASE_FILE_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Dossier introuvable")));
        }
    }


    // ============================================================
    // TESTS : GET /api/assignments/user/{userId}
    // ============================================================

    @Nested
    @DisplayName("GET /api/assignments/user/{userId} - Affectations d'un utilisateur")
    class GetAssignmentsByUserTests {

        @Test
        @DisplayName("✅ Affectations d'un utilisateur trouvées → 200 OK")
        void shouldGetAssignmentsByUserSuccessfully() throws Exception {

            when(assignmentService.getAssignmentsByUser(USER_ID))
                    .thenReturn(List.of(assignmentResponse));

            mockMvc.perform(get("/api/assignments/user/{userId}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Affectations de l'utilisateur récupérées avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].userId").value(USER_ID));
        }

        @Test
        @DisplayName("❌ Utilisateur inexistant → 404 Not Found")
        void shouldReturn404WhenUserNotFound() throws Exception {

            when(assignmentService.getAssignmentsByUser(USER_ID))
                    .thenThrow(new ResourceNotFoundException("Utilisateur", USER_ID));

            mockMvc.perform(get("/api/assignments/user/{userId}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Utilisateur introuvable")));
        }
    }


    // ============================================================
    // TESTS : PUT /api/assignments/{id}
    // ============================================================

    @Nested
    @DisplayName("PUT /api/assignments/{id} - Mise à jour complète")
    class UpdateAssignmentTests {

        @Test
        @DisplayName("✅ Désactivation réussie → 200 OK")
        void shouldUpdateAssignmentSuccessfully() throws Exception {

            CaseAssignmentResponse deactivated = buildResponse(
                    ASSIGNMENT_ID, CASE_FILE_ID, USER_ID, ASSIGNED_AT, false
            );

            when(assignmentService.updateAssignment(eq(ASSIGNMENT_ID), any(UpdateCaseAssignmentRequest.class)))
                    .thenReturn(deactivated);

            mockMvc.perform(
                            put("/api/assignments/{id}", ASSIGNMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Affectation mise à jour avec succès"))
                    .andExpect(jsonPath("$.data.id").value(ASSIGNMENT_ID))
                    .andExpect(jsonPath("$.data.active").value(false))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID));
        }

        @Test
        @DisplayName("❌ Affectation inexistante → 404 Not Found")
        void shouldReturn404WhenUpdatingNonExistentAssignment() throws Exception {

            when(assignmentService.updateAssignment(eq(ASSIGNMENT_ID), any(UpdateCaseAssignmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Affectation", ASSIGNMENT_ID));

            mockMvc.perform(
                            put("/api/assignments/{id}", ASSIGNMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Validation échouée (active null) → 400 Bad Request")
        void shouldReturn400WhenActiveIsNull() throws Exception {

            UpdateCaseAssignmentRequest invalid = new UpdateCaseAssignmentRequest(null);

            mockMvc.perform(
                            put("/api/assignments/{id}", ASSIGNMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.active").exists());

            verify(assignmentService, never()).updateAssignment(eq(ASSIGNMENT_ID), any());
        }
    }


    // ============================================================
    // TESTS : PATCH /api/assignments/{id}
    // ============================================================

    @Nested
    @DisplayName("PATCH /api/assignments/{id} - Mise à jour partielle")
    class PatchAssignmentTests {

        @Test
        @DisplayName("✅ Désactivation partielle réussie → 200 OK")
        void shouldPatchAssignmentSuccessfully() throws Exception {

            CaseAssignmentResponse deactivated = buildResponse(
                    ASSIGNMENT_ID, CASE_FILE_ID, USER_ID, ASSIGNED_AT, false
            );

            when(assignmentService.patchAssignment(eq(ASSIGNMENT_ID), any(PatchCaseAssignmentRequest.class)))
                    .thenReturn(deactivated);

            mockMvc.perform(
                            patch("/api/assignments/{id}", ASSIGNMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(patchRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Affectation partiellement mise à jour"))
                    .andExpect(jsonPath("$.data.active").value(false))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID));
        }

        @Test
        @DisplayName("❌ Affectation inexistante → 404 Not Found")
        void shouldReturn404WhenPatchingNonExistentAssignment() throws Exception {

            when(assignmentService.patchAssignment(eq(ASSIGNMENT_ID), any(PatchCaseAssignmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Affectation", ASSIGNMENT_ID));

            mockMvc.perform(
                            patch("/api/assignments/{id}", ASSIGNMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(patchRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }


    // ============================================================
    // TESTS : DELETE /api/assignments/{id}
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/assignments/{id} - Suppression logique")
    class DeleteAssignmentTests {

        @Test
        @DisplayName("✅ Suppression réussie → 204 No Content")
        void shouldDeleteAssignmentSuccessfully() throws Exception {

            mockMvc.perform(delete("/api/assignments/{id}", ASSIGNMENT_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

            verify(assignmentService).deleteAssignment(ASSIGNMENT_ID);
        }

        @Test
        @DisplayName("❌ Affectation inexistante → 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentAssignment() throws Exception {

            doThrow(new ResourceNotFoundException("Affectation", ASSIGNMENT_ID))
                    .when(assignmentService).deleteAssignment(ASSIGNMENT_ID);

            mockMvc.perform(delete("/api/assignments/{id}", ASSIGNMENT_ID))
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
     * Fournit uniquement un bean {@link AuditorAware} pour satisfaire
     * l'infrastructure d'audit de Spring Data JPA.
     * </p>
     *
     * <p>
     * Pas de bean {@code objectMapper} ici : on utilise l'instance
     * locale dans {@link #setUp()} pour éviter les écrasements
     * par les beans d'autres tests du projet.
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