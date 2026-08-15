package lu.police.pcms.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lu.police.pcms.audit.dto.AuditLogResponse;
import lu.police.pcms.audit.dto.CreateAuditLogRequest;
import lu.police.pcms.audit.service.AuditLogService;
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
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(AuditLogControllerTest.TestConfig.class)
@DisplayName("Tests du contrôleur AuditLogController")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditLogService auditLogService;

    // ============================================================
    // DONNÉES DE TEST
    // ============================================================

    private static final Long AUDIT_ID = 1L;
    private static final Long USER_ID = 5L;
    private static final String ACTION = "UPDATE";
    private static final String ENTITY_NAME = "User";
    private static final Long ENTITY_ID = 12L;
    private static final String DETAILS = "Mise à jour du champ email : john.doe@pcms.lu → jane.smith@pcms.lu";
    private static final String IP_ADDRESS = "192.168.1.42";

    private static final Instant DATE_AFTER = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DATE_START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DATE_END   = Instant.parse("2026-12-31T23:59:59Z");

    private CreateAuditLogRequest createRequest;
    private AuditLogResponse auditLogResponse;
    private AuditLogResponse auditLogResponse2;

    @BeforeEach
    void setUp() {
        createRequest = new CreateAuditLogRequest(
                USER_ID, ACTION, ENTITY_NAME, ENTITY_ID, DETAILS, IP_ADDRESS
        );

        auditLogResponse = new AuditLogResponse(
                AUDIT_ID, USER_ID, ACTION, ENTITY_NAME, ENTITY_ID,
                DETAILS, IP_ADDRESS, Instant.now(), "system"
        );

        auditLogResponse2 = new AuditLogResponse(
                2L, 8L, "DELETE", "CaseFile", 42L,
                "Suppression logique du dossier 2026-0042", "10.0.0.15",
                Instant.now(), "admin"
        );
    }

    // ============================================================
    // 1. POST /api/audit-logs (7 tests)
    // ============================================================

    @Nested
    @DisplayName("POST /api/audit-logs - Création d'un log d'audit")
    class CreateAuditLogTests {

        @Test
        @DisplayName("✅ Création réussie → 201 Created")
        void shouldCreateAuditLogSuccessfully() throws Exception {
            when(auditLogService.createAuditLog(any(CreateAuditLogRequest.class)))
                    .thenReturn(auditLogResponse);

            mockMvc.perform(post("/api/audit-logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Log d'audit créé avec succès"))
                    .andExpect(jsonPath("$.data.id").value(AUDIT_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID))
                    .andExpect(jsonPath("$.data.action").value(ACTION))
                    .andExpect(jsonPath("$.data.entityName").value(ENTITY_NAME))
                    .andExpect(jsonPath("$.data.entityId").value(ENTITY_ID))
                    .andExpect(jsonPath("$.data.details").value(DETAILS))
                    .andExpect(jsonPath("$.data.ipAddress").value(IP_ADDRESS));

            verify(auditLogService).createAuditLog(any(CreateAuditLogRequest.class));
        }

        @Test
        @DisplayName("✅ Création sans détails ni IP → 201 Created")
        void shouldCreateAuditLogWithoutOptionalFields() throws Exception {
            CreateAuditLogRequest minimalRequest = new CreateAuditLogRequest(
                    USER_ID, ACTION, ENTITY_NAME, ENTITY_ID, null, null
            );
            AuditLogResponse minimalResponse = new AuditLogResponse(
                    AUDIT_ID, USER_ID, ACTION, ENTITY_NAME, ENTITY_ID,
                    null, null, Instant.now(), "system"
            );

            when(auditLogService.createAuditLog(any(CreateAuditLogRequest.class)))
                    .thenReturn(minimalResponse);

            mockMvc.perform(post("/api/audit-logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(minimalRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.details").doesNotExist())
                    .andExpect(jsonPath("$.data.ipAddress").doesNotExist());

            verify(auditLogService).createAuditLog(any(CreateAuditLogRequest.class));
        }

        @Test
        @DisplayName("❌ Validation échouée (userId null) → 400 Bad Request")
        void shouldReturn400WhenUserIdIsNull() throws Exception {
            CreateAuditLogRequest invalidRequest = new CreateAuditLogRequest(
                    null, ACTION, ENTITY_NAME, ENTITY_ID, DETAILS, IP_ADDRESS
            );

            mockMvc.perform(post("/api/audit-logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.errors.userId").exists());

            verify(auditLogService, never()).createAuditLog(any());
        }

        @Test
        @DisplayName("❌ Validation échouée (action vide) → 400 Bad Request")
        void shouldReturn400WhenActionIsBlank() throws Exception {
            CreateAuditLogRequest invalidRequest = new CreateAuditLogRequest(
                    USER_ID, "", ENTITY_NAME, ENTITY_ID, DETAILS, IP_ADDRESS
            );

            mockMvc.perform(post("/api/audit-logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.action").value(containsString("obligatoire")));

            verify(auditLogService, never()).createAuditLog(any());
        }

        @Test
        @DisplayName("❌ Validation échouée (entityName vide) → 400 Bad Request")
        void shouldReturn400WhenEntityNameIsBlank() throws Exception {
            CreateAuditLogRequest invalidRequest = new CreateAuditLogRequest(
                    USER_ID, ACTION, "", ENTITY_ID, DETAILS, IP_ADDRESS
            );

            mockMvc.perform(post("/api/audit-logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.entityName").value(containsString("obligatoire")));

            verify(auditLogService, never()).createAuditLog(any());
        }

        @Test
        @DisplayName("❌ Validation échouée (entityId null) → 400 Bad Request")
        void shouldReturn400WhenEntityIdIsNull() throws Exception {
            CreateAuditLogRequest invalidRequest = new CreateAuditLogRequest(
                    USER_ID, ACTION, ENTITY_NAME, null, DETAILS, IP_ADDRESS
            );

            mockMvc.perform(post("/api/audit-logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.entityId").exists());

            verify(auditLogService, never()).createAuditLog(any());
        }

        @Test
        @DisplayName("❌ Corps de la requête vide → 400 Bad Request")
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/audit-logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(auditLogService, never()).createAuditLog(any());
        }
    }

    // ============================================================
    // 2. GET /api/audit-logs - Liste paginée (4 tests)
    // ============================================================

    @Nested
    @DisplayName("GET /api/audit-logs - Liste paginée des logs d'audit")
    class GetAllAuditLogsPaginatedTests {

        @Test
        @DisplayName("✅ Liste avec résultats → 200 OK")
        void shouldReturnPaginatedAuditLogs() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse, auditLogResponse2);
            Page<AuditLogResponse> page = new PageImpl<>(logs);

            when(auditLogService.getAuditLogsPaginated(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/audit-logs"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logs d'audit récupérés avec succès"))
                    .andExpect(jsonPath("$.data.content", hasSize(2)))
                    .andExpect(jsonPath("$.data.content[0].action").value(ACTION))
                    .andExpect(jsonPath("$.data.content[0].entityName").value(ENTITY_NAME))
                    .andExpect(jsonPath("$.data.content[1].action").value("DELETE"))
                    .andExpect(jsonPath("$.data.content[1].entityName").value("CaseFile"))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.empty").value(false));
        }

        @Test
        @DisplayName("✅ Liste vide → 200 OK avec content = []")
        void shouldReturnEmptyPageWhenNoAuditLogs() throws Exception {
            Page<AuditLogResponse> emptyPage = new PageImpl<>(List.of());

            when(auditLogService.getAuditLogsPaginated(any(Pageable.class))).thenReturn(emptyPage);

            mockMvc.perform(get("/api/audit-logs"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", empty()))
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.empty").value(true));
        }

        @Test
        @DisplayName("✅ Pagination personnalisée (page=1&size=5) → 200 OK")
        void shouldAcceptCustomPagination() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse);
            Page<AuditLogResponse> page = new PageImpl<>(logs);

            when(auditLogService.getAuditLogsPaginated(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/audit-logs")
                            .param("page", "1")
                            .param("size", "5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(1)));

            verify(auditLogService).getAuditLogsPaginated(any(Pageable.class));
        }

        @Test
        @DisplayName("✅ Tri personnalisé (sort=action,ASC) → 200 OK")
        void shouldAcceptCustomSort() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse, auditLogResponse2);
            Page<AuditLogResponse> page = new PageImpl<>(logs);

            when(auditLogService.getAuditLogsPaginated(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/audit-logs")
                            .param("sort", "action,ASC"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(2)));

            verify(auditLogService).getAuditLogsPaginated(any(Pageable.class));
        }
    }

    // ============================================================
    // 3. GET /api/audit-logs/{id} - Détail (2 tests)
    // ============================================================

    @Nested
    @DisplayName("GET /api/audit-logs/{id} - Détail d'un log d'audit")
    class GetAuditLogByIdTests {

        @Test
        @DisplayName("✅ Log trouvé → 200 OK")
        void shouldGetAuditLogByIdSuccessfully() throws Exception {
            when(auditLogService.getAuditLogById(AUDIT_ID)).thenReturn(auditLogResponse);

            mockMvc.perform(get("/api/audit-logs/{id}", AUDIT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Log d'audit récupéré avec succès"))
                    .andExpect(jsonPath("$.data.id").value(AUDIT_ID))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID))
                    .andExpect(jsonPath("$.data.action").value(ACTION))
                    .andExpect(jsonPath("$.data.entityName").value(ENTITY_NAME))
                    .andExpect(jsonPath("$.data.entityId").value(ENTITY_ID))
                    .andExpect(jsonPath("$.data.details").value(DETAILS))
                    .andExpect(jsonPath("$.data.ipAddress").value(IP_ADDRESS))
                    .andExpect(jsonPath("$.data.createdAt").exists())
                    .andExpect(jsonPath("$.data.createdBy").value("system"));
        }

        @Test
        @DisplayName("❌ Log inexistant → 404 Not Found")
        void shouldReturn404WhenAuditLogNotFound() throws Exception {
            when(auditLogService.getAuditLogById(AUDIT_ID))
                    .thenThrow(new ResourceNotFoundException("Journal d'audit", AUDIT_ID));

            mockMvc.perform(get("/api/audit-logs/{id}", AUDIT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }

    // ============================================================
    // 4. GET /api/audit-logs/user/{userId} - Logs par utilisateur (3 tests)
    // ============================================================

    @Nested
    @DisplayName("GET /api/audit-logs/user/{userId} - Logs par utilisateur")
    class GetAuditLogsByUserTests {

        @Test
        @DisplayName("✅ Utilisateur avec des logs → 200 OK")
        void shouldReturnLogsForUser() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse, auditLogResponse2);

            when(auditLogService.getAuditLogsByUser(USER_ID)).thenReturn(logs);

            mockMvc.perform(get("/api/audit-logs/user/{userId}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logs de l'utilisateur récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].userId").value(USER_ID))
                    .andExpect(jsonPath("$.data[1].userId").value(8L));

            verify(auditLogService).getAuditLogsByUser(USER_ID);
        }

        @Test
        @DisplayName("✅ Utilisateur sans logs → 200 OK avec liste vide")
        void shouldReturnEmptyListWhenUserHasNoLogs() throws Exception {
            when(auditLogService.getAuditLogsByUser(USER_ID)).thenReturn(List.of());

            mockMvc.perform(get("/api/audit-logs/user/{userId}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }

        @Test
        @DisplayName("❌ Utilisateur inexistant → 404 Not Found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(auditLogService.getAuditLogsByUser(USER_ID))
                    .thenThrow(new ResourceNotFoundException("Utilisateur", USER_ID));

            mockMvc.perform(get("/api/audit-logs/user/{userId}", USER_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("introuvable")));
        }
    }

    // ============================================================
    // 5. GET /api/audit-logs/action/{action} - Logs par action (2 tests)
    // ============================================================

    @Nested
    @DisplayName("GET /api/audit-logs/action/{action} - Logs par action")
    class GetAuditLogsByActionTests {

        @Test
        @DisplayName("✅ Action avec des logs → 200 OK")
        void shouldReturnLogsForAction() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse);

            when(auditLogService.getAuditLogsByAction(ACTION)).thenReturn(logs);

            mockMvc.perform(get("/api/audit-logs/action/{action}", ACTION))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logs par action récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].action").value(ACTION));

            verify(auditLogService).getAuditLogsByAction(ACTION);
        }

        @Test
        @DisplayName("✅ Action sans logs → 200 OK avec liste vide")
        void shouldReturnEmptyListWhenActionHasNoLogs() throws Exception {
            when(auditLogService.getAuditLogsByAction("UNKNOWN")).thenReturn(List.of());

            mockMvc.perform(get("/api/audit-logs/action/{action}", "UNKNOWN"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }

    // ============================================================
    // 6. GET /api/audit-logs/entity/{entityName} - Logs par entité (2 tests)
    // ============================================================

    @Nested
    @DisplayName("GET /api/audit-logs/entity/{entityName} - Logs par entité")
    class GetAuditLogsByEntityNameTests {

        @Test
        @DisplayName("✅ Entité avec des logs → 200 OK")
        void shouldReturnLogsForEntity() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse);

            when(auditLogService.getAuditLogsByEntityName(ENTITY_NAME)).thenReturn(logs);

            mockMvc.perform(get("/api/audit-logs/entity/{entityName}", ENTITY_NAME))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logs par entité récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].entityName").value(ENTITY_NAME));

            verify(auditLogService).getAuditLogsByEntityName(ENTITY_NAME);
        }

        @Test
        @DisplayName("✅ Entité sans logs → 200 OK avec liste vide")
        void shouldReturnEmptyListWhenEntityHasNoLogs() throws Exception {
            when(auditLogService.getAuditLogsByEntityName("UnknownEntity")).thenReturn(List.of());

            mockMvc.perform(get("/api/audit-logs/entity/{entityName}", "UnknownEntity"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }

    // ============================================================
    // 7. GET /api/audit-logs/entity/{entityName}/{entityId} - Par enregistrement (2 tests)
    // ============================================================

    @Nested
    @DisplayName("GET /api/audit-logs/entity/{entityName}/{entityId} - Logs par enregistrement")
    class GetAuditLogsByEntityNameAndEntityIdTests {

        @Test
        @DisplayName("✅ Enregistrement avec des logs → 200 OK")
        void shouldReturnLogsForSpecificRecord() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse);

            when(auditLogService.getAuditLogsByEntityNameAndEntityId(ENTITY_NAME, ENTITY_ID))
                    .thenReturn(logs);

            mockMvc.perform(get("/api/audit-logs/entity/{entityName}/{entityId}", ENTITY_NAME, ENTITY_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logs pour l'enregistrement récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].entityName").value(ENTITY_NAME))
                    .andExpect(jsonPath("$.data[0].entityId").value(ENTITY_ID));

            verify(auditLogService).getAuditLogsByEntityNameAndEntityId(ENTITY_NAME, ENTITY_ID);
        }

        @Test
        @DisplayName("✅ Enregistrement sans logs → 200 OK avec liste vide")
        void shouldReturnEmptyListWhenRecordHasNoLogs() throws Exception {
            when(auditLogService.getAuditLogsByEntityNameAndEntityId("UnknownEntity", 999L))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/audit-logs/entity/{entityName}/{entityId}", "UnknownEntity", 999L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }
    }

    // ============================================================
    // 8. GET /api/audit-logs/after/{date} - Logs après une date (3 tests)
    // ============================================================

    @Nested
    @DisplayName("GET /api/audit-logs/after/{date} - Logs après une date")
    class GetAuditLogsAfterTests {

        @Test
        @DisplayName("✅ Logs existants après la date → 200 OK")
        void shouldReturnLogsAfterDate() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse, auditLogResponse2);

            when(auditLogService.getAuditLogsAfter(any(Instant.class))).thenReturn(logs);

            mockMvc.perform(get("/api/audit-logs/after/{date}", DATE_AFTER))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logs après la date récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)));

            verify(auditLogService).getAuditLogsAfter(any(Instant.class));
        }

        @Test
        @DisplayName("✅ Aucun log après la date → 200 OK avec liste vide")
        void shouldReturnEmptyListWhenNoLogsAfterDate() throws Exception {
            when(auditLogService.getAuditLogsAfter(any(Instant.class))).thenReturn(List.of());

            mockMvc.perform(get("/api/audit-logs/after/{date}", DATE_AFTER))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }

        @Test
        @DisplayName("❌ Date invalide → 400 Bad Request")
        void shouldReturn400WhenDateIsInvalid() throws Exception {
            mockMvc.perform(get("/api/audit-logs/after/{date}", "not-a-date"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(auditLogService, never()).getAuditLogsAfter(any());
        }
    }

    // ============================================================
    // 9. GET /api/audit-logs/between?start=...&end=... - Entre deux dates (4 tests)
    // ============================================================

    @Nested
    @DisplayName("GET /api/audit-logs/between - Logs entre deux dates")
    class GetAuditLogsBetweenTests {

        @Test
        @DisplayName("✅ Logs existants entre les dates → 200 OK")
        void shouldReturnLogsBetweenDates() throws Exception {
            List<AuditLogResponse> logs = List.of(auditLogResponse, auditLogResponse2);

            when(auditLogService.getAuditLogsBetween(any(Instant.class), any(Instant.class)))
                    .thenReturn(logs);

            mockMvc.perform(get("/api/audit-logs/between")
                            .param("start", DATE_START.toString())
                            .param("end", DATE_END.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logs entre les dates récupérés avec succès"))
                    .andExpect(jsonPath("$.data", hasSize(2)));

            verify(auditLogService).getAuditLogsBetween(any(Instant.class), any(Instant.class));
        }

        @Test
        @DisplayName("✅ Aucun log entre les dates → 200 OK avec liste vide")
        void shouldReturnEmptyListWhenNoLogsBetweenDates() throws Exception {
            when(auditLogService.getAuditLogsBetween(any(Instant.class), any(Instant.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/audit-logs/between")
                            .param("start", DATE_START.toString())
                            .param("end", DATE_END.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", empty()));
        }

        @Test
        @DisplayName("❌ Paramètre 'start' manquant → 400 Bad Request")
        void shouldReturn400WhenStartIsMissing() throws Exception {
            mockMvc.perform(get("/api/audit-logs/between")
                            .param("end", DATE_END.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(auditLogService, never()).getAuditLogsBetween(any(), any());
        }

        @Test
        @DisplayName("❌ Paramètre 'end' manquant → 400 Bad Request")
        void shouldReturn400WhenEndIsMissing() throws Exception {
            mockMvc.perform(get("/api/audit-logs/between")
                            .param("start", DATE_START.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(auditLogService, never()).getAuditLogsBetween(any(), any());
        }
    }

    // ============================================================
    // Configuration de test
    // ============================================================

    @Configuration
    static class TestConfig {

        @Bean
        public AuditorAware<String> auditorProvider() {
            return () -> Optional.of("test");
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .registerModule(new JavaTimeModule());
        }
    }
}