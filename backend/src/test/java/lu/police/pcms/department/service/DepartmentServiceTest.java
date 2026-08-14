package lu.police.pcms.department.service;

import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.department.dto.CreateDepartmentRequest;
import lu.police.pcms.department.dto.DepartmentResponse;
import lu.police.pcms.department.dto.PatchDepartmentRequest;
import lu.police.pcms.department.dto.UpdateDepartmentRequest;
import lu.police.pcms.department.entity.Department;
import lu.police.pcms.department.mapper.DepartmentMapper;
import lu.police.pcms.department.repository.DepartmentRepository;
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
@DisplayName("Tests du service DepartmentService")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentService departmentService;

    // ========== DONNÉES DE TEST ==========

    private static final Long DEPT_ID = 1L;
    private static final String DEPT_CODE = "INV";
    private static final String DEPT_NAME = "Investigations";
    private static final String NEW_CODE = "IT";
    private static final String NEW_NAME = "Informatique";

    private Department mockDepartment(boolean deleted) {
        Department dept = new Department();
        dept.setId(DEPT_ID);
        dept.setCode(DEPT_CODE);
        dept.setName(DEPT_NAME);
        dept.setDeleted(deleted);
        dept.setCreatedAt(Instant.now());
        return dept;
    }

    private DepartmentResponse mockResponse(Department dept) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(dept.getId());
        response.setCode(dept.getCode());
        response.setName(dept.getName());
        response.setDeleted(dept.getDeleted());
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'un département avec succès")
    void shouldCreateDepartmentSuccessfully() {
        // Arrange
        CreateDepartmentRequest request = new CreateDepartmentRequest(DEPT_CODE, DEPT_NAME);
        Department entity = new Department();
        Department saved = mockDepartment(false);
        DepartmentResponse expected = mockResponse(saved);

        when(departmentRepository.existsByCode(DEPT_CODE)).thenReturn(false);
        when(departmentRepository.existsByName(DEPT_NAME)).thenReturn(false);
        when(departmentMapper.toEntity(request)).thenReturn(entity);
        when(departmentRepository.save(entity)).thenReturn(saved);
        when(departmentMapper.toResponse(saved)).thenReturn(expected);

        // Act
        DepartmentResponse actual = departmentService.createDepartment(request);

        // Assert
        assertThat(actual).isEqualTo(expected);
        verify(departmentRepository).save(entity);
    }

    @Test
    @DisplayName("Création avec code existant → exception")
    void shouldThrowExceptionWhenCodeExists() {
        CreateDepartmentRequest request = new CreateDepartmentRequest(DEPT_CODE, DEPT_NAME);
        when(departmentRepository.existsByCode(DEPT_CODE)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("code");

        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Création avec nom existant → exception")
    void shouldThrowExceptionWhenNameExists() {
        CreateDepartmentRequest request = new CreateDepartmentRequest(DEPT_CODE, DEPT_NAME);
        when(departmentRepository.existsByCode(DEPT_CODE)).thenReturn(false);
        when(departmentRepository.existsByName(DEPT_NAME)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("nom");

        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Récupération d'un département existant")
    void shouldGetDepartmentByIdSuccessfully() {
        Department dept = mockDepartment(false);
        DepartmentResponse expected = mockResponse(dept);
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(dept));
        when(departmentMapper.toResponse(dept)).thenReturn(expected);

        DepartmentResponse actual = departmentService.getDepartmentById(DEPT_ID);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'un département inexistant → exception")
    void shouldThrowExceptionWhenDepartmentNotFound() {
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(DEPT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération d'un département supprimé → exception")
    void shouldThrowExceptionWhenDepartmentIsDeleted() {
        Department dept = mockDepartment(true);
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(dept));

        assertThatThrownBy(() -> departmentService.getDepartmentById(DEPT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération de tous les départements actifs")
    void shouldGetAllActiveDepartments() {
        Department dept1 = mockDepartment(false);
        Department dept2 = mockDepartment(false);
        dept2.setId(2L);
        dept2.setCode("HR");
        dept2.setName("Ressources Humaines");
        List<Department> list = List.of(dept1, dept2);

        when(departmentRepository.findByDeletedFalse()).thenReturn(list);
        when(departmentMapper.toResponse(any(Department.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<DepartmentResponse> responses = departmentService.getAllDepartments();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(DepartmentResponse::getCode)
                .containsExactlyInAnyOrder("INV", "HR");
    }

    @Test
    @DisplayName("Mise à jour complète (PUT) avec succès")
    void shouldUpdateDepartmentSuccessfully() {
        // Arrange
        Department existing = mockDepartment(false);
        UpdateDepartmentRequest request = new UpdateDepartmentRequest(NEW_CODE, NEW_NAME);

        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(existing));
        when(departmentRepository.existsByCode(NEW_CODE)).thenReturn(false);
        when(departmentRepository.existsByName(NEW_NAME)).thenReturn(false);

        doAnswer(inv -> {
            Department d = inv.getArgument(1);
            d.setCode(NEW_CODE);
            d.setName(NEW_NAME);
            return null;
        }).when(departmentMapper).updateEntity(request, existing);

        when(departmentRepository.save(existing)).thenReturn(existing);
        when(departmentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        DepartmentResponse actual = departmentService.updateDepartment(DEPT_ID, request);

        // Assert
        assertThat(actual.getCode()).isEqualTo(NEW_CODE);
        assertThat(actual.getName()).isEqualTo(NEW_NAME);
        verify(departmentRepository).save(existing);
    }

    @Test
    @DisplayName("PUT avec code déjà utilisé par un autre → exception")
    void shouldThrowExceptionWhenUpdatingToExistingCode() {
        Department existing = mockDepartment(false);
        UpdateDepartmentRequest request = new UpdateDepartmentRequest(NEW_CODE, DEPT_NAME);

        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(existing));
        when(departmentRepository.existsByCode(NEW_CODE)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.updateDepartment(DEPT_ID, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("code");
    }

    @Test
    @DisplayName("Mise à jour partielle (PATCH) avec succès")
    void shouldPatchDepartmentSuccessfully() {
        // Arrange
        Department existing = mockDepartment(false);
        PatchDepartmentRequest request = new PatchDepartmentRequest(NEW_CODE, null); // seul le code change

        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(existing));
        when(departmentRepository.existsByCode(NEW_CODE)).thenReturn(false);
        doAnswer(inv -> {
            Department d = inv.getArgument(1);
            d.setCode(NEW_CODE);
            return null;
        }).when(departmentMapper).patchEntity(request, existing);

        when(departmentRepository.save(existing)).thenReturn(existing);
        when(departmentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        DepartmentResponse actual = departmentService.patchDepartment(DEPT_ID, request);

        // Assert
        assertThat(actual.getCode()).isEqualTo(NEW_CODE);
        assertThat(actual.getName()).isEqualTo(DEPT_NAME); // inchangé
        verify(departmentRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression logique avec succès")
    void shouldDeleteDepartmentSuccessfully() {
        Department existing = mockDepartment(false);
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(existing));

        departmentService.deleteDepartment(DEPT_ID);

        assertThat(existing.getDeleted()).isTrue();
        verify(departmentRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression d'un département déjà supprimé ne fait rien")
    void shouldDoNothingWhenDeletingAlreadyDeleted() {
        Department existing = mockDepartment(true);
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(existing));

        departmentService.deleteDepartment(DEPT_ID);

        verify(departmentRepository, never()).save(existing);
    }
}