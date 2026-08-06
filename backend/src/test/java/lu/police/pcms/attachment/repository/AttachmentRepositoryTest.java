package lu.police.pcms.attachment.repository;

import lu.police.pcms.common.TestDataFactory;
import lu.police.pcms.attachment.entity.Attachment;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import lu.police.pcms.casefile.repository.CaseFileRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du repository {@link AttachmentRepository}.
 * <p>
 * Vérifie toutes les méthodes du repository : CRUD, recherches par dossier,
 * par type, par type MIME, par chemin de stockage, par dates de téléversement,
 * par état de suppression (logique), comptages et existences.
 * </p>
 * <p>
 * Utilise {@link DataJpaTest} pour la couche JPA et {@link AutoConfigureTestDatabase}
 * avec {@code Replace.NONE} pour utiliser la base PostgreSQL de test.
 * </p>
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttachmentRepositoryTest {

    // ========================================================================
    // 1. Injections des repositories
    // ========================================================================

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private CaseFileRepository caseFileRepository;

    // ========================================================================
    // 2. Données partagées
    // ========================================================================

    private CaseFile testCaseFile; // Dossier de référence pour les pièces jointes

    /**
     * Crée et persiste un dossier avant chaque test.
     * Ce dossier servira de parent pour toutes les pièces jointes créées.
     */
    @BeforeEach
    void setUp() {
        testCaseFile = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-ATTACH-001", "Dossier avec pièces jointes", CaseStatus.OPEN, CasePriority.MEDIUM
        ));
    }

    // ========================================================================
    // 3. Tests des méthodes
    // ========================================================================

    /**
     * Test de la méthode {@link AttachmentRepository#save(Object)}.
     * Vérifie que tous les champs obligatoires sont bien renseignés après la sauvegarde.
     */
    @Test
    @DisplayName("Sauvegarde d'une pièce jointe")
    void shouldSaveAttachment() {
        // Arrange : création d'une pièce jointe non persistée
        Attachment attachment = TestDataFactory.createAttachment(testCaseFile);

        // Act : sauvegarde
        Attachment saved = attachmentRepository.save(attachment);

        // Assert : vérifications
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCaseFile()).isEqualTo(testCaseFile);
        assertThat(saved.getFilename()).isNotBlank();
        assertThat(saved.getOriginalFilename()).isEqualTo("report.pdf");
        assertThat(saved.getMimeType()).isEqualTo("application/pdf");
        assertThat(saved.getFileSize()).isEqualTo(1024L);
        assertThat(saved.getStoragePath()).isNotBlank();
        assertThat(saved.getType()).isEqualTo("DOCUMENT");
        assertThat(saved.getUploadedAt()).isNotNull();
        assertThat(saved.getDeleted()).isFalse(); // Par défaut
    }

    /**
     * Test de {@link AttachmentRepository#findByCaseFile(CaseFile)}.
     * Vérifie que la recherche par dossier retourne toutes les pièces jointes de ce dossier.
     */
    @Test
    @DisplayName("Recherche des pièces jointes par dossier")
    void shouldFindByCaseFile() {
        // Arrange : on crée deux pièces jointes sur le dossier principal
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "photo.jpg", "image/jpeg", 2048L, "PHOTO"));

        // On crée une pièce jointe sur un autre dossier pour s'assurer qu'elle n'est pas remontée
        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-ATTACH-002", "Autre dossier", CaseStatus.OPEN, CasePriority.LOW
        ));
        attachmentRepository.save(TestDataFactory.createAttachment(otherCase));

        // Act : recherche des pièces jointes du dossier principal
        List<Attachment> attachments = attachmentRepository.findByCaseFile(testCaseFile);

        // Assert : on doit obtenir les deux pièces jointes du dossier principal
        assertThat(attachments).hasSize(2);
        assertThat(attachments).extracting(Attachment::getOriginalFilename)
                .containsExactlyInAnyOrder("report.pdf", "photo.jpg");
    }

    /**
     * Test de {@link AttachmentRepository#findByCaseFileId(Long)}.
     * Variante utilisant l'identifiant du dossier.
     */
    @Test
    @DisplayName("Recherche des pièces jointes par ID de dossier")
    void shouldFindByCaseFileId() {
        // Arrange
        Attachment attachment = attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));

        // Act
        List<Attachment> attachments = attachmentRepository.findByCaseFileId(testCaseFile.getId());

        // Assert
        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).getId()).isEqualTo(attachment.getId());
    }

    /**
     * Test de {@link AttachmentRepository#findByType(String)}.
     * Vérifie le filtrage par type métier (DOCUMENT, PHOTO, etc.).
     */
    @Test
    @DisplayName("Recherche des pièces jointes par type")
    void shouldFindByType() {
        // Arrange : on crée des pièces jointes de différents types
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "doc1.pdf", "application/pdf", 100L, "DOCUMENT"));
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "photo1.jpg", "image/jpeg", 200L, "PHOTO"));
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "doc2.pdf", "application/pdf", 150L, "DOCUMENT"));

        // Act : recherche des pièces jointes de type "DOCUMENT"
        List<Attachment> documents = attachmentRepository.findByType("DOCUMENT");

        // Assert : on doit trouver les deux documents
        assertThat(documents).hasSize(2);
        assertThat(documents).extracting(Attachment::getOriginalFilename)
                .containsExactlyInAnyOrder("doc1.pdf", "doc2.pdf");
    }

    /**
     * Test de {@link AttachmentRepository#findByMimeType(String)}.
     * Vérifie le filtrage par type MIME.
     */
    @Test
    @DisplayName("Recherche des pièces jointes par type MIME")
    void shouldFindByMimeType() {
        // Arrange : on crée des pièces jointes avec différents types MIME
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "doc.pdf", "application/pdf", 100L, "DOCUMENT"));
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "photo.jpg", "image/jpeg", 200L, "PHOTO"));
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "image.png", "image/png", 300L, "PHOTO"));

        // Act : recherche des pièces jointes de type MIME "image/jpeg"
        List<Attachment> images = attachmentRepository.findByMimeType("image/jpeg");

        // Assert : on doit trouver une seule pièce jointe (photo.jpg)
        assertThat(images).hasSize(1);
        assertThat(images.get(0).getOriginalFilename()).isEqualTo("photo.jpg");
    }

    /**
     * Test de {@link AttachmentRepository#findByStoragePath(String)}.
     * Vérifie la recherche par chemin de stockage (unique).
     */
    @Test
    @DisplayName("Recherche d'une pièce jointe par chemin de stockage")
    void shouldFindByStoragePath() {
        // Arrange
        Attachment attachment = attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));

        // Act
        Optional<Attachment> found = attachmentRepository.findByStoragePath(attachment.getStoragePath());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(attachment.getId());
    }

    /**
     * Test de {@link AttachmentRepository#findByUploadedAtAfter(Instant)}.
     * Vérifie que seules les pièces jointes téléversées après une date donnée sont retournées.
     */
    @Test
    @DisplayName("Recherche des pièces jointes par date de téléversement après une date")
    void shouldFindByUploadedAtAfter() {
        // Arrange
        Instant now = Instant.now();
        Attachment old = TestDataFactory.createAttachment(testCaseFile);
        old.setUploadedAt(now.minus(10, ChronoUnit.DAYS)); // 10 jours avant
        attachmentRepository.save(old);

        Attachment recent = TestDataFactory.createAttachment(testCaseFile);
        recent.setUploadedAt(now.minus(1, ChronoUnit.DAYS)); // 1 jour avant
        attachmentRepository.save(recent);

        // Act : on recherche les pièces jointes téléversées après une date (il y a 5 jours)
        List<Attachment> recentAttachments = attachmentRepository.findByUploadedAtAfter(now.minus(5, ChronoUnit.DAYS));

        // Assert : seule la pièce récente doit être retournée
        assertThat(recentAttachments).hasSize(1);
        assertThat(recentAttachments.get(0).getUploadedAt()).isAfter(now.minus(5, ChronoUnit.DAYS));
    }

    /**
     * Test de {@link AttachmentRepository#findByUploadedAtBetween(Instant, Instant)}.
     * Vérifie le filtrage par plage de dates.
     */
    @Test
    @DisplayName("Recherche des pièces jointes par plage de dates")
    void shouldFindByUploadedAtBetween() {
        // Arrange : on définit une plage de dates [start, end]
        Instant start = Instant.now().minus(5, ChronoUnit.DAYS);
        Instant end = Instant.now().minus(1, ChronoUnit.DAYS);

        // Pièce dans la plage
        Attachment inRange = TestDataFactory.createAttachment(testCaseFile);
        inRange.setUploadedAt(start.plus(1, ChronoUnit.DAYS)); // +1 jour après start
        attachmentRepository.save(inRange);

        // Pièce avant la plage
        Attachment before = TestDataFactory.createAttachment(testCaseFile);
        before.setUploadedAt(start.minus(1, ChronoUnit.DAYS)); // -1 jour avant start
        attachmentRepository.save(before);

        // Pièce après la plage
        Attachment after = TestDataFactory.createAttachment(testCaseFile);
        after.setUploadedAt(end.plus(1, ChronoUnit.DAYS)); // +1 jour après end
        attachmentRepository.save(after);

        // Act : recherche dans la plage
        List<Attachment> between = attachmentRepository.findByUploadedAtBetween(start, end);

        // Assert : seule la pièce dans la plage doit être retournée
        assertThat(between).hasSize(1);
        assertThat(between.get(0).getUploadedAt()).isBetween(start, end);
    }

    /**
     * Test de {@link AttachmentRepository#findByDeletedFalse()}.
     * Vérifie que seules les pièces jointes non supprimées sont retournées.
     */
    @Test
    @DisplayName("Recherche des pièces jointes non supprimées")
    void shouldFindByDeletedFalse() {
        // Arrange : on sauvegarde une pièce active (deleted=false par défaut)
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));

        // On sauvegarde une pièce supprimée logiquement
        Attachment deleted = TestDataFactory.createAttachment(testCaseFile);
        deleted.setDeleted(true);
        attachmentRepository.save(deleted);

        // Act : recherche des pièces non supprimées
        List<Attachment> active = attachmentRepository.findByDeletedFalse();

        // Assert : on doit obtenir uniquement la pièce active
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getDeleted()).isFalse();
    }

    /**
     * Test de {@link AttachmentRepository#findByDeletedTrue()}.
     * Vérifie que seules les pièces jointes supprimées sont retournées.
     */
    @Test
    @DisplayName("Recherche des pièces jointes supprimées")
    void shouldFindByDeletedTrue() {
        // Arrange : une pièce active
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));

        // Une pièce supprimée
        Attachment deleted = TestDataFactory.createAttachment(testCaseFile);
        deleted.setDeleted(true);
        attachmentRepository.save(deleted);

        // Act : recherche des pièces supprimées
        List<Attachment> deletedAttachments = attachmentRepository.findByDeletedTrue();

        // Assert : on doit obtenir la pièce supprimée
        assertThat(deletedAttachments).hasSize(1);
        assertThat(deletedAttachments.get(0).getDeleted()).isTrue();
    }

    /**
     * Test de {@link AttachmentRepository#countByCaseFileId(Long)}.
     * Vérifie le comptage des pièces jointes par dossier.
     */
    @Test
    @DisplayName("Comptage des pièces jointes par dossier")
    void shouldCountByCaseFileId() {
        // Arrange : on crée deux pièces jointes sur le dossier principal
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));

        // Et une sur un autre dossier
        CaseFile otherCase = caseFileRepository.save(TestDataFactory.createCaseFile(
                "CASE-ATTACH-003", "Autre dossier", CaseStatus.OPEN, CasePriority.HIGH
        ));
        attachmentRepository.save(TestDataFactory.createAttachment(otherCase));

        // Act : comptage sur le dossier principal
        long count = attachmentRepository.countByCaseFileId(testCaseFile.getId());

        // Assert : on doit avoir 2
        assertThat(count).isEqualTo(2);
    }

    /**
     * Test de {@link AttachmentRepository#findByFilename(String)}.
     * Recherche par nom de fichier interne (unique).
     */
    @Test
    @DisplayName("Recherche d'une pièce jointe par nom de fichier interne")
    void shouldFindByFilename() {
        // Arrange
        Attachment attachment = attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));

        // Act
        Optional<Attachment> found = attachmentRepository.findByFilename(attachment.getFilename());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(attachment.getId());
    }

    /**
     * Test de {@link AttachmentRepository#findByOriginalFilename(String)}.
     * Recherche par nom d'origine (peut retourner plusieurs résultats).
     */
    @Test
    @DisplayName("Recherche des pièces jointes par nom d'origine")
    void shouldFindByOriginalFilename() {
        // Arrange : deux pièces avec le même nom d'origine "doc.pdf"
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "doc.pdf", "application/pdf", 100L, "DOCUMENT"));
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "photo.jpg", "image/jpeg", 200L, "PHOTO"));
        attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile, "doc.pdf", "application/pdf", 150L, "DOCUMENT"));

        // Act
        List<Attachment> docs = attachmentRepository.findByOriginalFilename("doc.pdf");

        // Assert : on doit trouver les deux "doc.pdf"
        assertThat(docs).hasSize(2);
        assertThat(docs).extracting(Attachment::getMimeType)
                .containsOnly("application/pdf");
    }

    /**
     * Test de {@link AttachmentRepository#existsByFilename(String)}.
     * Vérifie l'existence d'une pièce jointe par son nom de fichier interne.
     */
    @Test
    @DisplayName("existsByFilename retourne vrai si le nom de fichier existe")
    void shouldReturnTrueWhenFilenameExists() {
        // Arrange
        Attachment attachment = attachmentRepository.save(TestDataFactory.createAttachment(testCaseFile));

        // Act
        boolean exists = attachmentRepository.existsByFilename(attachment.getFilename());

        // Assert
        assertThat(exists).isTrue();
    }

    /**
     * Test de {@link AttachmentRepository#existsByFilename(String)}.
     * Vérifie que false est retourné pour un nom de fichier inexistant.
     */
    @Test
    @DisplayName("existsByFilename retourne faux si le nom de fichier n'existe pas")
    void shouldReturnFalseWhenFilenameDoesNotExist() {
        // Act
        boolean exists = attachmentRepository.existsByFilename("non-existent-file.pdf");

        // Assert
        assertThat(exists).isFalse();
    }
}