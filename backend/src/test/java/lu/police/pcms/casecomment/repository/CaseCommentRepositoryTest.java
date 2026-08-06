package lu.police.pcms.casecomment.repository;

import lu.police.pcms.common.TestDataFactory;
import lu.police.pcms.casecomment.entity.CaseComment;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du repository {@link CaseCommentRepository}.
 * <p>
 * Vérifie toutes les méthodes du repository : CRUD, recherches par dossier,
 * par utilisateur, par dates, par état de suppression, et comptages.
 * </p>
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CaseCommentRepositoryTest {

    // ========================================================================
    // 1. Injections des repositories
    // ========================================================================

    @Autowired
    private CaseCommentRepository caseCommentRepository;

    @Autowired
    private CaseFileRepository caseFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // ========================================================================
    // 2. Données partagées (réinitialisées avant chaque test)
    // ========================================================================

    private CaseFile testCaseFile;
    private User testUser;

    /**
     * Crée et persiste un dossier et un utilisateur avant chaque test.
     */
    @BeforeEach
    void setUp() {
        // 1. Dossier parent
        testCaseFile = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-COMMENT-001", "Dossier avec commentaires", CaseStatus.OPEN, CasePriority.MEDIUM
        ));

        // 2. Utilisateur (auteur des commentaires)
        Role role = roleRepository.save(TestDataFactory.createRole("ROLE_USER"));
        Department department = departmentRepository.save(TestDataFactory.createDepartment("DEPT_IT", "Informatique"));
        testUser = userRepository.save(TestDataFactory.createUser(
                "Alice", "Dupont", "alice.dupont@pcms.lu", role, department
        ));
    }

    // ========================================================================
    // 3. Tests des méthodes
    // ========================================================================

    @Test
    @DisplayName("Sauvegarde d'un commentaire")
    void shouldSaveComment() {
        CaseComment comment = TestDataFactory.createCaseComment(testCaseFile, testUser);
        CaseComment saved = caseCommentRepository.save(comment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCaseFile()).isEqualTo(testCaseFile);
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getContent()).isEqualTo("Ceci est un commentaire de test.");
        assertThat(saved.getDeleted()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Recherche des commentaires par dossier (entité)")
    void shouldFindByCaseFile() {
        // Deux commentaires sur le dossier principal
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser, "Deuxième commentaire"));

        // Un commentaire sur un autre dossier (ne doit pas être retourné)
        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-COMMENT-002", "Autre dossier", CaseStatus.OPEN, CasePriority.LOW
        ));
        caseCommentRepository.save(TestDataFactory.createCaseComment(otherCase, testUser));

        List<CaseComment> comments = caseCommentRepository.findByCaseFile(testCaseFile);

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(CaseComment::getContent)
                .containsExactlyInAnyOrder("Ceci est un commentaire de test.", "Deuxième commentaire");
    }

    @Test
    @DisplayName("Recherche des commentaires par ID de dossier")
    void shouldFindByCaseFileId() {
        CaseComment comment = caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));
        List<CaseComment> comments = caseCommentRepository.findByCaseFileId(testCaseFile.getId());

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getId()).isEqualTo(comment.getId());
    }

    @Test
    @DisplayName("Recherche des commentaires par utilisateur (entité)")
    void shouldFindByUser() {
        // Deux commentaires pour l'utilisateur principal
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser, "Second commentaire"));

        // Un commentaire pour un autre utilisateur
        Role role = roleRepository.save(TestDataFactory.createRole("ROLE_VIEWER"));
        Department department = departmentRepository.save(TestDataFactory.createDepartment("DEPT_HR", "RH"));
        User otherUser = userRepository.save(TestDataFactory.createUser(
                "Bob", "Martin", "bob.martin@pcms.lu", role, department
        ));
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, otherUser));

        List<CaseComment> comments = caseCommentRepository.findByUser(testUser);

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(CaseComment::getUser).containsOnly(testUser);
    }

    @Test
    @DisplayName("Recherche des commentaires par ID d'utilisateur")
    void shouldFindByUserId() {
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));
        List<CaseComment> comments = caseCommentRepository.findByUserId(testUser.getId());

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test de {@link CaseCommentRepository#findByCreatedAtAfter(Instant)}.
     * On sauvegarde un commentaire, puis on recherche tous ceux créés après un instant
     * antérieur à la sauvegarde. On doit trouver au moins ce commentaire.
     */
    @Test
    @DisplayName("Recherche des commentaires créés après une date")
    void shouldFindByCreatedAtAfter() {
        // On note l'instant avant la sauvegarde
        Instant beforeSave = Instant.now();

        // Sauvegarde d'un commentaire
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));

        // Recherche des commentaires créés après beforeSave
        List<CaseComment> comments = caseCommentRepository.findByCreatedAtAfter(beforeSave);

        // On doit avoir au moins un commentaire (celui qu'on vient de sauvegarder)
        assertThat(comments).isNotEmpty();
        // On peut vérifier que l'ID du commentaire trouvé correspond, mais on n'a pas gardé l'ID.
        // On se contente de vérifier qu'il y a au moins un résultat.
    }

    /**
     * Test de {@link CaseCommentRepository#findByCreatedAtBetween(Instant, Instant)}.
     * On sauvegarde un commentaire, on définit une plage qui l'englobe, et on vérifie
     * qu'il est bien retourné.
     */
    @Test
    @DisplayName("Recherche des commentaires entre deux dates")
    void shouldFindByCreatedAtBetween() {
        // Sauvegarde d'un commentaire
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));

        // Plage de dates : 1 heure avant et 1 heure après maintenant
        Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(1, ChronoUnit.HOURS);

        // Recherche
        List<CaseComment> comments = caseCommentRepository.findByCreatedAtBetween(start, end);

        // On doit trouver le commentaire sauvegardé (car sa date de création est dans cette plage)
        assertThat(comments).hasSize(1);
    }

    @Test
    @DisplayName("Recherche des commentaires non supprimés")
    void shouldFindByDeletedFalse() {
        // Commentaire actif
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));

        // Commentaire supprimé logiquement
        CaseComment deleted = TestDataFactory.createCaseComment(testCaseFile, testUser);
        deleted.setDeleted(true);
        caseCommentRepository.save(deleted);

        List<CaseComment> active = caseCommentRepository.findByDeletedFalse();

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getDeleted()).isFalse();
    }

    @Test
    @DisplayName("Recherche des commentaires supprimés")
    void shouldFindByDeletedTrue() {
        // Commentaire actif
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));

        // Commentaire supprimé
        CaseComment deleted = TestDataFactory.createCaseComment(testCaseFile, testUser);
        deleted.setDeleted(true);
        caseCommentRepository.save(deleted);

        List<CaseComment> deletedComments = caseCommentRepository.findByDeletedTrue();

        assertThat(deletedComments).hasSize(1);
        assertThat(deletedComments.get(0).getDeleted()).isTrue();
    }

    @Test
    @DisplayName("Comptage des commentaires par dossier")
    void shouldCountByCaseFileId() {
        // Deux commentaires sur le dossier principal
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser, "Deuxième commentaire"));

        // Un commentaire sur un autre dossier
        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-COMMENT-003", "Troisième dossier", CaseStatus.OPEN, CasePriority.HIGH
        ));
        caseCommentRepository.save(TestDataFactory.createCaseComment(otherCase, testUser));

        long count = caseCommentRepository.countByCaseFileId(testCaseFile.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Comptage des commentaires par utilisateur")
    void shouldCountByUserId() {
        // Deux commentaires pour l'utilisateur principal
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser));
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, testUser, "Deuxième commentaire"));

        // Un commentaire pour un autre utilisateur
        Role role = roleRepository.save(TestDataFactory.createRole("ROLE_VIEWER"));
        Department department = departmentRepository.save(TestDataFactory.createDepartment("DEPT_HR", "RH"));
        User otherUser = userRepository.save(TestDataFactory.createUser(
                "Bob", "Martin", "bob.martin@pcms.lu", role, department
        ));
        caseCommentRepository.save(TestDataFactory.createCaseComment(testCaseFile, otherUser));

        long count = caseCommentRepository.countByUserId(testUser.getId());

        assertThat(count).isEqualTo(2);
    }
}