package lu.police.pcms.role.entity;

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
    name = "roles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity {

    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String name;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<User> users;

}