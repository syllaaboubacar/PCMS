package lu.police.pcms.attachment.entity;

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

import java.time.Instant;

import org.hibernate.annotations.Check;

@SuppressWarnings("deprecation")

@Entity
@Table(name = "attachments")
@Check(constraints = "file_size >= 0")
@Getter
@Setter
@NoArgsConstructor
public class Attachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseFile caseFile;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

}