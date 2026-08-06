package lu.police.pcms.user.repository;

import lu.police.pcms.common.TestDataFactory;
import lu.police.pcms.department.entity.Department;
import lu.police.pcms.department.repository.DepartmentRepository;
import lu.police.pcms.role.entity.Role;
import lu.police.pcms.role.repository.RoleRepository;
import lu.police.pcms.user.entity.User;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Role testRole;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // Créer et persister un rôle et un département pour les utiliser dans tous les tests
        testRole = roleRepository.save(TestDataFactory.createRole("ROLE_USER"));
        testDepartment = departmentRepository.save(TestDataFactory.createDepartment("IT", "Informatique"));
    }

    @Test
    @DisplayName("Sauvegarde d'un utilisateur")
    void shouldSaveUser() {
        // Arrange
        User user = TestDataFactory.createUser("John", "Doe", "aboubacar.sylla@pcms.lu", testRole, testDepartment);

        // Act
        User saved = userRepository.save(user);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getEmail()).isEqualTo("aboubacar.sylla@pcms.lu");
        assertThat(saved.getRole()).isEqualTo(testRole);
        assertThat(saved.getDepartment()).isEqualTo(testDepartment);
        assertThat(saved.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("Recherche d'un utilisateur par email")
    void shouldFindByEmail() {
        // Arrange
        User user = userRepository.save(TestDataFactory.createUser(testRole, testDepartment));

        // Act
        Optional<User> found = userRepository.findByEmail(user.getEmail());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Recherche des utilisateurs par rôle")
    void shouldFindByRole() {
        // Arrange
        userRepository.save(TestDataFactory.createUser(testRole, testDepartment));
        // Créer un autre utilisateur avec un rôle différent pour vérifier le filtre
        Role otherRole = roleRepository.save(TestDataFactory.createRole("ROLE_ADMIN"));
        userRepository.save(TestDataFactory.createUser(otherRole, testDepartment));

        // Act
        List<User> users = userRepository.findByRole(testRole);

        // Assert
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getRole()).isEqualTo(testRole);
    }

    @Test
    @DisplayName("Recherche des utilisateurs par département")
    void shouldFindByDepartment() {
        // Arrange
        userRepository.save(TestDataFactory.createUser(testRole, testDepartment));
        Department otherDepartment = departmentRepository.save(TestDataFactory.createDepartment("HR", "Ressources Humaines"));
        userRepository.save(TestDataFactory.createUser(testRole, otherDepartment));

        // Act
        List<User> users = userRepository.findByDepartment(testDepartment);

        // Assert
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getDepartment()).isEqualTo(testDepartment);
    }

    @Test
    @DisplayName("Recherche des utilisateurs activés")
    void shouldFindByEnabledTrue() {
        // Arrange
        userRepository.save(TestDataFactory.createUser(testRole, testDepartment));
        User disabledUser = TestDataFactory.createUser(testRole, testDepartment);
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        // Act
        List<User> activeUsers = userRepository.findByEnabledTrue();

        // Assert
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEnabled()).isTrue();
    }

    @Test
    @DisplayName("Recherche des utilisateurs désactivés")
    void shouldFindByEnabledFalse() {
        // Arrange
        User enabledUser = TestDataFactory.createUser(testRole, testDepartment);
        userRepository.save(enabledUser);
        User disabledUser = TestDataFactory.createUser(testRole, testDepartment);
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        // Act
        List<User> inactiveUsers = userRepository.findByEnabledFalse();

        // Assert
        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getEnabled()).isFalse();
    }

    @Test
    @DisplayName("Recherche des utilisateurs actifs par département")
    void shouldFindByDepartmentAndEnabledTrue() {
        // Arrange
        Department otherDepartment = departmentRepository.save(TestDataFactory.createDepartment("HR", "Ressources Humaines"));
        User user1 = TestDataFactory.createUser(testRole, testDepartment);
        userRepository.save(user1);
        User user2 = TestDataFactory.createUser(testRole, otherDepartment);
        userRepository.save(user2);
        User user3 = TestDataFactory.createUser(testRole, testDepartment);
        user3.setEnabled(false);
        userRepository.save(user3);

        // Act
        List<User> activeInDept = userRepository.findByDepartmentAndEnabledTrue(testDepartment);

        // Assert
        assertThat(activeInDept).hasSize(1);
        assertThat(activeInDept.get(0).getDepartment()).isEqualTo(testDepartment);
        assertThat(activeInDept.get(0).getEnabled()).isTrue();
    }

    @Test
    @DisplayName("Recherche des utilisateurs actifs par rôle")
    void shouldFindByRoleAndEnabledTrue() {
        // Arrange
        Role otherRole = roleRepository.save(TestDataFactory.createRole("ROLE_ADMIN"));
        User user1 = TestDataFactory.createUser(testRole, testDepartment);
        userRepository.save(user1);
        User user2 = TestDataFactory.createUser(otherRole, testDepartment);
        userRepository.save(user2);
        User user3 = TestDataFactory.createUser(testRole, testDepartment);
        user3.setEnabled(false);
        userRepository.save(user3);

        // Act
        List<User> activeWithRole = userRepository.findByRoleAndEnabledTrue(testRole);

        // Assert
        assertThat(activeWithRole).hasSize(1);
        assertThat(activeWithRole.get(0).getRole()).isEqualTo(testRole);
        assertThat(activeWithRole.get(0).getEnabled()).isTrue();
    }

    @Test
    @DisplayName("existsByEmail retourne vrai quand l'email existe")
    void shouldReturnTrueWhenEmailExists() {
        // Arrange
        User user = userRepository.save(TestDataFactory.createUser(testRole, testDepartment));

        // Act
        boolean exists = userRepository.existsByEmail(user.getEmail());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail retourne faux quand l'email n'existe pas")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@pcms.lu");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("countByEnabledTrue retourne le bon nombre")
    void shouldCountEnabledUsers() {
        // Arrange
        userRepository.save(TestDataFactory.createUser(testRole, testDepartment));
        User disabledUser = TestDataFactory.createUser(testRole, testDepartment);
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        // Act
        long count = userRepository.countByEnabledTrue();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countByDepartment retourne le bon nombre")
    void shouldCountUsersByDepartment() {
        // Arrange
        Department otherDepartment = departmentRepository.save(TestDataFactory.createDepartment("HR", "Ressources Humaines"));
        userRepository.save(TestDataFactory.createUser(testRole, testDepartment));
        userRepository.save(TestDataFactory.createUser(testRole, testDepartment));
        userRepository.save(TestDataFactory.createUser(testRole, otherDepartment));

        // Act
        long count = userRepository.countByDepartment(testDepartment);

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countByRole retourne le bon nombre")
    void shouldCountUsersByRole() {
        // Arrange
        Role otherRole = roleRepository.save(TestDataFactory.createRole("ROLE_ADMIN"));
        userRepository.save(TestDataFactory.createUser(testRole, testDepartment));
        userRepository.save(TestDataFactory.createUser(testRole, testDepartment));
        userRepository.save(TestDataFactory.createUser(otherRole, testDepartment));

        // Act
        long count = userRepository.countByRole(testRole);

        // Assert
        assertThat(count).isEqualTo(2);
    }
}