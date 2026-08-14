package lu.police.pcms.department.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.department.dto.CreateDepartmentRequest;
import lu.police.pcms.department.dto.DepartmentResponse;
import lu.police.pcms.department.dto.PatchDepartmentRequest;
import lu.police.pcms.department.dto.UpdateDepartmentRequest;
import lu.police.pcms.department.service.DepartmentService;
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
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du contrôleur {@link DepartmentController}.
 *
 * <p>
 * Utilise {@link SpringBootTest} pour charger tout le contexte,
 * mais {@code DepartmentService} est mocké avec {@link MockitoBean}.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(DepartmentControllerTest.TestConfig.class)
@DisplayName("Tests du contrôleur DepartmentController")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    // ============================================================
    // DONNÉES DE TEST
    // ============================================================

    private static final Long DEPT_ID = 1L;
    private static final String DEPT_CODE = "INV";
    private static final String DEPT_NAME = "Investigations";
    private static final String DEPT_CODE_UPDATED = "IT";
    private static final String DEPT_NAME_UPDATED = "Informatique";

    private CreateDepartmentRequest createRequest;
    private UpdateDepartmentRequest updateRequest;
    private PatchDepartmentRequest patchRequest;
    private DepartmentResponse departmentResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateDepartmentRequest(DEPT_CODE, DEPT_NAME);
        updateRequest = new UpdateDepartmentRequest(DEPT_CODE_UPDATED, DEPT_NAME_UPDATED);
        patchRequest = new PatchDepartmentRequest(DEPT_CODE_UPDATED, null);

        departmentResponse = new DepartmentResponse(
                DEPT_ID,
                DEPT_CODE,
                DEPT_NAME,
                Instant.now(),
                "system",
                Instant.now(),
                "system",
                false
        );
    }

    // ============================================================
    // TESTS : POST /api/departments (Création)
    // ============================================================

    @Nested
    @DisplayName("POST /api/departments - Création d'un département")
    class CreateDepartmentTests {

        @Test
        @DisplayName("✅ Création réussie → 201 Created")
        void shouldCreateDepartmentSuccessfully() throws Exception {
            when(departmentService.createDepartment(any(CreateDepartmentRequest.class)))
                    .thenReturn(departmentResponse);

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Département créé avec succès"))
                    .andExpect(jsonPath("$.data.id").value(DEPT_ID))
                    .andExpect(jsonPath("$.data.code").value(DEPT_CODE))
                    .andExpect(jsonPath("$.data.name").value(DEPT_NAME));

            verify(departmentService).createDepartment(any(CreateDepartmentRequest.class));
        }

        @Test
        @DisplayName("❌ Code déjà existant → 409 Conflict")
        void shouldReturn409WhenCodeAlreadyExists() throws Exception {
            when(departmentService.createDepartment(any(CreateDepartmentRequest.class)))
                    .thenThrow(new DuplicateResourceException("Département", "code", DEPT_CODE));

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value(containsString("existe déjà")));
        }

        @Test
        @DisplayName("❌ Nom déjà existant → 409 Conflict")
        void shouldReturn409WhenNameAlreadyExists() throws Exception {
            when(departmentService.createDepartment(any(CreateDepartmentRequest.class)))
                    .thenThrow(new DuplicateResourceException("Département", "nom", DEPT_NAME));

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value(containsString("existe déjà")));
        }

        @Test
        @DisplayName("❌ Validation échouée (code vide) → 400 Bad Request")
        void shouldReturn400WhenCodeIsBlank() throws Exception {
            CreateDepartmentRequest invalidRequest = new CreateDepartmentRequest("", DEPT_NAME);

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.code").exists());

            verify(departmentService, never()).createDepartment(any(CreateDepartmentRequest.class));
        }

        @Test
        @DisplayName("❌ Validation échouée (nom vide) → 400 Bad Request")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            CreateDepartmentRequest invalidRequest = new CreateDepartmentRequest(DEPT_CODE, "");

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.name").exists());
        }

        @Test
        @DisplayName("❌ Validation échouée (code avec caractères invalides) → 400 Bad Request")
        void shouldReturn400WhenCodeInvalidFormat() throws Exception {
            // Le code doit être en majuscules, sans espaces, avec chiffres, tirets ou underscores
            CreateDepartmentRequest invalidRequest = new CreateDepartmentRequest("Invalid Code", DEPT_NAME);

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.code").value(containsString("uniquement des lettres majuscules")));
        }
    }

    // ============================================================
    // TESTS : GET /api/departments (Liste)
    // ============================================================

    @Nested
    @DisplayName("GET /api/departments - Liste des départements")
    class GetAllDepartmentsTests {

        @Test
        @DisplayName("✅ Récupération de la liste → 200 OK")
        void shouldGetAllDepartmentsSuccessfully() throws Exception {
            DepartmentResponse dept2 = new DepartmentResponse(
                    2L, "HR", "Ressources Humaines",
                    Instant.now(), "system", Instant.now(), "system", false
            );
            List<DepartmentResponse> departments = List.of(departmentResponse, dept2);

            when(departmentService.getAllDepartments()).thenReturn(departments);

            mockMvc.perform(get("/api/departments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Départements récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].code").value(DEPT_CODE))
                    .andExpect(jsonPath("$.data[1].code").value("HR"));
        }

        @Test
        @DisplayName("✅ Liste vide → 200 OK avec data = []")
        void shouldReturnEmptyListWhenNoDepartments() throws Exception {
            when(departmentService.getAllDepartments()).thenReturn(List.of());

            mockMvc.perform(get("/api/departments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }

    // ============================================================
    // TESTS : GET /api/departments/{id} (Détail)
    // ============================================================

    @Nested
    @DisplayName("GET /api/departments/{id} - Détail d'un département")
    class GetDepartmentByIdTests {

        @Test
        @DisplayName("✅ Département trouvé → 200 OK")
        void shouldGetDepartmentByIdSuccessfully() throws Exception {
            when(departmentService.getDepartmentById(DEPT_ID)).thenReturn(departmentResponse);

            mockMvc.perform(get("/api/departments/{id}", DEPT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(DEPT_ID))
                    .andExpect(jsonPath("$.data.code").value(DEPT_CODE));
        }

        @Test
        @DisplayName("❌ Département inexistant → 404 Not Found")
        void shouldReturn404WhenDepartmentNotFound() throws Exception {
            when(departmentService.getDepartmentById(DEPT_ID))
                    .thenThrow(new ResourceNotFoundException("Département", DEPT_ID));

            mockMvc.perform(get("/api/departments/{id}", DEPT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }

    // ============================================================
    // TESTS : PUT /api/departments/{id} (Mise à jour complète)
    // ============================================================

    @Nested
    @DisplayName("PUT /api/departments/{id} - Mise à jour complète")
    class UpdateDepartmentTests {

        @Test
        @DisplayName("✅ Mise à jour réussie → 200 OK")
        void shouldUpdateDepartmentSuccessfully() throws Exception {
            DepartmentResponse updatedResponse = new DepartmentResponse(
                    DEPT_ID, DEPT_CODE_UPDATED, DEPT_NAME_UPDATED,
                    Instant.now(), "system", Instant.now(), "system", false
            );

            when(departmentService.updateDepartment(eq(DEPT_ID), any(UpdateDepartmentRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/departments/{id}", DEPT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value(DEPT_CODE_UPDATED))
                    .andExpect(jsonPath("$.data.name").value(DEPT_NAME_UPDATED));
        }

        @Test
        @DisplayName("❌ Département inexistant → 404 Not Found")
        void shouldReturn404WhenUpdatingNonExistentDepartment() throws Exception {
            when(departmentService.updateDepartment(eq(DEPT_ID), any(UpdateDepartmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Département", DEPT_ID));

            mockMvc.perform(put("/api/departments/{id}", DEPT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Code déjà utilisé par un autre département → 409 Conflict")
        void shouldReturn409WhenCodeConflict() throws Exception {
            when(departmentService.updateDepartment(eq(DEPT_ID), any(UpdateDepartmentRequest.class)))
                    .thenThrow(new DuplicateResourceException("Département", "code", DEPT_CODE_UPDATED));

            mockMvc.perform(put("/api/departments/{id}", DEPT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("❌ Nom déjà utilisé par un autre département → 409 Conflict")
        void shouldReturn409WhenNameConflict() throws Exception {
            when(departmentService.updateDepartment(eq(DEPT_ID), any(UpdateDepartmentRequest.class)))
                    .thenThrow(new DuplicateResourceException("Département", "nom", DEPT_NAME_UPDATED));

            mockMvc.perform(put("/api/departments/{id}", DEPT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("❌ Validation échouée (code vide) → 400 Bad Request")
        void shouldReturn400WhenUpdateCodeIsBlank() throws Exception {
            UpdateDepartmentRequest invalidRequest = new UpdateDepartmentRequest("", DEPT_NAME);

            mockMvc.perform(put("/api/departments/{id}", DEPT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    // ============================================================
    // TESTS : PATCH /api/departments/{id} (Mise à jour partielle)
    // ============================================================

    @Nested
    @DisplayName("PATCH /api/departments/{id} - Mise à jour partielle")
    class PatchDepartmentTests {

        @Test
        @DisplayName("✅ Mise à jour partielle réussie (seul le code change) → 200 OK")
        void shouldPatchDepartmentSuccessfully() throws Exception {
            DepartmentResponse patchedResponse = new DepartmentResponse(
                    DEPT_ID, DEPT_CODE_UPDATED, DEPT_NAME,
                    Instant.now(), "system", Instant.now(), "system", false
            );

            when(departmentService.patchDepartment(eq(DEPT_ID), any(PatchDepartmentRequest.class)))
                    .thenReturn(patchedResponse);

            mockMvc.perform(patch("/api/departments/{id}", DEPT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.code").value(DEPT_CODE_UPDATED))
                    .andExpect(jsonPath("$.data.name").value(DEPT_NAME)); // inchangé
        }

        @Test
        @DisplayName("❌ Département inexistant → 404 Not Found")
        void shouldReturn404WhenPatchingNonExistentDepartment() throws Exception {
            when(departmentService.patchDepartment(eq(DEPT_ID), any(PatchDepartmentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Département", DEPT_ID));

            mockMvc.perform(patch("/api/departments/{id}", DEPT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Code déjà utilisé par un autre département → 409 Conflict")
        void shouldReturn409WhenPatchCodeConflict() throws Exception {
            when(departmentService.patchDepartment(eq(DEPT_ID), any(PatchDepartmentRequest.class)))
                    .thenThrow(new DuplicateResourceException("Département", "code", DEPT_CODE_UPDATED));

            mockMvc.perform(patch("/api/departments/{id}", DEPT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    // ============================================================
    // TESTS : DELETE /api/departments/{id} (Suppression logique)
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/departments/{id} - Suppression logique")
    class DeleteDepartmentTests {

        @Test
        @DisplayName("✅ Suppression réussie → 204 No Content")
        void shouldDeleteDepartmentSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/departments/{id}", DEPT_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

            verify(departmentService).deleteDepartment(DEPT_ID);
        }

        @Test
        @DisplayName("❌ Département inexistant → 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentDepartment() throws Exception {
            doThrow(new ResourceNotFoundException("Département", DEPT_ID))
                    .when(departmentService).deleteDepartment(DEPT_ID);

            mockMvc.perform(delete("/api/departments/{id}", DEPT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }
    }

    // ============================================================
    // Configuration de test (fournit les beans manquants)
    // ============================================================

    @Configuration
    static class TestConfig {
        @Bean
        public AuditorAware<String> auditorProvider() {
            return () -> Optional.of("test");
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}