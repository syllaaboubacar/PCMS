package lu.police.pcms.department.repository;

import lu.police.pcms.common.TestDataFactory;
import lu.police.pcms.department.entity.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("Sauvegarde d'un département")
    void shouldSaveDepartment() {
        Department department = TestDataFactory.createDepartment();
        Department saved = departmentRepository.save(department);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("INV");
        assertThat(saved.getName()).isEqualTo("Investigations");
    }

    @Test
    @DisplayName("Recherche par code")
    void shouldFindByCode() {
        Department department = departmentRepository.save(TestDataFactory.createDepartment());
        Optional<Department> result = departmentRepository.findByCode(department.getCode());
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("INV");
    }

    @Test
    @DisplayName("Recherche par nom")
    void shouldFindByName() {
        Department department = departmentRepository.save(TestDataFactory.createDepartment());
        Optional<Department> result = departmentRepository.findByName(department.getName());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Investigations");
    }

    @Test
    @DisplayName("existsByCode retourne vrai")
    void shouldReturnTrueWhenCodeExists() {
        Department department = departmentRepository.save(TestDataFactory.createDepartment());
        boolean exists = departmentRepository.existsByCode(department.getCode());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByName retourne vrai")
    void shouldReturnTrueWhenNameExists() {
        Department department = departmentRepository.save(TestDataFactory.createDepartment());
        boolean exists = departmentRepository.existsByName(department.getName());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Comptage des départements")
    void shouldCountDepartments() {
        departmentRepository.save(TestDataFactory.createDepartment());
        long count = departmentRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Suppression d'un département")
    void shouldDeleteDepartment() {
        Department department = departmentRepository.save(TestDataFactory.createDepartment());
        departmentRepository.delete(department);
        assertThat(departmentRepository.count()).isZero();
    }
}