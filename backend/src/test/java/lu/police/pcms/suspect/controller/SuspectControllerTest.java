package lu.police.pcms.suspect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.suspect.dto.CreateSuspectRequest;
import lu.police.pcms.suspect.dto.PatchSuspectRequest;
import lu.police.pcms.suspect.dto.SuspectResponse;
import lu.police.pcms.suspect.dto.UpdateSuspectRequest;
import lu.police.pcms.suspect.service.SuspectService;
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

import java.time.LocalDate;
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
 * Tests d'intégration du contrôleur SuspectController.
 *
 * <p>
 * Ce test vérifie les 7 endpoints REST de gestion des suspects
 * en utilisant MockMvc avec un contexte Spring Boot complet.
 * Le service est mocké pour isoler la couche contrôleur.
 * </p>
 *
 * <p>
 * Points de vigilance spécifiques à ce module :
 * </p>
 * <ul>
 *     <li>La contrainte d'unicité porte sur le triplet
 *         (dossier, prénom, nom).</li>
 *     <li>Le champ {@code caseFileId} est immuable après création.</li>
 *     <li>Les champs {@code birthDate}, {@code nationality} et
 *         {@code notes} sont optionnels.</li>
 *     <li>Pas d'endpoint de filtrage par utilisateur.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(SuspectControllerTest.TestConfig.class)
@DisplayName("Tests du contrôleur SuspectController")
class SuspectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper local avec JavaTimeModule pour sérialiser
     * correctement les LocalDate et Instant.
     */
    private ObjectMapper objectMapper;

    @MockitoBean
    private SuspectService suspectService;


    // ============================================================
    // CONSTANTES DE TEST
    // ============================================================

    private static final Long SUSPECT_ID = 1L;
    private static final Long CASE_FILE_ID = 10L;
    private static final String FIRST_NAME = "Jean";
    private static final String LAST_NAME = "Dupont";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1985, 3, 15);
    private static final String NATIONALITY = "Française";
    private static final String NOTES = "Suspect principal dans l'affaire.";

    private static final String UPDATED_FIRST_NAME = "Pierre";
    private static final String UPDATED_LAST_NAME = "Martin";
    private static final String UPDATED_NATIONALITY = "Belge";

    private static final Long CASE_FILE_ID_2 = 20L;


    // ============================================================
    // OBJETS DE TEST
    // ============================================================

    private CreateSuspectRequest createRequest;
    private UpdateSuspectRequest updateRequest;
    private PatchSuspectRequest patchRequest;
    private SuspectResponse suspectResponse;


    // ============================================================
    // INITIALISATION
    // ============================================================

    @BeforeEach
    void setUp() {

        // ObjectMapper local avec support JSR-310
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Création avec tous les champs
        createRequest = new CreateSuspectRequest(
                CASE_FILE_ID, FIRST_NAME, LAST_NAME,
                BIRTH_DATE, NATIONALITY, NOTES
        );

        // Mise à jour complète (pas de caseFileId)
        updateRequest = new UpdateSuspectRequest(
                UPDATED_FIRST_NAME, UPDATED_LAST_NAME,
                LocalDate.of(1990, 7, 20), UPDATED_NATIONALITY, "Notes mises à jour"
        );

        // Mise à jour partielle (seul le nom change)
        patchRequest = new PatchSuspectRequest(
                null, UPDATED_LAST_NAME, null, null, null
        );

        // Réponse de référence
        suspectResponse = buildResponse(
                SUSPECT_ID, CASE_FILE_ID, FIRST_NAME, LAST_NAME,
                BIRTH_DATE, NATIONALITY, NOTES
        );
    }


    // ============================================================
    // UTILITAIRES
    // ============================================================

    /**
     * Construit un DTO de réponse pour les tests via setters.
     */
    private SuspectResponse buildResponse(
            Long id, Long caseFileId,
            String firstName, String lastName,
            LocalDate birthDate, String nationality, String notes) {

        SuspectResponse r = new SuspectResponse();
        r.setId(id);
        r.setCaseFileId(caseFileId);
        r.setFirstName(firstName);
        r.setLastName(lastName);
        r.setBirthDate(birthDate);
        r.setNationality(nationality);
        r.setNotes(notes);
        r.setCreatedAt(Instant.now());
        r.setCreatedBy("system");
        r.setUpdatedAt(Instant.now());
        r.setUpdatedBy("system");
        return r;
    }


    // ============================================================
    // TESTS : POST /api/suspects (Création)
    // ============================================================

    /**
     * Tests de l'endpoint POST /api/suspects.
     *
     * <p>
     * Vérifie la création réussie, les rejets pour dossier
     * introuvable, doublon, et validations des champs obligatoires.
     * </p>
     */
    @Nested
    @DisplayName("POST /api/suspects - Création d'un suspect")
    class CreateSuspectTests {

        @Test
        @DisplayName("✅ Création réussie → 201 Created")
        void shouldCreateSuspectSuccessfully() throws Exception {

            when(suspectService.createSuspect(any(CreateSuspectRequest.class)))
                    .thenReturn(suspectResponse);

            mockMvc.perform(
                            post("/api/suspects")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Suspect créé avec succès"))
                    .andExpect(jsonPath("$.data.id").value(SUSPECT_ID))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data.firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.data.lastName").value(LAST_NAME))
                    .andExpect(jsonPath("$.data.nationality").value(NATIONALITY))
                    .andExpect(jsonPath("$.data.notes").value(NOTES));

            verify(suspectService).createSuspect(any(CreateSuspectRequest.class));
        }

        @Test
        @DisplayName("❌ Dossier inexistant → 404 Not Found")
        void shouldReturn404WhenCaseFileNotFound() throws Exception {

            when(suspectService.createSuspect(any(CreateSuspectRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Dossier", CASE_FILE_ID));

            mockMvc.perform(
                            post("/api/suspects")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Dossier introuvable")));
        }

        @Test
        @DisplayName("❌ Suspect déjà existant dans le dossier → 409 Conflict")
        void shouldReturn409WhenSuspectAlreadyExists() throws Exception {

            when(suspectService.createSuspect(any(CreateSuspectRequest.class)))
                    .thenThrow(new DuplicateResourceException(
                            "Suspect",
                            "nom/prénom",
                            FIRST_NAME + " " + LAST_NAME
                    ));

            mockMvc.perform(
                            post("/api/suspects")
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

            CreateSuspectRequest invalid = new CreateSuspectRequest(
                    null, FIRST_NAME, LAST_NAME, BIRTH_DATE, NATIONALITY, NOTES
            );

            mockMvc.perform(
                            post("/api/suspects")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.caseFileId").exists());

            verify(suspectService, never()).createSuspect(any());
        }

        @Test
        @DisplayName("❌ Validation échouée (firstName vide) → 400 Bad Request")
        void shouldReturn400WhenFirstNameIsBlank() throws Exception {

            CreateSuspectRequest invalid = new CreateSuspectRequest(
                    CASE_FILE_ID, "", LAST_NAME, BIRTH_DATE, NATIONALITY, NOTES
            );

            mockMvc.perform(
                            post("/api/suspects")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").exists());

            verify(suspectService, never()).createSuspect(any());
        }

        @Test
        @DisplayName("❌ Validation échouée (lastName vide) → 400 Bad Request")
        void shouldReturn400WhenLastNameIsBlank() throws Exception {

            CreateSuspectRequest invalid = new CreateSuspectRequest(
                    CASE_FILE_ID, FIRST_NAME, "", BIRTH_DATE, NATIONALITY, NOTES
            );

            mockMvc.perform(
                            post("/api/suspects")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.lastName").exists());

            verify(suspectService, never()).createSuspect(any());
        }
    }


    // ============================================================
    // TESTS : GET /api/suspects (Liste)
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/suspects.
     */
    @Nested
    @DisplayName("GET /api/suspects - Liste des suspects")
    class GetAllSuspectsTests {

        @Test
        @DisplayName("✅ Récupération de la liste → 200 OK")
        void shouldGetAllSuspectsSuccessfully() throws Exception {

            SuspectResponse suspect2 = buildResponse(
                    2L, CASE_FILE_ID_2, "Alice", "Bernard",
                    null, null, null
            );

            when(suspectService.getAllSuspects())
                    .thenReturn(List.of(suspectResponse, suspect2));

            mockMvc.perform(get("/api/suspects"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Suspects récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.data[1].firstName").value("Alice"));
        }

        @Test
        @DisplayName("✅ Liste vide → 200 OK avec data = []")
        void shouldReturnEmptyListWhenNoSuspects() throws Exception {

            when(suspectService.getAllSuspects()).thenReturn(List.of());

            mockMvc.perform(get("/api/suspects"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }


    // ============================================================
    // TESTS : GET /api/suspects/{id} (Détail)
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/suspects/{id}.
     */
    @Nested
    @DisplayName("GET /api/suspects/{id} - Détail d'un suspect")
    class GetSuspectByIdTests {

        @Test
        @DisplayName("✅ Suspect trouvé → 200 OK")
        void shouldGetSuspectByIdSuccessfully() throws Exception {

            when(suspectService.getSuspectById(SUSPECT_ID))
                    .thenReturn(suspectResponse);

            mockMvc.perform(get("/api/suspects/{id}", SUSPECT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Suspect récupéré avec succès"))
                    .andExpect(jsonPath("$.data.id").value(SUSPECT_ID))
                    .andExpect(jsonPath("$.data.firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.data.lastName").value(LAST_NAME))
                    .andExpect(jsonPath("$.data.birthDate").value("1985-03-15"))
                    .andExpect(jsonPath("$.data.nationality").value(NATIONALITY));
        }

        @Test
        @DisplayName("❌ Suspect inexistant → 404 Not Found")
        void shouldReturn404WhenSuspectNotFound() throws Exception {

            when(suspectService.getSuspectById(SUSPECT_ID))
                    .thenThrow(new ResourceNotFoundException("Suspect", SUSPECT_ID));

            mockMvc.perform(get("/api/suspects/{id}", SUSPECT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }


    // ============================================================
    // TESTS : GET /api/suspects/case/{caseFileId}
    // ============================================================

    /**
     * Tests de l'endpoint GET /api/suspects/case/{caseFileId}.
     *
     * <p>
     * Vérifie la récupération des suspects filtrés par dossier.
     * </p>
     */
    @Nested
    @DisplayName("GET /api/suspects/case/{caseFileId} - Suspects d'un dossier")
    class GetSuspectsByCaseFileTests {

        @Test
        @DisplayName("✅ Suspects d'un dossier trouvés → 200 OK")
        void shouldGetSuspectsByCaseFileSuccessfully() throws Exception {

            when(suspectService.getSuspectsByCaseFile(CASE_FILE_ID))
                    .thenReturn(List.of(suspectResponse));

            mockMvc.perform(get("/api/suspects/case/{caseFileId}", CASE_FILE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Suspects du dossier récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].caseFileId").value(CASE_FILE_ID))
                    .andExpect(jsonPath("$.data[0].lastName").value(LAST_NAME));
        }

        @Test
        @DisplayName("❌ Dossier inexistant → 404 Not Found")
        void shouldReturn404WhenCaseFileNotFound() throws Exception {

            when(suspectService.getSuspectsByCaseFile(CASE_FILE_ID))
                    .thenThrow(new ResourceNotFoundException("Dossier", CASE_FILE_ID));

            mockMvc.perform(get("/api/suspects/case/{caseFileId}", CASE_FILE_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Dossier introuvable")));
        }
    }


    // ============================================================
    // TESTS : PUT /api/suspects/{id} (Mise à jour complète)
    // ============================================================

    /**
     * Tests de l'endpoint PUT /api/suspects/{id}.
     *
     * <p>
     * Le dossier est immuable. Seuls les champs d'identité
     * et les champs optionnels sont modifiables.
     * </p>
     */
    @Nested
    @DisplayName("PUT /api/suspects/{id} - Mise à jour complète")
    class UpdateSuspectTests {

        @Test
        @DisplayName("✅ Mise à jour réussie → 200 OK")
        void shouldUpdateSuspectSuccessfully() throws Exception {

            SuspectResponse updated = buildResponse(
                    SUSPECT_ID, CASE_FILE_ID,
                    UPDATED_FIRST_NAME, UPDATED_LAST_NAME,
                    LocalDate.of(1990, 7, 20), UPDATED_NATIONALITY, "Notes mises à jour"
            );

            when(suspectService.updateSuspect(eq(SUSPECT_ID), any(UpdateSuspectRequest.class)))
                    .thenReturn(updated);

            mockMvc.perform(
                            put("/api/suspects/{id}", SUSPECT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Suspect mis à jour avec succès"))
                    .andExpect(jsonPath("$.data.id").value(SUSPECT_ID))
                    .andExpect(jsonPath("$.data.firstName").value(UPDATED_FIRST_NAME))
                    .andExpect(jsonPath("$.data.lastName").value(UPDATED_LAST_NAME))
                    .andExpect(jsonPath("$.data.nationality").value(UPDATED_NATIONALITY))
                    // caseFileId reste inchangé
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID));
        }

        @Test
        @DisplayName("❌ Suspect inexistant → 404 Not Found")
        void shouldReturn404WhenUpdatingNonExistentSuspect() throws Exception {

            when(suspectService.updateSuspect(eq(SUSPECT_ID), any(UpdateSuspectRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Suspect", SUSPECT_ID));

            mockMvc.perform(
                            put("/api/suspects/{id}", SUSPECT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ Doublon sur le même dossier → 409 Conflict")
        void shouldReturn409WhenDuplicateOnUpdate() throws Exception {

            when(suspectService.updateSuspect(eq(SUSPECT_ID), any(UpdateSuspectRequest.class)))
                    .thenThrow(new DuplicateResourceException(
                            "Suspect", "nom/prénom", UPDATED_FIRST_NAME + " " + UPDATED_LAST_NAME
                    ));

            mockMvc.perform(
                            put("/api/suspects/{id}", SUSPECT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(containsString("existe déjà")));
        }

        @Test
        @DisplayName("❌ Validation échouée (firstName vide) → 400 Bad Request")
        void shouldReturn400WhenFirstNameIsBlank() throws Exception {

            UpdateSuspectRequest invalid = new UpdateSuspectRequest(
                    "", UPDATED_LAST_NAME, null, null, null
            );

            mockMvc.perform(
                            put("/api/suspects/{id}", SUSPECT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").exists());

            verify(suspectService, never()).updateSuspect(eq(SUSPECT_ID), any());
        }
    }


    // ============================================================
    // TESTS : PATCH /api/suspects/{id} (Mise à jour partielle)
    // ============================================================

    /**
     * Tests de l'endpoint PATCH /api/suspects/{id}.
     *
     * <p>
     * Tous les champs sont optionnels. Le dossier reste immuable.
     * </p>
     */
    @Nested
    @DisplayName("PATCH /api/suspects/{id} - Mise à jour partielle")
    class PatchSuspectTests {

        @Test
        @DisplayName("✅ Mise à jour partielle réussie (seul le nom change) → 200 OK")
        void shouldPatchSuspectSuccessfully() throws Exception {

            SuspectResponse patched = buildResponse(
                    SUSPECT_ID, CASE_FILE_ID,
                    FIRST_NAME, UPDATED_LAST_NAME,
                    BIRTH_DATE, NATIONALITY, NOTES
            );

            when(suspectService.patchSuspect(eq(SUSPECT_ID), any(PatchSuspectRequest.class)))
                    .thenReturn(patched);

            mockMvc.perform(
                            patch("/api/suspects/{id}", SUSPECT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(patchRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Suspect partiellement mis à jour"))
                    .andExpect(jsonPath("$.data.lastName").value(UPDATED_LAST_NAME))
                    .andExpect(jsonPath("$.data.firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.data.caseFileId").value(CASE_FILE_ID));
        }

        @Test
        @DisplayName("❌ Suspect inexistant → 404 Not Found")
        void shouldReturn404WhenPatchingNonExistentSuspect() throws Exception {

            when(suspectService.patchSuspect(eq(SUSPECT_ID), any(PatchSuspectRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Suspect", SUSPECT_ID));

            mockMvc.perform(
                            patch("/api/suspects/{id}", SUSPECT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(patchRequest))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }


    // ============================================================
    // TESTS : DELETE /api/suspects/{id} (Suppression logique)
    // ============================================================

    /**
     * Tests de l'endpoint DELETE /api/suspects/{id}.
     *
     * <p>
     * La suppression est logique (deleted = true).
     * Le contrôleur retourne 204 No Content.
     * </p>
     */
    @Nested
    @DisplayName("DELETE /api/suspects/{id} - Suppression logique")
    class DeleteSuspectTests {

        @Test
        @DisplayName("✅ Suppression réussie → 204 No Content")
        void shouldDeleteSuspectSuccessfully() throws Exception {

            mockMvc.perform(delete("/api/suspects/{id}", SUSPECT_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

            verify(suspectService).deleteSuspect(SUSPECT_ID);
        }

        @Test
        @DisplayName("❌ Suspect inexistant → 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentSuspect() throws Exception {

            doThrow(new ResourceNotFoundException("Suspect", SUSPECT_ID))
                    .when(suspectService).deleteSuspect(SUSPECT_ID);

            mockMvc.perform(delete("/api/suspects/{id}", SUSPECT_ID))
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