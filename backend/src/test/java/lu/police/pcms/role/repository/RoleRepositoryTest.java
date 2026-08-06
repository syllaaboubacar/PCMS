package lu.police.pcms.role.repository;

import lu.police.pcms.common.TestDataFactory;
import lu.police.pcms.role.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du RoleRepository.
 *
 * <p>
 * Ces tests vérifient le bon fonctionnement des méthodes
 * Spring Data JPA en utilisant une véritable base PostgreSQL
 * dédiée aux tests.
 * </p>
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Vérifie qu'un rôle peut être enregistré.
     */
    @Test
    @DisplayName("Sauvegarde d'un rôle")
    void shouldSaveRole() {

        // Arrange
        Role role = TestDataFactory.createRole();

        // Act
        Role savedRole = roleRepository.save(role);

        // Assert
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("ROLE_ADMIN");
    }

    /**
     * Vérifie la recherche par nom.
     */
    @Test
    @DisplayName("Recherche d'un rôle par son nom")
    void shouldFindRoleByName() {

        // Arrange
        Role role = roleRepository.save(
                TestDataFactory.createRole()
        );

        // Act
        Optional<Role> result =
                roleRepository.findByName(role.getName());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo("ROLE_ADMIN");
    }

    /**
     * Vérifie existsByName().
     */
    @Test
    @DisplayName("Vérification de l'existence d'un rôle")
    void shouldReturnTrueWhenRoleExists() {

        // Arrange
        Role role = roleRepository.save(
                TestDataFactory.createRole()
        );

        // Act
        boolean exists =
                roleRepository.existsByName(role.getName());

        // Assert
        assertThat(exists).isTrue();
    }

    /**
     * Vérifie count().
     */
    @Test
    @DisplayName("Compter les rôles")
    void shouldCountRoles() {

        // Arrange
        roleRepository.save(
                TestDataFactory.createRole()
        );

        // Act
        long count = roleRepository.count();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    /**
     * Vérifie delete().
     */
    @Test
    @DisplayName("Supprimer un rôle")
    void shouldDeleteRole() {

        // Arrange
        Role role = roleRepository.save(
                TestDataFactory.createRole()
        );

        // Act
        roleRepository.delete(role);

        // Assert
        assertThat(roleRepository.count())
                .isZero();
    }

}