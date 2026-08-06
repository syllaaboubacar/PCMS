package lu.police.pcms.common.entity;

import jakarta.persistence.Column;
//import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
//@EntityListeners(AuditingEntityListener.class) //Optionnel si cette classe hérite d'une classe ou @EntityListeners est présent
public abstract class BaseEntity extends BaseCreatedEntity {

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

}