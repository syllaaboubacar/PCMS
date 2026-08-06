package lu.police.pcms.caseassignment.repository;

import lu.police.pcms.common.TestDataFactory;
import lu.police.pcms.caseassignment.entity.CaseAssignment;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.department.entity.Department;
import lu.police.pcms.department.repository.DepartmentRepository;
import lu.police.pcms.role.entity.Role;
import lu.police.pcms.role.repository.RoleRepository;
import lu.police.pcms.user.entity.User;
import lu.police.pcms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CaseAssignmentRepositoryTest {

    @Autowired
    private CaseAssignmentRepository caseAssignmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaseFileRepository caseFileRepository;

    private Role testRole;
    private Department testDepartment;
    private User testUser;
    private CaseFile testCaseFile;

    @BeforeEach
    void setUp() {
        testRole = roleRepository.save(TestDataFactory.createRole("ROLE_INVESTIGATOR"));
        testDepartment = departmentRepository.save(TestDataFactory.createDepartment("DEPT_INV", "Investigations"));
        testUser = userRepository.save(TestDataFactory.createUser(
                "Alice", "Dupont", "alice.dupont@pcms.lu", testRole, testDepartment
        ));
        testCaseFile = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-001", "Enquête initiale", CaseStatus.OPEN, CasePriority.HIGH
        ));
    }

    @Test
    @DisplayName("Sauvegarde d'une affectation")
    void shouldSaveAssignment() {
        CaseAssignment assignment = TestDataFactory.createCaseAssignment(testCaseFile, testUser);
        CaseAssignment saved = caseAssignmentRepository.save(assignment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCaseFile()).isEqualTo(testCaseFile);
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getAssignedAt()).isNotNull();
    }

    @Test
    @DisplayName("Recherche des affectations par dossier")
    void shouldFindByCaseFile() {
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-002", "Autre enquête", CaseStatus.OPEN, CasePriority.MEDIUM
        ));
        User otherUser = userRepository.save(TestDataFactory.createUser(
                "Bob", "Martin", "bob.martin@pcms.lu", testRole, testDepartment
        ));
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(otherCase, otherUser));

        List<CaseAssignment> assignments = caseAssignmentRepository.findByCaseFile(testCaseFile);

        assertThat(assignments).hasSize(1);
        assertThat(assignments.get(0).getCaseFile()).isEqualTo(testCaseFile);
    }

    @Test
    @DisplayName("Recherche des affectations actives par dossier")
    void shouldFindByCaseFileAndActiveTrue() {
        // 1. Une affectation active sur le dossier principal avec l'utilisateur principal
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        // 2. Une affectation inactive sur le même dossier, mais avec un second utilisateur
        User secondUser = userRepository.save(TestDataFactory.createUser(
                "Bob", "Martin", "bob.martin@pcms.lu", testRole, testDepartment
        ));
        CaseAssignment inactive = TestDataFactory.createCaseAssignment(testCaseFile, secondUser);
        inactive.setActive(false);
        caseAssignmentRepository.save(inactive);

        List<CaseAssignment> activeAssignments = caseAssignmentRepository.findByCaseFileAndActiveTrue(testCaseFile);

        assertThat(activeAssignments).hasSize(1);
        assertThat(activeAssignments.get(0).getActive()).isTrue();
    }

    @Test
    @DisplayName("Recherche des affectations par utilisateur")
    void shouldFindByUser() {
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        User otherUser = userRepository.save(TestDataFactory.createUser(
                "Charlie", "Durand", "charlie.durand@pcms.lu", testRole, testDepartment
        ));
        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-003", "Troisième enquête", CaseStatus.OPEN, CasePriority.LOW
        ));
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(otherCase, otherUser));

        List<CaseAssignment> assignments = caseAssignmentRepository.findByUser(testUser);

        assertThat(assignments).hasSize(1);
        assertThat(assignments.get(0).getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Recherche des affectations actives par utilisateur")
    void shouldFindByUserAndActiveTrue() {
        // 1. Affectation active pour l'utilisateur principal
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        // 2. Affectation inactive pour le même utilisateur, mais sur un autre dossier
        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-004", "Autre affaire", CaseStatus.OPEN, CasePriority.MEDIUM
        ));
        CaseAssignment inactive = TestDataFactory.createCaseAssignment(otherCase, testUser);
        inactive.setActive(false);
        caseAssignmentRepository.save(inactive);

        List<CaseAssignment> activeAssignments = caseAssignmentRepository.findByUserAndActiveTrue(testUser);

        assertThat(activeAssignments).hasSize(1);
        assertThat(activeAssignments.get(0).getActive()).isTrue();
    }

    @Test
    @DisplayName("Recherche d'une affectation par dossier et utilisateur")
    void shouldFindByCaseFileAndUser() {
        CaseAssignment assignment = caseAssignmentRepository.save(
                TestDataFactory.createCaseAssignment(testCaseFile, testUser)
        );

        Optional<CaseAssignment> found = caseAssignmentRepository.findByCaseFileAndUser(testCaseFile, testUser);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(assignment.getId());
    }

    @Test
    @DisplayName("existsByCaseFileAndUser retourne vrai si l'affectation existe")
    void shouldReturnTrueWhenAssignmentExists() {
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        boolean exists = caseAssignmentRepository.existsByCaseFileAndUser(testCaseFile, testUser);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByCaseFileAndUser retourne faux si l'affectation n'existe pas")
    void shouldReturnFalseWhenAssignmentDoesNotExist() {
        User otherUser = userRepository.save(TestDataFactory.createUser(
                "Diane", "Lefevre", "diane.lefevre@pcms.lu", testRole, testDepartment
        ));

        boolean exists = caseAssignmentRepository.existsByCaseFileAndUser(testCaseFile, otherUser);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Recherche des affectations actives globales")
    void shouldFindByActiveTrue() {
        // 1. Deux affectations actives sur des dossiers différents
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-005", "Enquête 5", CaseStatus.OPEN, CasePriority.HIGH
        ));
        User otherUser = userRepository.save(TestDataFactory.createUser(
                "Eva", "Petit", "eva.petit@pcms.lu", testRole, testDepartment
        ));
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(otherCase, otherUser));

        // 2. Une affectation inactive – sur le même dossier que la première, mais avec un second utilisateur
        User secondUser = userRepository.save(TestDataFactory.createUser(
                "Fred", "Moreau", "fred.moreau@pcms.lu", testRole, testDepartment
        ));
        CaseAssignment inactive = TestDataFactory.createCaseAssignment(testCaseFile, secondUser);
        inactive.setActive(false);
        caseAssignmentRepository.save(inactive);

        List<CaseAssignment> activeAssignments = caseAssignmentRepository.findByActiveTrue();

        assertThat(activeAssignments).hasSize(2);
        assertThat(activeAssignments).allMatch(CaseAssignment::getActive);
    }

    @Test
    @DisplayName("Recherche des affectations inactives globales")
    void shouldFindByActiveFalse() {
        // 1. Affectation inactive
        CaseAssignment inactive = TestDataFactory.createCaseAssignment(testCaseFile, testUser);
        inactive.setActive(false);
        caseAssignmentRepository.save(inactive);

        // 2. Affectation active – sur le même dossier, mais avec un second utilisateur
        User secondUser = userRepository.save(TestDataFactory.createUser(
                "Alice", "Doe", "alice.doe@pcms.lu", testRole, testDepartment
        ));
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, secondUser));

        List<CaseAssignment> inactiveAssignments = caseAssignmentRepository.findByActiveFalse();

        assertThat(inactiveAssignments).hasSize(1);
        assertThat(inactiveAssignments.get(0).getActive()).isFalse();
    }

    @Test
    @DisplayName("Comptage des affectations par dossier")
    void shouldCountByCaseFile() {
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        User secondUser = userRepository.save(TestDataFactory.createUser(
                "Fred", "Moreau", "fred.moreau@pcms.lu", testRole, testDepartment
        ));
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, secondUser));

        long count = caseAssignmentRepository.countByCaseFile(testCaseFile);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Comptage des affectations actives par dossier")
    void shouldCountByCaseFileAndActiveTrue() {
        // 1. Affectation active sur le dossier principal
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        // 2. Affectation inactive sur le même dossier, mais avec un second utilisateur
        User secondUser = userRepository.save(TestDataFactory.createUser(
                "George", "Lucas", "george.lucas@pcms.lu", testRole, testDepartment
        ));
        CaseAssignment inactive = TestDataFactory.createCaseAssignment(testCaseFile, secondUser);
        inactive.setActive(false);
        caseAssignmentRepository.save(inactive);

        long activeCount = caseAssignmentRepository.countByCaseFileAndActiveTrue(testCaseFile);

        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Comptage des affectations par utilisateur")
    void shouldCountByUser() {
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(testCaseFile, testUser));

        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-006", "Autre dossier", CaseStatus.OPEN, CasePriority.LOW
        ));
        caseAssignmentRepository.save(TestDataFactory.createCaseAssignment(otherCase, testUser));

        long count = caseAssignmentRepository.countByUser(testUser);

        assertThat(count).isEqualTo(2);
    }
}