package lu.police.pcms.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.user.dto.CreateUserRequest;
import lu.police.pcms.user.dto.PatchUserRequest;
import lu.police.pcms.user.dto.UpdateUserRequest;
import lu.police.pcms.user.dto.UserResponse;
import lu.police.pcms.user.service.UserService;
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
 * Tests du contrôleur {@link UserController}.
 *
 * <p>
 * Utilise {@link SpringBootTest} pour charger tout le contexte,
 * mais {@code UserService} est mocké avec {@link MockitoBean}.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(UserControllerTest.TestConfig.class)
@DisplayName("Tests du contrôleur UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // ============================================================
    // DONNÉES DE TEST
    // ============================================================

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "john.doe@pcms.lu";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Doe";
    private static final String USER_PASSWORD = "password123";
    private static final Long ROLE_ID = 1L;
    private static final Long DEPT_ID = 1L;
    private static final String UPDATED_EMAIL = "jane.smith@pcms.lu";
    private static final String UPDATED_FIRST_NAME = "Jane";
    private static final String UPDATED_LAST_NAME = "Smith";

    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private PatchUserRequest patchRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateUserRequest(
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_EMAIL,
                USER_PASSWORD,
                true,
                ROLE_ID,
                DEPT_ID
        );

        updateRequest = new UpdateUserRequest(
                UPDATED_FIRST_NAME,
                UPDATED_LAST_NAME,
                UPDATED_EMAIL,
                true,
                2L,
                2L
        );

        patchRequest = new PatchUserRequest(
                UPDATED_FIRST_NAME,
                null,
                UPDATED_EMAIL,
                null,
                null,
                null
        );

        userResponse = new UserResponse(
                USER_ID,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_EMAIL,
                true,
                ROLE_ID,
                "ROLE_USER",
                DEPT_ID,
                "IT",
                "Informatique",
                Instant.now(),
                "system",
                Instant.now(),
                "system",
                false
        );
    }

    // ============================================================
    // TESTS : POST /api/users (Création)
    // ============================================================

    @Nested
    @DisplayName("POST /api/users - Création d'un utilisateur")
    class CreateUserTests {

        @Test
        @DisplayName("✅ Création réussie → 201 Created")
        void shouldCreateUserSuccessfully() throws Exception {
            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenReturn(userResponse);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Utilisateur créé avec succès"))
                    .andExpect(jsonPath("$.data.id").value(USER_ID))
                    .andExpect(jsonPath("$.data.email").value(USER_EMAIL))
                    .andExpect(jsonPath("$.data.firstName").value(USER_FIRST_NAME))
                    .andExpect(jsonPath("$.data.lastName").value(USER_LAST_NAME));

            verify(userService).createUser(any(CreateUserRequest.class));
        }

        @Test
        @DisplayName("❌ Email déjà existant → 409 Conflict")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenThrow(new DuplicateResourceException("Utilisateur", "email", USER_EMAIL));

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value(containsString("existe déjà")));
        }

        @Test
        @DisplayName("❌ Validation échouée (email vide) → 400 Bad Request")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            CreateUserRequest invalidRequest = new CreateUserRequest(
                    USER_FIRST_NAME,
                    USER_LAST_NAME,
                    "",
                    USER_PASSWORD,
                    true,
                    ROLE_ID,
                    DEPT_ID
            );

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.email").exists());

            verify(userService, never()).createUser(any(CreateUserRequest.class));
        }

        @Test
        @DisplayName("❌ Validation échouée (email invalide) → 400 Bad Request")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            CreateUserRequest invalidRequest = new CreateUserRequest(
                    USER_FIRST_NAME,
                    USER_LAST_NAME,
                    "invalid-email",
                    USER_PASSWORD,
                    true,
                    ROLE_ID,
                    DEPT_ID
            );

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.email").value(containsString("doit être valide")));
        }

        @Test
        @DisplayName("❌ Validation échouée (mot de passe trop court) → 400 Bad Request")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            CreateUserRequest invalidRequest = new CreateUserRequest(
                    USER_FIRST_NAME,
                    USER_LAST_NAME,
                    USER_EMAIL,
                    "123",  // moins de 8 caractères
                    true,
                    ROLE_ID,
                    DEPT_ID
            );

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.password").value(containsString("8 et 255 caractères")));
        }

        @Test
        @DisplayName("❌ Validation échouée (roleId null) → 400 Bad Request")
        void shouldReturn400WhenRoleIdNull() throws Exception {
            CreateUserRequest invalidRequest = new CreateUserRequest(
                    USER_FIRST_NAME,
                    USER_LAST_NAME,
                    USER_EMAIL,
                    USER_PASSWORD,
                    true,
                    null,
                    DEPT_ID
            );

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.roleId").value(containsString("obligatoire")));
        }
    }

    // ============================================================
    // TESTS : GET /api/users (Liste)
    // ============================================================

    @Nested
    @DisplayName("GET /api/users - Liste des utilisateurs")
    class GetAllUsersTests {

        @Test
        @DisplayName("✅ Récupération de la liste → 200 OK")
        void shouldGetAllUsersSuccessfully() throws Exception {
            UserResponse user2 = new UserResponse(
                    2L,
                    "Alice",
                    "Doe",
                    "alice@pcms.lu",
                    true,
                    2L,
                    "ROLE_USER",
                    2L,
                    "HR",
                    "Ressources Humaines",
                    Instant.now(),
                    "system",
                    Instant.now(),
                    "system",
                    false
            );
            List<UserResponse> users = List.of(userResponse, user2);

            when(userService.getAllUsers()).thenReturn(users);

            mockMvc.perform(get("/api/users"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Utilisateurs récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].email").value(USER_EMAIL))
                    .andExpect(jsonPath("$.data[1].email").value("alice@pcms.lu"));
        }

        @Test
        @DisplayName("✅ Liste vide → 200 OK avec data = []")
        void shouldReturnEmptyListWhenNoUsers() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/users"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }

    // ============================================================
    // TESTS : GET /api/users/{id} (Détail)
    // ============================================================

    @Nested
    @DisplayName("GET /api/users/{id} - Détail d'un utilisateur")
    class GetUserByIdTests {

        @Test
        @DisplayName("✅ Utilisateur trouvé → 200 OK")
        void shouldGetUserByIdSuccessfully() throws Exception {
            when(userService.getUserById(USER_ID)).thenReturn(userResponse);

            mockMvc.perform(get("/api/users/{id}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(USER_ID))
                    .andExpect(jsonPath("$.data.email").value(USER_EMAIL));
        }

        @Test
        @DisplayName("❌ Utilisateur inexistant → 404 Not Found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(userService.getUserById(USER_ID))
                    .thenThrow(new ResourceNotFoundException("Utilisateur", USER_ID));

            mockMvc.perform(get("/api/users/{id}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }

    // ============================================================
    // TESTS : PUT /api/users/{id} (Mise à jour complète)
    // ============================================================

    @Nested
    @DisplayName("PUT /api/users/{id} - Mise à jour complète")
    class UpdateUserTests {

        @Test
        @DisplayName("✅ Mise à jour réussie → 200 OK")
        void shouldUpdateUserSuccessfully() throws Exception {
            UserResponse updatedResponse = new UserResponse(
                    USER_ID,
                    UPDATED_FIRST_NAME,
                    UPDATED_LAST_NAME,
                    UPDATED_EMAIL,
                    true,
                    2L,
                    "ROLE_ADMIN",
                    2L,
                    "HR",
                    "Ressources Humaines",
                    Instant.now(),
                    "system",
                    Instant.now(),
                    "system",
                    false
            );

            when(userService.updateUser(eq(USER_ID), any(UpdateUserRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value(UPDATED_FIRST_NAME))
                    .andExpect(jsonPath("$.data.lastName").value(UPDATED_LAST_NAME))
                    .andExpect(jsonPath("$.data.email").value(UPDATED_EMAIL));
        }

        @Test
        @DisplayName("❌ Utilisateur inexistant → 404 Not Found")
        void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
            when(userService.updateUser(eq(USER_ID), any(UpdateUserRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Utilisateur", USER_ID));

            mockMvc.perform(put("/api/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Email déjà utilisé par un autre utilisateur → 409 Conflict")
        void shouldReturn409WhenEmailConflict() throws Exception {
            when(userService.updateUser(eq(USER_ID), any(UpdateUserRequest.class)))
                    .thenThrow(new DuplicateResourceException("Utilisateur", "email", UPDATED_EMAIL));

            mockMvc.perform(put("/api/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("❌ Validation échouée (email vide) → 400 Bad Request")
        void shouldReturn400WhenUpdateEmailIsBlank() throws Exception {
            UpdateUserRequest invalidRequest = new UpdateUserRequest(
                    UPDATED_FIRST_NAME,
                    UPDATED_LAST_NAME,
                    "",
                    true,
                    2L,
                    2L
            );

            mockMvc.perform(put("/api/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    // ============================================================
    // TESTS : PATCH /api/users/{id} (Mise à jour partielle)
    // ============================================================

    @Nested
    @DisplayName("PATCH /api/users/{id} - Mise à jour partielle")
    class PatchUserTests {

        @Test
        @DisplayName("✅ Mise à jour partielle réussie (prénom + email) → 200 OK")
        void shouldPatchUserSuccessfully() throws Exception {
            UserResponse patchedResponse = new UserResponse(
                    USER_ID,
                    UPDATED_FIRST_NAME,
                    USER_LAST_NAME,  // nom inchangé
                    UPDATED_EMAIL,
                    true,
                    ROLE_ID,
                    "ROLE_USER",
                    DEPT_ID,
                    "IT",
                    "Informatique",
                    Instant.now(),
                    "system",
                    Instant.now(),
                    "system",
                    false
            );

            when(userService.patchUser(eq(USER_ID), any(PatchUserRequest.class)))
                    .thenReturn(patchedResponse);

            mockMvc.perform(patch("/api/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.firstName").value(UPDATED_FIRST_NAME))
                    .andExpect(jsonPath("$.data.email").value(UPDATED_EMAIL))
                    .andExpect(jsonPath("$.data.lastName").value(USER_LAST_NAME)); // inchangé
        }

        @Test
        @DisplayName("❌ Utilisateur inexistant → 404 Not Found")
        void shouldReturn404WhenPatchingNonExistentUser() throws Exception {
            when(userService.patchUser(eq(USER_ID), any(PatchUserRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Utilisateur", USER_ID));

            mockMvc.perform(patch("/api/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Email déjà utilisé par un autre utilisateur → 409 Conflict")
        void shouldReturn409WhenPatchEmailConflict() throws Exception {
            when(userService.patchUser(eq(USER_ID), any(PatchUserRequest.class)))
                    .thenThrow(new DuplicateResourceException("Utilisateur", "email", UPDATED_EMAIL));

            mockMvc.perform(patch("/api/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    // ============================================================
    // TESTS : DELETE /api/users/{id} (Suppression logique)
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/users/{id} - Suppression logique")
    class DeleteUserTests {

        @Test
        @DisplayName("✅ Suppression réussie → 204 No Content")
        void shouldDeleteUserSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/users/{id}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

            verify(userService).deleteUser(USER_ID);
        }

        @Test
        @DisplayName("❌ Utilisateur inexistant → 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
            doThrow(new ResourceNotFoundException("Utilisateur", USER_ID))
                    .when(userService).deleteUser(USER_ID);

            mockMvc.perform(delete("/api/users/{id}", USER_ID))
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