package lu.police.pcms.suspect.repository;

import lu.police.pcms.common.TestDataFactory;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.suspect.entity.Suspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du repository {@link SuspectRepository}.
 * <p>
 * Vérifie toutes les méthodes : CRUD, recherches par dossier, identité,
 * date de naissance, nationalité, existences et comptages.
 * </p>
 * <p>
 * Utilise {@link DataJpaTest} pour la couche JPA et
 * {@link AutoConfigureTestDatabase} avec {@code Replace.NONE} pour
 * utiliser la base PostgreSQL de test.
 * </p>
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SuspectRepositoryTest {

    // ========================================================================
    // 1. Injections des repositories
    // ========================================================================

    @Autowired
    private SuspectRepository suspectRepository;

    @Autowired
    private CaseFileRepository caseFileRepository;

    // ========================================================================
    // 2. Données partagées (réinitialisées avant chaque test)
    // ========================================================================

    private CaseFile testCaseFile; // Dossier de référence

    /**
     * Crée et persiste un dossier avant chaque test.
     * Ce dossier sert de parent pour les suspects créés dans les tests.
     */
    @BeforeEach
    void setUp() {
        testCaseFile = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-SUSPECT-001", "Enquête avec suspects", CaseStatus.OPEN, CasePriority.HIGH
        ));
    }

    // ========================================================================
    // 3. Tests
    // ========================================================================

    @Test
    @DisplayName("Sauvegarde d'un suspect")
    void shouldSaveSuspect() {
        // Arrange
        Suspect suspect = TestDataFactory.createSuspect(testCaseFile);

        // Act
        Suspect saved = suspectRepository.save(suspect);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCaseFile()).isEqualTo(testCaseFile);
        assertThat(saved.getFirstName()).isEqualTo("Jean");
        assertThat(saved.getLastName()).isEqualTo("Dupont");
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1980, 5, 15));
        assertThat(saved.getNationality()).isEqualTo("Française");
        assertThat(saved.getNotes()).isEqualTo("Suspect principal");
    }

    @Test
    @DisplayName("Recherche des suspects par dossier (entité)")
    void shouldFindByCaseFile() {
        // Arrange : deux suspects sur le dossier principal
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile));
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Marie", "Durand"));

        // Un suspect sur un autre dossier (ne doit pas être retourné)
        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-SUSPECT-002", "Autre enquête", CaseStatus.OPEN, CasePriority.MEDIUM
        ));
        suspectRepository.save(TestDataFactory.createSuspect(otherCase, "Pierre", "Martin"));

        // Act
        List<Suspect> suspects = suspectRepository.findByCaseFile(testCaseFile);

        // Assert
        assertThat(suspects).hasSize(2);
        assertThat(suspects).extracting(Suspect::getLastName)
                .containsExactlyInAnyOrder("Dupont", "Durand");
    }

    @Test
    @DisplayName("Recherche des suspects par ID de dossier")
    void shouldFindByCaseFileId() {
        // Arrange
        Suspect suspect = suspectRepository.save(TestDataFactory.createSuspect(testCaseFile));

        // Act
        List<Suspect> suspects = suspectRepository.findByCaseFileId(testCaseFile.getId());

        // Assert
        assertThat(suspects).hasSize(1);
        assertThat(suspects.get(0).getId()).isEqualTo(suspect.getId());
    }

    @Test
    @DisplayName("Recherche des suspects par nom de famille")
    void shouldFindByLastName() {
        // Arrange : deux suspects avec le même nom
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Jean", "Dupont"));
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Marie", "Dupont"));
        // Un suspect avec un autre nom
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Pierre", "Martin"));

        // Act
        List<Suspect> suspects = suspectRepository.findByLastName("Dupont");

        // Assert : on doit trouver les deux Dupont
        assertThat(suspects).hasSize(2);
        assertThat(suspects).extracting(Suspect::getFirstName)
                .containsExactlyInAnyOrder("Jean", "Marie");
    }

    @Test
    @DisplayName("Recherche des suspects par prénom")
    void shouldFindByFirstName() {
        // Arrange
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Jean", "Dupont"));
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Marie", "Durand"));
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Jean", "Martin"));

        // Act : recherche des "Jean"
        List<Suspect> suspects = suspectRepository.findByFirstName("Jean");

        // Assert : on doit trouver les deux Jean
        assertThat(suspects).hasSize(2);
        assertThat(suspects).extracting(Suspect::getLastName)
                .containsExactlyInAnyOrder("Dupont", "Martin");
    }

    @Test
    @DisplayName("Recherche des suspects par nom et prénom exacts")
    void shouldFindByLastNameAndFirstName() {
        // Arrange
        Suspect suspect = suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Jean", "Dupont"));
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Marie", "Dupont"));
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Jean", "Martin"));

        // Act
        List<Suspect> suspects = suspectRepository.findByLastNameAndFirstName("Dupont", "Jean");

        // Assert : seul Jean Dupont doit être trouvé
        assertThat(suspects).hasSize(1);
        assertThat(suspects.get(0).getId()).isEqualTo(suspect.getId());
    }

    @Test
    @DisplayName("Recherche des suspects par date de naissance exacte")
    void shouldFindByBirthDate() {
        // Arrange
        LocalDate birthDate = LocalDate.of(1985, 10, 20);

        Suspect suspect1 = suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Jean", "Dupont"));
        suspect1.setBirthDate(birthDate);
        suspectRepository.save(suspect1);

        Suspect suspect2 = suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Marie", "Durand"));
        suspect2.setBirthDate(birthDate);
        suspectRepository.save(suspect2);

        // Un suspect avec une date différente
        Suspect suspect3 = TestDataFactory.createSuspect(testCaseFile, "Pierre", "Martin");
        suspect3.setBirthDate(LocalDate.of(1990, 5, 5));
        suspectRepository.save(suspect3);

        // Act
        List<Suspect> suspects = suspectRepository.findByBirthDate(birthDate);

        // Assert : on doit trouver les deux suspects nés ce jour-là
        assertThat(suspects).hasSize(2);
        assertThat(suspects).extracting(Suspect::getLastName)
                .containsExactlyInAnyOrder("Dupont", "Durand");
    }

    @Test
    @DisplayName("Recherche des suspects par nationalité")
    void shouldFindByNationality() {
        // Arrange
        Suspect suspect1 = suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Jean", "Dupont"));
        suspect1.setNationality("Française");
        suspectRepository.save(suspect1);

        Suspect suspect2 = suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Maria", "Garcia"));
        suspect2.setNationality("Espagnole");
        suspectRepository.save(suspect2);

        Suspect suspect3 = suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "John", "Smith"));
        suspect3.setNationality("Anglaise");
        suspectRepository.save(suspect3);

        // Act
        List<Suspect> suspects = suspectRepository.findByNationality("Française");

        // Assert : un seul suspect français
        assertThat(suspects).hasSize(1);
        assertThat(suspects.get(0).getLastName()).isEqualTo("Dupont");
    }

    @Test
    @DisplayName("existsByCaseFileIdAndLastNameAndFirstName retourne vrai si le suspect existe")
    void shouldReturnTrueWhenSuspectExists() {
        // Arrange
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Jean", "Dupont"));

        // Act
        boolean exists = suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                testCaseFile.getId(), "Dupont", "Jean"
        );

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByCaseFileIdAndLastNameAndFirstName retourne faux si le suspect n'existe pas")
    void shouldReturnFalseWhenSuspectDoesNotExist() {
        // Arrange : on crée un suspect avec un autre nom
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Marie", "Durand"));

        // Act : on cherche un couple nom/prénom inexistant
        boolean exists = suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                testCaseFile.getId(), "Dupont", "Jean"
        );

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Comptage des suspects par dossier")
    void shouldCountByCaseFileId() {
        // Arrange : deux suspects sur le dossier principal
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile));
        suspectRepository.save(TestDataFactory.createSuspect(testCaseFile, "Marie", "Durand"));

        // Un suspect sur un autre dossier
        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-SUSPECT-003", "Enquête 3", CaseStatus.OPEN, CasePriority.LOW
        ));
        suspectRepository.save(TestDataFactory.createSuspect(otherCase));

        // Act
        long count = suspectRepository.countByCaseFileId(testCaseFile.getId());

        // Assert : seuls les deux suspects du dossier principal sont comptés
        assertThat(count).isEqualTo(2);
    }
}