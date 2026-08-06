package lu.police.pcms.department.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lu.police.pcms.common.entity.BaseEntity;
import lu.police.pcms.user.entity.User;

@Entity
@Table(
    name = "departments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_departments_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_departments_name", columnNames = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {

    @Column(
            nullable = false,
            unique = true,
            length = 20
    )
    private String code;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String name;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<User> users;

}