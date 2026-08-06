package lu.police.pcms.caseassignment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.common.entity.BaseEntity;
import lu.police.pcms.user.entity.User;

import java.time.Instant;

@Entity
@Table(
    name = "case_assignments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_assignment", columnNames = {"case_id", "user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class CaseAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseFile caseFile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

}