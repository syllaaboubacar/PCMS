package lu.police.pcms.audit.service;

import lu.police.pcms.audit.dto.AuditLogResponse;
import lu.police.pcms.audit.dto.CreateAuditLogRequest;
import lu.police.pcms.audit.entity.AuditLog;
import lu.police.pcms.audit.mapper.AuditLogMapper;
import lu.police.pcms.audit.repository.AuditLogRepository;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.user.entity.User;
import lu.police.pcms.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service AuditLogService")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogService auditLogService;

    // ========== DONNÉES DE TEST ==========

    private static final Long LOG_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final String ACTION = "CREATE";
    private static final String ENTITY_NAME = "ROLE";
    private static final Long ENTITY_ID = 100L;

    private User mockUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("admin@pcms.lu");
        return user;
    }

    private AuditLog mockAuditLog() {
        AuditLog log = new AuditLog();
        log.setId(LOG_ID);
        log.setUser(mockUser());
        log.setAction(ACTION);
        log.setEntityName(ENTITY_NAME);
        log.setEntityId(ENTITY_ID);
        log.setDetails("Création du rôle ADMIN");
        log.setIpAddress("127.0.0.1");
        log.setCreatedAt(Instant.now());
        return log;
    }

    private AuditLogResponse mockResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setUserId(log.getUser().getId());
        response.setAction(log.getAction());
        response.setEntityName(log.getEntityName());
        response.setEntityId(log.getEntityId());
        response.setDetails(log.getDetails());
        response.setIpAddress(log.getIpAddress());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'un log d'audit avec succès")
    void shouldCreateAuditLogSuccessfully() {
        // Arrange
        CreateAuditLogRequest request = new CreateAuditLogRequest(
                USER_ID, ACTION, ENTITY_NAME, ENTITY_ID,
                "Création du rôle ADMIN", "127.0.0.1"
        );
        User user = mockUser();
        AuditLog entity = new AuditLog();
        AuditLog saved = mockAuditLog();
        AuditLogResponse expected = mockResponse(saved);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(auditLogMapper.toEntity(request)).thenReturn(entity);
        when(auditLogRepository.save(entity)).thenReturn(saved);
        when(auditLogMapper.toResponse(saved)).thenReturn(expected);

        // Act
        AuditLogResponse actual = auditLogService.createAuditLog(request);

        // Assert
        assertThat(actual).isEqualTo(expected);
        assertThat(entity.getUser()).isEqualTo(user);
        verify(auditLogRepository).save(entity);
    }

    @Test
    @DisplayName("Création avec utilisateur inexistant → exception")
    void shouldThrowExceptionWhenUserNotFound() {
        CreateAuditLogRequest request = new CreateAuditLogRequest(
                USER_ID, ACTION, ENTITY_NAME, ENTITY_ID, null, null
        );
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.createAuditLog(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Utilisateur");
    }

    @Test
    @DisplayName("Récupération d'un log par ID avec succès")
    void shouldGetAuditLogByIdSuccessfully() {
        AuditLog log = mockAuditLog();
        AuditLogResponse expected = mockResponse(log);
        when(auditLogRepository.findById(LOG_ID)).thenReturn(Optional.of(log));
        when(auditLogMapper.toResponse(log)).thenReturn(expected);

        AuditLogResponse actual = auditLogService.getAuditLogById(LOG_ID);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'un log inexistant → exception")
    void shouldThrowExceptionWhenLogNotFound() {
        when(auditLogRepository.findById(LOG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.getAuditLogById(LOG_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Log d'audit");
    }

    @Test
    @DisplayName("Récupération de tous les logs")
    void shouldGetAllAuditLogs() {
        AuditLog log1 = mockAuditLog();
        AuditLog log2 = mockAuditLog();
        log2.setId(2L);
        List<AuditLog> list = List.of(log1, log2);

        when(auditLogRepository.findAll()).thenReturn(list);
        when(auditLogMapper.toResponse(any(AuditLog.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<AuditLogResponse> responses = auditLogService.getAllAuditLogs();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(AuditLogResponse::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Récupération paginée des logs")
    void shouldGetAuditLogsPaginated() {
        PageRequest pageable = PageRequest.of(0, 10);
        AuditLog log = mockAuditLog();
        Page<AuditLog> page = new PageImpl<>(List.of(log));

        when(auditLogRepository.findAll(pageable)).thenReturn(page);
        when(auditLogMapper.toResponse(log)).thenReturn(mockResponse(log));

        Page<AuditLogResponse> responses = auditLogService.getAuditLogsPaginated(pageable);

        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(LOG_ID);
    }

    @Test
    @DisplayName("Récupération des logs par utilisateur")
    void shouldGetAuditLogsByUser() {
        User user = mockUser();
        AuditLog log = mockAuditLog();
        List<AuditLog> list = List.of(log);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(auditLogRepository.findByUser(user)).thenReturn(list);
        when(auditLogMapper.toResponse(any(AuditLog.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<AuditLogResponse> responses = auditLogService.getAuditLogsByUser(USER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("Récupération des logs par action")
    void shouldGetAuditLogsByAction() {
        AuditLog log = mockAuditLog();
        List<AuditLog> list = List.of(log);

        when(auditLogRepository.findByAction(ACTION)).thenReturn(list);
        when(auditLogMapper.toResponse(any(AuditLog.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<AuditLogResponse> responses = auditLogService.getAuditLogsByAction(ACTION);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAction()).isEqualTo(ACTION);
    }

    @Test
    @DisplayName("Récupération des logs par entité")
    void shouldGetAuditLogsByEntityName() {
        AuditLog log = mockAuditLog();
        List<AuditLog> list = List.of(log);

        when(auditLogRepository.findByEntityName(ENTITY_NAME)).thenReturn(list);
        when(auditLogMapper.toResponse(any(AuditLog.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<AuditLogResponse> responses = auditLogService.getAuditLogsByEntityName(ENTITY_NAME);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEntityName()).isEqualTo(ENTITY_NAME);
    }

    @Test
    @DisplayName("Récupération des logs par entité et ID")
    void shouldGetAuditLogsByEntityNameAndEntityId() {
        AuditLog log = mockAuditLog();
        List<AuditLog> list = List.of(log);

        when(auditLogRepository.findByEntityNameAndEntityId(ENTITY_NAME, ENTITY_ID)).thenReturn(list);
        when(auditLogMapper.toResponse(any(AuditLog.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<AuditLogResponse> responses = auditLogService.getAuditLogsByEntityNameAndEntityId(ENTITY_NAME, ENTITY_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEntityId()).isEqualTo(ENTITY_ID);
    }

    @Test
    @DisplayName("Récupération des logs après une date")
    void shouldGetAuditLogsAfter() {
        Instant date = Instant.now().minusSeconds(3600);
        AuditLog log = mockAuditLog();
        List<AuditLog> list = List.of(log);

        when(auditLogRepository.findByCreatedAtAfter(date)).thenReturn(list);
        when(auditLogMapper.toResponse(any(AuditLog.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<AuditLogResponse> responses = auditLogService.getAuditLogsAfter(date);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Comptage des logs par utilisateur")
    void shouldCountAuditLogsByUser() {
        User user = mockUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(auditLogRepository.countByUser(user)).thenReturn(5L);

        long count = auditLogService.countAuditLogsByUser(USER_ID);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("Comptage des logs par action")
    void shouldCountAuditLogsByAction() {
        when(auditLogRepository.countByAction(ACTION)).thenReturn(3L);

        long count = auditLogService.countAuditLogsByAction(ACTION);

        assertThat(count).isEqualTo(3L);
    }
}