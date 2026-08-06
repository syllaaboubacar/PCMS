package lu.police.pcms.casefile.repository;

import lu.police.pcms.common.TestDataFactory;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CaseFileRepositoryTest {

    @Autowired
    private CaseFileRepository caseFileRepository;

    @Test
    @DisplayName("Sauvegarde d'un dossier")
    void shouldSaveCaseFile() {
        // Arrange
        CaseFile caseFile = TestDataFactory.createCaseFile();

        // Act
        CaseFile saved = caseFileRepository.save(caseFile);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCaseNumber()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Investigation title");
        assertThat(saved.getStatus()).isEqualTo(CaseStatus.OPEN);
        assertThat(saved.getPriority()).isEqualTo(CasePriority.MEDIUM);
    }

    @Test
    @DisplayName("Recherche par numéro de dossier")
    void shouldFindByCaseNumber() {
        // Arrange
        CaseFile caseFile = caseFileRepository.save(TestDataFactory.createCaseFile());

        // Act
        Optional<CaseFile> found = caseFileRepository.findByCaseNumber(caseFile.getCaseNumber());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getCaseNumber()).isEqualTo(caseFile.getCaseNumber());
    }

    @Test
    @DisplayName("existsByCaseNumber retourne vrai")
    void shouldReturnTrueWhenCaseNumberExists() {
        // Arrange
        CaseFile caseFile = caseFileRepository.save(TestDataFactory.createCaseFile());

        // Act
        boolean exists = caseFileRepository.existsByCaseNumber(caseFile.getCaseNumber());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Recherche par statut")
    void shouldFindByStatus() {
        // Arrange
        CaseFile openCase = TestDataFactory.createCaseFile("CASE-001", "Open Case", CaseStatus.OPEN, CasePriority.HIGH);
        CaseFile closedCase = TestDataFactory.createCaseFile("CASE-002", "Closed Case", CaseStatus.CLOSED, CasePriority.LOW);
        caseFileRepository.save(openCase);
        caseFileRepository.save(closedCase);

        // Act
        List<CaseFile> openCases = caseFileRepository.findByStatus(CaseStatus.OPEN);

        // Assert
        assertThat(openCases).hasSize(1);
        assertThat(openCases.get(0).getStatus()).isEqualTo(CaseStatus.OPEN);
    }

    @Test
    @DisplayName("Recherche par priorité")
    void shouldFindByPriority() {
        // Arrange
        CaseFile highPriority = TestDataFactory.createCaseFile("CASE-003", "High Priority", CaseStatus.OPEN, CasePriority.HIGH);
        CaseFile lowPriority = TestDataFactory.createCaseFile("CASE-004", "Low Priority", CaseStatus.OPEN, CasePriority.LOW);
        caseFileRepository.save(highPriority);
        caseFileRepository.save(lowPriority);

        // Act
        List<CaseFile> highCases = caseFileRepository.findByPriority(CasePriority.HIGH);

        // Assert
        assertThat(highCases).hasSize(1);
        assertThat(highCases.get(0).getPriority()).isEqualTo(CasePriority.HIGH);
    }

    @Test
    @DisplayName("Recherche par date d'ouverture après une date")
    void shouldFindByOpenedAtAfter() {
        // Arrange
        Instant now = Instant.now();
        CaseFile oldCase = TestDataFactory.createCaseFile();
        oldCase.setOpenedAt(now.minus(10, ChronoUnit.DAYS));
        CaseFile recentCase = TestDataFactory.createCaseFile();
        recentCase.setOpenedAt(now.minus(1, ChronoUnit.DAYS));
        caseFileRepository.save(oldCase);
        caseFileRepository.save(recentCase);

        // Act
        List<CaseFile> recentCases = caseFileRepository.findByOpenedAtAfter(now.minus(5, ChronoUnit.DAYS));

        // Assert
        assertThat(recentCases).hasSize(1);
        assertThat(recentCases.get(0).getOpenedAt()).isAfter(now.minus(5, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("Recherche par date d'ouverture entre deux dates")
    void shouldFindByOpenedAtBetween() {
        // Arrange
        Instant start = Instant.now().minus(5, ChronoUnit.DAYS);
        Instant end = Instant.now().minus(1, ChronoUnit.DAYS);
        CaseFile case1 = TestDataFactory.createCaseFile();
        case1.setOpenedAt(start.plus(1, ChronoUnit.DAYS));
        CaseFile case2 = TestDataFactory.createCaseFile();
        case2.setOpenedAt(end.minus(1, ChronoUnit.DAYS));
        caseFileRepository.save(case1);
        caseFileRepository.save(case2);

        // Act
        List<CaseFile> between = caseFileRepository.findByOpenedAtBetween(start, end);

        // Assert
        assertThat(between).hasSize(2);
    }

    @Test
    @DisplayName("Compter les dossiers par statut")
    void shouldCountByStatus() {
        // Arrange
        CaseFile open1 = TestDataFactory.createCaseFile("CASE-005", "Open1", CaseStatus.OPEN, CasePriority.MEDIUM);
        CaseFile open2 = TestDataFactory.createCaseFile("CASE-006", "Open2", CaseStatus.OPEN, CasePriority.HIGH);
        CaseFile closed = TestDataFactory.createCaseFile("CASE-007", "Closed", CaseStatus.CLOSED, CasePriority.LOW);
        caseFileRepository.save(open1);
        caseFileRepository.save(open2);
        caseFileRepository.save(closed);

        // Act
        long openCount = caseFileRepository.countByStatus(CaseStatus.OPEN);
        long closedCount = caseFileRepository.countByStatus(CaseStatus.CLOSED);

        // Assert
        assertThat(openCount).isEqualTo(2);
        assertThat(closedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Compter les dossiers par priorité")
    void shouldCountByPriority() {
        // Arrange
        CaseFile high1 = TestDataFactory.createCaseFile("CASE-008", "High1", CaseStatus.OPEN, CasePriority.HIGH);
        CaseFile high2 = TestDataFactory.createCaseFile("CASE-009", "High2", CaseStatus.OPEN, CasePriority.HIGH);
        CaseFile low = TestDataFactory.createCaseFile("CASE-010", "Low", CaseStatus.OPEN, CasePriority.LOW);
        caseFileRepository.save(high1);
        caseFileRepository.save(high2);
        caseFileRepository.save(low);

        // Act
        long highCount = caseFileRepository.countByPriority(CasePriority.HIGH);
        long lowCount = caseFileRepository.countByPriority(CasePriority.LOW);

        // Assert
        assertThat(highCount).isEqualTo(2);
        assertThat(lowCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Recherche paginée par statut (vérification simple)")
    void shouldFindByStatusWithPagination() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            CaseFile c = TestDataFactory.createCaseFile("CASE-PAGE-" + i, "Page Case", CaseStatus.OPEN, CasePriority.MEDIUM);
            caseFileRepository.save(c);
        }

        // Act – on demande une page de 2 éléments
        var pageable = org.springframework.data.domain.PageRequest.of(0, 2);
        var page = caseFileRepository.findByStatus(CaseStatus.OPEN, pageable);

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Recherche par statut et priorité (paginated)")
    void shouldFindByStatusAndPriority() {
        // Arrange
        CaseFile highOpen = TestDataFactory.createCaseFile("CASE-011", "High Open", CaseStatus.OPEN, CasePriority.HIGH);
        CaseFile lowOpen = TestDataFactory.createCaseFile("CASE-012", "Low Open", CaseStatus.OPEN, CasePriority.LOW);
        CaseFile highClosed = TestDataFactory.createCaseFile("CASE-013", "High Closed", CaseStatus.CLOSED, CasePriority.HIGH);
        caseFileRepository.save(highOpen);
        caseFileRepository.save(lowOpen);
        caseFileRepository.save(highClosed);

        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var page = caseFileRepository.findByStatusAndPriority(CaseStatus.OPEN, CasePriority.HIGH, pageable);

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(CaseStatus.OPEN);
        assertThat(page.getContent().get(0).getPriority()).isEqualTo(CasePriority.HIGH);
    }
}