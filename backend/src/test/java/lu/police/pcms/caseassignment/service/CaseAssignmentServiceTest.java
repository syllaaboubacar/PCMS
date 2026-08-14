package lu.police.pcms.caseassignment.service;

import lu.police.pcms.caseassignment.dto.CaseAssignmentResponse;
import lu.police.pcms.caseassignment.dto.CreateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.PatchCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.UpdateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.entity.CaseAssignment;
import lu.police.pcms.caseassignment.mapper.CaseAssignmentMapper;
import lu.police.pcms.caseassignment.repository.CaseAssignmentRepository;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.user.entity.User;
import lu.police.pcms.user.repository.UserRepository;
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
@DisplayName("Tests du service CaseAssignmentService")
class CaseAssignmentServiceTest {

    @Mock
    private CaseAssignmentRepository assignmentRepository;

    @Mock
    private CaseFileRepository caseFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CaseAssignmentMapper assignmentMapper;

    @InjectMocks
    private CaseAssignmentService assignmentService;

    // ========== DONNÉES DE TEST ==========

    private static final Long ASSIGNMENT_ID = 1L;
    private static final Long CASE_FILE_ID = 10L;
    private static final Long USER_ID = 20L;

    private CaseFile mockCaseFile() {
        CaseFile cf = new CaseFile();
        cf.setId(CASE_FILE_ID);
        cf.setCaseNumber("PCMS_CASE_001");
        return cf;
    }

    private User mockUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("alice@pcms.lu");
        return user;
    }

    private CaseAssignment mockAssignment(boolean deleted, boolean active) {
        CaseAssignment assignment = new CaseAssignment();
        assignment.setId(ASSIGNMENT_ID);
        assignment.setCaseFile(mockCaseFile());
        assignment.setUser(mockUser());
        assignment.setAssignedAt(Instant.now());
        assignment.setActive(active);
        assignment.setDeleted(deleted);
        return assignment;
    }

    private CaseAssignmentResponse mockResponse(CaseAssignment assignment) {
        CaseAssignmentResponse response = new CaseAssignmentResponse();
        response.setId(assignment.getId());
        response.setCaseFileId(assignment.getCaseFile().getId());
        response.setUserId(assignment.getUser().getId());
        response.setAssignedAt(assignment.getAssignedAt());
        response.setActive(assignment.getActive());
        response.setDeleted(assignment.getDeleted());
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'une affectation avec succès")
    void shouldCreateAssignmentSuccessfully() {
        // Arrange
        CreateCaseAssignmentRequest request = new CreateCaseAssignmentRequest(CASE_FILE_ID, USER_ID, null);
        CaseFile caseFile = mockCaseFile();
        User user = mockUser();
        CaseAssignment entity = new CaseAssignment();
        CaseAssignment saved = mockAssignment(false, true);
        CaseAssignmentResponse expected = mockResponse(saved);

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(assignmentRepository.existsByCaseFileAndUser(caseFile, user)).thenReturn(false);
        when(assignmentMapper.toEntity(request)).thenReturn(entity);
        when(assignmentRepository.save(entity)).thenReturn(saved);
        when(assignmentMapper.toResponse(saved)).thenReturn(expected);

        // Act
        CaseAssignmentResponse actual = assignmentService.createAssignment(request);

        // Assert
        assertThat(actual).isEqualTo(expected);
        assertThat(entity.getAssignedAt()).isNotNull(); // généré automatiquement
        assertThat(entity.getActive()).isTrue(); // valeur par défaut
        verify(assignmentRepository).save(entity);
    }

    @Test
    @DisplayName("Création avec une affectation existante → exception")
    void shouldThrowExceptionWhenAssignmentExists() {
        // Arrange
        CreateCaseAssignmentRequest request = new CreateCaseAssignmentRequest(CASE_FILE_ID, USER_ID, null);
        CaseFile caseFile = mockCaseFile();
        User user = mockUser();

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(assignmentRepository.existsByCaseFileAndUser(caseFile, user)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> assignmentService.createAssignment(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Affectation");

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Création avec dossier inexistant → exception")
    void shouldThrowExceptionWhenCaseFileNotFound() {
        CreateCaseAssignmentRequest request = new CreateCaseAssignmentRequest(CASE_FILE_ID, USER_ID, null);
        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.createAssignment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dossier");
    }

    @Test
    @DisplayName("Récupération d'une affectation par ID avec succès")
    void shouldGetAssignmentByIdSuccessfully() {
        CaseAssignment assignment = mockAssignment(false, true);
        CaseAssignmentResponse expected = mockResponse(assignment);
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(assignmentMapper.toResponse(assignment)).thenReturn(expected);

        CaseAssignmentResponse actual = assignmentService.getAssignmentById(ASSIGNMENT_ID);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'une affectation supprimée → exception")
    void shouldThrowExceptionWhenAssignmentDeleted() {
        CaseAssignment assignment = mockAssignment(true, true);
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> assignmentService.getAssignmentById(ASSIGNMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération de toutes les affectations")
    void shouldGetAllAssignments() {
        CaseAssignment a1 = mockAssignment(false, true);
        CaseAssignment a2 = mockAssignment(false, false);
        a2.setId(2L);
        List<CaseAssignment> list = List.of(a1, a2);

        when(assignmentRepository.findByDeletedFalse()).thenReturn(list);
        when(assignmentMapper.toResponse(any(CaseAssignment.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<CaseAssignmentResponse> responses = assignmentService.getAllAssignments();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CaseAssignmentResponse::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Récupération des affectations par dossier")
    void shouldGetAssignmentsByCaseFile() {
        CaseFile caseFile = mockCaseFile();
        CaseAssignment a1 = mockAssignment(false, true);
        CaseAssignment a2 = mockAssignment(false, false);
        List<CaseAssignment> list = List.of(a1, a2);

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(assignmentRepository.findByCaseFile(caseFile)).thenReturn(list);
        when(assignmentMapper.toResponse(any(CaseAssignment.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<CaseAssignmentResponse> responses = assignmentService.getAssignmentsByCaseFile(CASE_FILE_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CaseAssignmentResponse::getCaseFileId)
                .containsOnly(CASE_FILE_ID);
    }

    @Test
    @DisplayName("Mise à jour complète (PUT) avec succès")
    void shouldUpdateAssignmentSuccessfully() {
        // Arrange
        CaseAssignment existing = mockAssignment(false, true);
        UpdateCaseAssignmentRequest request = new UpdateCaseAssignmentRequest(false);

        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(existing));
        doAnswer(inv -> {
            CaseAssignment a = inv.getArgument(1);
            a.setActive(false);
            return null;
        }).when(assignmentMapper).updateEntity(request, existing);

        when(assignmentRepository.save(existing)).thenReturn(existing);
        when(assignmentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        CaseAssignmentResponse actual = assignmentService.updateAssignment(ASSIGNMENT_ID, request);

        // Assert
        assertThat(actual.getActive()).isFalse();
        verify(assignmentRepository).save(existing);
    }

    @Test
    @DisplayName("Mise à jour partielle (PATCH) avec succès")
    void shouldPatchAssignmentSuccessfully() {
        // Arrange
        CaseAssignment existing = mockAssignment(false, true);
        PatchCaseAssignmentRequest request = new PatchCaseAssignmentRequest(false);

        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(existing));
        doAnswer(inv -> {
            CaseAssignment a = inv.getArgument(1);
            a.setActive(false);
            return null;
        }).when(assignmentMapper).patchEntity(request, existing);

        when(assignmentRepository.save(existing)).thenReturn(existing);
        when(assignmentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        CaseAssignmentResponse actual = assignmentService.patchAssignment(ASSIGNMENT_ID, request);

        // Assert
        assertThat(actual.getActive()).isFalse();
        verify(assignmentRepository).save(existing);
    }

    @Test
    @DisplayName("PATCH avec active = null ne modifie rien")
    void shouldPatchWithNullActiveDoNothing() {
        // Arrange
        CaseAssignment existing = mockAssignment(false, true);
        PatchCaseAssignmentRequest request = new PatchCaseAssignmentRequest(null);

        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(existing));
        doAnswer(inv -> {
            // Ne rien modifier
            return null;
        }).when(assignmentMapper).patchEntity(request, existing);

        when(assignmentRepository.save(existing)).thenReturn(existing);
        when(assignmentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        CaseAssignmentResponse actual = assignmentService.patchAssignment(ASSIGNMENT_ID, request);

        // Assert
        assertThat(actual.getActive()).isTrue(); // inchangé
        verify(assignmentRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression logique avec succès")
    void shouldDeleteAssignmentSuccessfully() {
        CaseAssignment existing = mockAssignment(false, true);
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(existing));

        assignmentService.deleteAssignment(ASSIGNMENT_ID);

        assertThat(existing.getDeleted()).isTrue();
        verify(assignmentRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression d'une affectation déjà supprimée ne fait rien")
    void shouldDoNothingWhenDeletingAlreadyDeleted() {
        CaseAssignment existing = mockAssignment(true, true);
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(existing));

        assignmentService.deleteAssignment(ASSIGNMENT_ID);

        verify(assignmentRepository, never()).save(existing);
    }
}