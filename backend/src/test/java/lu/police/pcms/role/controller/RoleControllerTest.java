package lu.police.pcms.role.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.role.dto.CreateRoleRequest;
import lu.police.pcms.role.dto.PatchRoleRequest;
import lu.police.pcms.role.dto.RoleResponse;
import lu.police.pcms.role.dto.UpdateRoleRequest;
import lu.police.pcms.role.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
 * Tests du contrôleur {@link RoleController}.
 *
 * <p>
 * Utilise {@link SpringBootTest} pour charger tout le contexte,
 * mais {@code RoleService} est mocké avec {@link MockitoBean}.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(RoleControllerTest.TestConfig.class)   // Importe la configuration de test
@DisplayName("Tests du contrôleur RoleController")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    // ============================================================
    // DONNÉES DE TEST
    // ============================================================

    private static final Long ROLE_ID = 1L;
    private static final String ROLE_NAME = "ROLE_ADMIN";
    private static final String ROLE_DESC = "Administrateur";
    private static final String ROLE_NAME_UPDATED = "ROLE_SUPER_ADMIN";
    private static final String ROLE_DESC_UPDATED = "Super Administrateur";

    private CreateRoleRequest createRequest;
    private UpdateRoleRequest updateRequest;
    private PatchRoleRequest patchRequest;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateRoleRequest(ROLE_NAME, ROLE_DESC);
        updateRequest = new UpdateRoleRequest(ROLE_NAME_UPDATED, ROLE_DESC_UPDATED);
        patchRequest = new PatchRoleRequest(ROLE_NAME_UPDATED, null);

        roleResponse = new RoleResponse(
                ROLE_ID,
                ROLE_NAME,
                ROLE_DESC,
                Instant.now(),
                "system",
                Instant.now(),
                "system",
                false
        );
    }

    // ============================================================
    // TESTS : POST /api/roles (Création)
    // ============================================================

    @Nested
    @DisplayName("POST /api/roles - Création d'un rôle")
    class CreateRoleTests {

        @Test
        @DisplayName("✅ Création réussie → 201 Created")
        void shouldCreateRoleSuccessfully() throws Exception {
            when(roleService.createRole(any(CreateRoleRequest.class)))
                    .thenReturn(roleResponse);

            mockMvc.perform(post("/api/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Rôle créé avec succès"))
                    .andExpect(jsonPath("$.data.id").value(ROLE_ID))
                    .andExpect(jsonPath("$.data.name").value(ROLE_NAME))
                    .andExpect(jsonPath("$.data.description").value(ROLE_DESC));

            verify(roleService).createRole(any(CreateRoleRequest.class));
        }

        @Test
        @DisplayName("❌ Nom déjà existant → 409 Conflict")
        void shouldReturn409WhenNameAlreadyExists() throws Exception {
            when(roleService.createRole(any(CreateRoleRequest.class)))
                    .thenThrow(new DuplicateResourceException("Rôle", "nom", ROLE_NAME));

            mockMvc.perform(post("/api/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value(containsString("existe déjà")));
        }

        @Test
        @DisplayName("❌ Validation échouée (nom vide) → 400 Bad Request")
        void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateRoleRequest invalidRequest = new CreateRoleRequest("", ROLE_DESC);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors.name").exists());  // ✅ plus souple

        verify(roleService, never()).createRole(any(CreateRoleRequest.class));
        }

        @Test
        @DisplayName("❌ Validation échouée (nom sans préfixe ROLE_) → 400 Bad Request")
        void shouldReturn400WhenNameInvalidFormat() throws Exception {
        // "ROLE_a" : longueur 6 (OK pour @Size) mais contient une minuscule (KO pour @Pattern)
        CreateRoleRequest invalidRequest = new CreateRoleRequest("ROLE_a", ROLE_DESC);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").value(containsString("commencer par 'ROLE_'")));
        }
    }

    // ============================================================
    // TESTS : GET /api/roles (Liste)
    // ============================================================

    @Nested
    @DisplayName("GET /api/roles - Liste des rôles")
    class GetAllRolesTests {

        @Test
        @DisplayName("✅ Récupération de la liste → 200 OK")
        void shouldGetAllRolesSuccessfully() throws Exception {
            RoleResponse role2 = new RoleResponse(
                    2L, "ROLE_USER", "Utilisateur",
                    Instant.now(), "system", Instant.now(), "system", false
            );
            List<RoleResponse> roles = List.of(roleResponse, role2);

            when(roleService.getAllRoles()).thenReturn(roles);

            mockMvc.perform(get("/api/roles"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Rôles récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name").value(ROLE_NAME))
                    .andExpect(jsonPath("$.data[1].name").value("ROLE_USER"));
        }

        @Test
        @DisplayName("✅ Liste vide → 200 OK avec data = []")
        void shouldReturnEmptyListWhenNoRoles() throws Exception {
            when(roleService.getAllRoles()).thenReturn(List.of());

            mockMvc.perform(get("/api/roles"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }

    // ============================================================
    // TESTS : GET /api/roles/{id} (Détail)
    // ============================================================

    @Nested
    @DisplayName("GET /api/roles/{id} - Détail d'un rôle")
    class GetRoleByIdTests {

        @Test
        @DisplayName("✅ Rôle trouvé → 200 OK")
        void shouldGetRoleByIdSuccessfully() throws Exception {
            when(roleService.getRoleById(ROLE_ID)).thenReturn(roleResponse);

            mockMvc.perform(get("/api/roles/{id}", ROLE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(ROLE_ID))
                    .andExpect(jsonPath("$.data.name").value(ROLE_NAME));
        }

        @Test
        @DisplayName("❌ Rôle inexistant → 404 Not Found")
        void shouldReturn404WhenRoleNotFound() throws Exception {
            when(roleService.getRoleById(ROLE_ID))
                    .thenThrow(new ResourceNotFoundException("Rôle", ROLE_ID));

            mockMvc.perform(get("/api/roles/{id}", ROLE_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }

    // ============================================================
    // TESTS : PUT /api/roles/{id} (Mise à jour complète)
    // ============================================================

    @Nested
    @DisplayName("PUT /api/roles/{id} - Mise à jour complète")
    class UpdateRoleTests {

        @Test
        @DisplayName("✅ Mise à jour réussie → 200 OK")
        void shouldUpdateRoleSuccessfully() throws Exception {
            RoleResponse updatedResponse = new RoleResponse(
                    ROLE_ID, ROLE_NAME_UPDATED, ROLE_DESC_UPDATED,
                    Instant.now(), "system", Instant.now(), "system", false
            );

            when(roleService.updateRole(eq(ROLE_ID), any(UpdateRoleRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/roles/{id}", ROLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value(ROLE_NAME_UPDATED))
                    .andExpect(jsonPath("$.data.description").value(ROLE_DESC_UPDATED));
        }

        @Test
        @DisplayName("❌ Rôle inexistant → 404 Not Found")
        void shouldReturn404WhenUpdatingNonExistentRole() throws Exception {
            when(roleService.updateRole(eq(ROLE_ID), any(UpdateRoleRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Rôle", ROLE_ID));

            mockMvc.perform(put("/api/roles/{id}", ROLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Nom déjà utilisé par un autre rôle → 409 Conflict")
        void shouldReturn409WhenNameConflict() throws Exception {
            when(roleService.updateRole(eq(ROLE_ID), any(UpdateRoleRequest.class)))
                    .thenThrow(new DuplicateResourceException("Rôle", "nom", ROLE_NAME_UPDATED));

            mockMvc.perform(put("/api/roles/{id}", ROLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("❌ Validation échouée (nom vide) → 400 Bad Request")
        void shouldReturn400WhenUpdateNameIsBlank() throws Exception {
            UpdateRoleRequest invalidRequest = new UpdateRoleRequest("", ROLE_DESC);

            mockMvc.perform(put("/api/roles/{id}", ROLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    // ============================================================
    // TESTS : PATCH /api/roles/{id} (Mise à jour partielle)
    // ============================================================

    @Nested
    @DisplayName("PATCH /api/roles/{id} - Mise à jour partielle")
    class PatchRoleTests {

        @Test
        @DisplayName("✅ Mise à jour partielle réussie → 200 OK")
        void shouldPatchRoleSuccessfully() throws Exception {
            RoleResponse patchedResponse = new RoleResponse(
                    ROLE_ID, ROLE_NAME_UPDATED, ROLE_DESC,
                    Instant.now(), "system", Instant.now(), "system", false
            );

            when(roleService.patchRole(eq(ROLE_ID), any(PatchRoleRequest.class)))
                    .thenReturn(patchedResponse);

            mockMvc.perform(patch("/api/roles/{id}", ROLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value(ROLE_NAME_UPDATED))
                    .andExpect(jsonPath("$.data.description").value(ROLE_DESC));
        }

        @Test
        @DisplayName("❌ Rôle inexistant → 404 Not Found")
        void shouldReturn404WhenPatchingNonExistentRole() throws Exception {
            when(roleService.patchRole(eq(ROLE_ID), any(PatchRoleRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Rôle", ROLE_ID));

            mockMvc.perform(patch("/api/roles/{id}", ROLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ============================================================
    // TESTS : DELETE /api/roles/{id} (Suppression logique)
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/roles/{id} - Suppression logique")
    class DeleteRoleTests {

        @Test
        @DisplayName("✅ Suppression réussie → 204 No Content")
        void shouldDeleteRoleSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/roles/{id}", ROLE_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

            verify(roleService).deleteRole(ROLE_ID);
        }

        @Test
        @DisplayName("❌ Rôle inexistant → 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentRole() throws Exception {
            doThrow(new ResourceNotFoundException("Rôle", ROLE_ID))
                    .when(roleService).deleteRole(ROLE_ID);

            mockMvc.perform(delete("/api/roles/{id}", ROLE_ID))
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
            // Crée un ObjectMapper et enregistre le module JavaTime pour gérer Instant, LocalDate, etc.
            return new ObjectMapper()
                    .registerModule(new JavaTimeModule()); 
        }
    }
}