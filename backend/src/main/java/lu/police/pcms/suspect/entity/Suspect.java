package lu.police.pcms.suspect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.common.entity.BaseEntity;

import java.time.LocalDate;


@Entity
@Table(name = "suspects")
@Getter
@Setter
@NoArgsConstructor
public class Suspect extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseFile caseFile;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

}