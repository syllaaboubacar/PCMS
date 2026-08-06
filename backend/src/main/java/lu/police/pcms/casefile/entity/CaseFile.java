package lu.police.pcms.casefile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lu.police.pcms.attachment.entity.Attachment;
import lu.police.pcms.caseassignment.entity.CaseAssignment;
import lu.police.pcms.casecomment.entity.CaseComment;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import lu.police.pcms.common.entity.BaseEntity;
import lu.police.pcms.suspect.entity.Suspect;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import org.hibernate.annotations.Check;

@SuppressWarnings("deprecation")

@Entity
@Table(
    name = "cases",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cases_number", columnNames = "case_number")
    }
)
@Check(constraints = "status IN ('OPEN','IN_PROGRESS','ON_HOLD','CLOSED','ARCHIVED')")
@Check(constraints = "priority IN ('LOW','MEDIUM','HIGH','CRITICAL')")
@Getter
@Setter
@NoArgsConstructor
public class CaseFile extends BaseEntity {

    @Column(name = "case_number", nullable = false, unique = true, length = 30)
    private String caseNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CaseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private CasePriority priority;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "incident_date")
    private LocalDate incidentDate;

    @Column(name = "location", length = 255)
    private String location;

    @OneToMany(mappedBy = "caseFile", fetch = FetchType.LAZY)
    private Set<CaseAssignment> caseAssignments;

    @OneToMany(mappedBy = "caseFile", fetch = FetchType.LAZY)
    private Set<Suspect> suspects;

    @OneToMany(mappedBy = "caseFile", fetch = FetchType.LAZY)
    private Set<Attachment> attachments;

    @OneToMany(mappedBy = "caseFile", fetch = FetchType.LAZY)
    private Set<CaseComment> caseComments;


}