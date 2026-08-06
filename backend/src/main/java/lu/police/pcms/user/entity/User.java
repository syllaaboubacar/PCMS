package lu.police.pcms.user.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lu.police.pcms.audit.entity.AuditLog;
import lu.police.pcms.caseassignment.entity.CaseAssignment;
import lu.police.pcms.casecomment.entity.CaseComment;
import lu.police.pcms.common.entity.BaseEntity;
import lu.police.pcms.department.entity.Department;
import lu.police.pcms.role.entity.Role;
import lu.police.pcms.user.entity.User;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;    

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<CaseAssignment> caseAssignments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<CaseComment> caseComments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<AuditLog> auditLogs;

}