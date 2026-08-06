package lu.police.pcms.common;

import lu.police.pcms.attachment.entity.Attachment;
import lu.police.pcms.audit.entity.AuditLog;
import lu.police.pcms.caseassignment.entity.CaseAssignment;
import lu.police.pcms.casecomment.entity.CaseComment;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import lu.police.pcms.department.entity.Department;
import lu.police.pcms.role.entity.Role;
import lu.police.pcms.suspect.entity.Suspect;
import lu.police.pcms.user.entity.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Factory pour créer des instances d'entités avec des données minimales
 * pour les tests d'intégration.
 *
 * <p>
 * Toutes les méthodes retournent des objets non persistés.
 * Les champs obligatoires sont systématiquement renseignés.
 * Les champs facultatifs peuvent être laissés à {@code null}.
 * </p>
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // Empêche l'instanciation
    }

    /*
     * ============================================================
     * ROLE
     * ============================================================
     */

    /**
     * Crée un rôle avec le nom "ROLE_ADMIN".
     *
     * @return une instance de {@link Role}
     */
    public static Role createRole() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        return role;
    }

    /**
     * Crée un rôle avec un nom personnalisé.
     *
     * @param name le nom du rôle
     * @return une instance de {@link Role}
     */
    public static Role createRole(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }

    /*
     * ============================================================
     * DEPARTMENT
     * ============================================================
     */

    /**
     * Crée un département avec le code "INV" et le nom "Investigations".
     *
     * @return une instance de {@link Department}
     */
    public static Department createDepartment() {
        Department department = new Department();
        department.setCode("INV");
        department.setName("Investigations");
        return department;
    }

    /**
     * Crée un département avec un code et un nom personnalisés.
     *
     * @param code code du département
     * @param name nom du département
     * @return une instance de {@link Department}
     */
    public static Department createDepartment(String code, String name) {
        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        return department;
    }

    /*
     * ============================================================
     * USER
     * ============================================================
     */

    /**
     * Crée un utilisateur avec des valeurs par défaut.
     * Nécessite un rôle et un département valides.
     *
     * @param role      rôle de l'utilisateur
     * @param department département de l'utilisateur
     * @return une instance de {@link User}
     */
    public static User createUser(Role role, Department department) {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        // Génère un email unique
        user.setEmail("user-" + UUID.randomUUID() + "@pcms.lu");
        user.setPassword("password");
        user.setEnabled(true);
        user.setRole(role);
        user.setDepartment(department);
        return user;
    }

    /**
     * Crée un utilisateur avec des valeurs personnalisées.
     *
     * @param firstName  prénom
     * @param lastName   nom
     * @param email      adresse email (doit être unique)
     * @param role       rôle
     * @param department département
     * @return une instance de {@link User}
     */
    public static User createUser(String firstName, String lastName, String email,
                                  Role role, Department department) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("password");
        user.setEnabled(true);
        user.setRole(role);
        user.setDepartment(department);
        return user;
    }

    /*
     * ============================================================
     * CASE FILE
     * ============================================================
     */

    /**
     * Crée un dossier d'enquête avec des valeurs par défaut.
     * Le numéro de dossier est généré aléatoirement.
     *
     * @return une instance de {@link CaseFile}
     */
    public static CaseFile createCaseFile() {
        CaseFile caseFile = new CaseFile();
        caseFile.setCaseNumber("CASE-" + UUID.randomUUID().toString().substring(0, 8));
        caseFile.setTitle("Investigation title");
        caseFile.setDescription("Description of the investigation");
        caseFile.setStatus(CaseStatus.OPEN);
        caseFile.setPriority(CasePriority.MEDIUM);
        caseFile.setOpenedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        caseFile.setIncidentDate(LocalDate.now().minusDays(2));
        caseFile.setLocation("Luxembourg City");
        return caseFile;
    }

    /**
     * Crée un dossier avec des valeurs personnalisées.
     *
     * @param caseNumber numéro de dossier
     * @param title      titre
     * @param status     statut
     * @param priority   priorité
     * @return une instance de {@link CaseFile}
     */
    public static CaseFile createCaseFile(String caseNumber, String title,
                                          CaseStatus status, CasePriority priority) {
        CaseFile caseFile = new CaseFile();
        caseFile.setCaseNumber(caseNumber);
        caseFile.setTitle(title);
        caseFile.setDescription("Description for " + title);
        caseFile.setStatus(status);
        caseFile.setPriority(priority);
        caseFile.setOpenedAt(Instant.now());
        return caseFile;
    }

    /*
     * ============================================================
     * CASE ASSIGNMENT
     * ============================================================
     */

    /**
     * Crée une affectation avec les paramètres obligatoires.
     *
     * @param caseFile dossier à affecter
     * @param user     enquêteur
     * @return une instance de {@link CaseAssignment}
     */
    public static CaseAssignment createCaseAssignment(CaseFile caseFile, User user) {
        CaseAssignment assignment = new CaseAssignment();
        assignment.setCaseFile(caseFile);
        assignment.setUser(user);
        assignment.setAssignedAt(Instant.now());
        assignment.setActive(true);
        return assignment;
    }

    /**
     * Crée une affectation avec une date d'affectation personnalisée.
     *
     * @param caseFile   dossier
     * @param user       enquêteur
     * @param assignedAt date d'affectation
     * @param active     état actif
     * @return une instance de {@link CaseAssignment}
     */
    public static CaseAssignment createCaseAssignment(CaseFile caseFile, User user,
                                                      Instant assignedAt, boolean active) {
        CaseAssignment assignment = new CaseAssignment();
        assignment.setCaseFile(caseFile);
        assignment.setUser(user);
        assignment.setAssignedAt(assignedAt);
        assignment.setActive(active);
        return assignment;
    }

    /*
     * ============================================================
     * SUSPECT
     * ============================================================
     */

    /**
     * Crée un suspect avec des valeurs par défaut.
     *
     * @param caseFile dossier auquel le suspect est associé
     * @return une instance de {@link Suspect}
     */
    public static Suspect createSuspect(CaseFile caseFile) {
        Suspect suspect = new Suspect();
        suspect.setCaseFile(caseFile);
        suspect.setFirstName("Jean");
        suspect.setLastName("Dupont");
        suspect.setBirthDate(LocalDate.of(1980, 5, 15));
        suspect.setNationality("Française");
        suspect.setNotes("Suspect principal");
        return suspect;
    }

    /**
     * Crée un suspect avec des valeurs personnalisées.
     *
     * @param caseFile  dossier
     * @param firstName prénom
     * @param lastName  nom
     * @return une instance de {@link Suspect}
     */
    public static Suspect createSuspect(CaseFile caseFile, String firstName, String lastName) {
        Suspect suspect = new Suspect();
        suspect.setCaseFile(caseFile);
        suspect.setFirstName(firstName);
        suspect.setLastName(lastName);
        suspect.setNationality("Inconnue");
        return suspect;
    }

    /*
     * ============================================================
     * ATTACHMENT
     * ============================================================
     */

    /**
     * Crée une pièce jointe avec des valeurs par défaut.
     *
     * @param caseFile dossier associé
     * @return une instance de {@link Attachment}
     */
    public static Attachment createAttachment(CaseFile caseFile) {
        Attachment attachment = new Attachment();
        attachment.setCaseFile(caseFile);
        attachment.setFilename("file-" + UUID.randomUUID() + ".pdf");
        attachment.setOriginalFilename("report.pdf");
        attachment.setMimeType("application/pdf");
        attachment.setFileSize(1024L);
        attachment.setStoragePath("/uploads/" + attachment.getFilename());
        attachment.setType("DOCUMENT");
        attachment.setUploadedAt(Instant.now());
        return attachment;
    }

    /**
     * Crée une pièce jointe avec des valeurs personnalisées.
     *
     * @param caseFile           dossier
     * @param originalFilename   nom d'origine
     * @param mimeType           type MIME
     * @param fileSize           taille en octets
     * @param type               type métier (PHOTO, VIDEO, etc.)
     * @return une instance de {@link Attachment}
     */
    public static Attachment createAttachment(CaseFile caseFile, String originalFilename,
                                              String mimeType, long fileSize, String type) {
        Attachment attachment = new Attachment();
        attachment.setCaseFile(caseFile);
        String uuid = UUID.randomUUID().toString();
        attachment.setFilename(uuid + "-" + originalFilename);
        attachment.setOriginalFilename(originalFilename);
        attachment.setMimeType(mimeType);
        attachment.setFileSize(fileSize);
        attachment.setStoragePath("/uploads/" + uuid);
        attachment.setType(type);
        attachment.setUploadedAt(Instant.now());
        return attachment;
    }

    /*
     * ============================================================
     * CASE COMMENT
     * ============================================================
     */

    /**
     * Crée un commentaire avec des valeurs par défaut.
     *
     * @param caseFile dossier concerné
     * @param user     auteur du commentaire
     * @return une instance de {@link CaseComment}
     */
    public static CaseComment createCaseComment(CaseFile caseFile, User user) {
        CaseComment comment = new CaseComment();
        comment.setCaseFile(caseFile);
        comment.setUser(user);
        comment.setContent("Ceci est un commentaire de test.");
        return comment;
    }

    /**
     * Crée un commentaire avec un contenu personnalisé.
     *
     * @param caseFile dossier
     * @param user     auteur
     * @param content  texte du commentaire
     * @return une instance de {@link CaseComment}
     */
    public static CaseComment createCaseComment(CaseFile caseFile, User user, String content) {
        CaseComment comment = new CaseComment();
        comment.setCaseFile(caseFile);
        comment.setUser(user);
        comment.setContent(content);
        return comment;
    }

    /*
     * ============================================================
     * AUDIT LOG
     * ============================================================
     */

    /**
     * Crée un log d'audit avec des valeurs par défaut.
     *
     * @param user utilisateur à l'origine de l'action
     * @return une instance de {@link AuditLog}
     */
    public static AuditLog createAuditLog(User user) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction("CREATE");
        log.setEntityName("Role");
        log.setEntityId(1L);
        log.setDetails("Creation d'un rôle ADMIN");
        log.setIpAddress("127.0.0.1");
        return log;
    }

    /**
     * Crée un log d'audit avec des valeurs personnalisées.
     *
     * @param user       utilisateur
     * @param action     action effectuée
     * @param entityName nom de l'entité
     * @param entityId   identifiant de l'entité
     * @param details    détails supplémentaires
     * @return une instance de {@link AuditLog}
     */
    public static AuditLog createAuditLog(User user, String action, String entityName,
                                          Long entityId, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress("192.168.1.1");
        return log;
    }
}