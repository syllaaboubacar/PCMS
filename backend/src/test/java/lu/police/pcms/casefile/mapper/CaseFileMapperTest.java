package lu.police.pcms.casefile.mapper;

import lu.police.pcms.casefile.dto.CaseFileResponse;
import lu.police.pcms.casefile.dto.CreateCaseFileRequest;
import lu.police.pcms.casefile.dto.PatchCaseFileRequest;
import lu.police.pcms.casefile.dto.UpdateCaseFileRequest;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CaseFileMapperTest {

    private final CaseFileMapper mapper = Mappers.getMapper(CaseFileMapper.class);

    @Test
    @DisplayName("Conversion CreateCaseFileRequest → CaseFile")
    void shouldMapCreateRequestToEntity() {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now();

        CreateCaseFileRequest request = new CreateCaseFileRequest(
                "PCMS_CASE_001",
                "Enquête initiale",
                "Description de l'enquête",
                CaseStatus.OPEN,
                CasePriority.HIGH,
                now,
                today,
                "Luxembourg"
        );

        CaseFile entity = mapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getCaseNumber()).isEqualTo("PCMS_CASE_001");
        assertThat(entity.getTitle()).isEqualTo("Enquête initiale");
        assertThat(entity.getDescription()).isEqualTo("Description de l'enquête");
        assertThat(entity.getStatus()).isEqualTo(CaseStatus.OPEN);
        assertThat(entity.getPriority()).isEqualTo(CasePriority.HIGH);
        assertThat(entity.getOpenedAt()).isEqualTo(now);
        assertThat(entity.getIncidentDate()).isEqualTo(today);
        assertThat(entity.getLocation()).isEqualTo("Luxembourg");
        assertThat(entity.getClosedAt()).isNull();
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("Conversion CaseFile → CaseFileResponse")
    void shouldMapEntityToResponse() {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now();

        CaseFile entity = new CaseFile();
        entity.setId(1L);
        entity.setCaseNumber("PCMS_CASE_001");
        entity.setTitle("Enquête initiale");
        entity.setDescription("Description de l'enquête");
        entity.setStatus(CaseStatus.OPEN);
        entity.setPriority(CasePriority.HIGH);
        entity.setOpenedAt(now);
        entity.setClosedAt(now.plus(10, ChronoUnit.DAYS));
        entity.setIncidentDate(today);
        entity.setLocation("Luxembourg");

        CaseFileResponse response = mapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCaseNumber()).isEqualTo("PCMS_CASE_001");
        assertThat(response.getTitle()).isEqualTo("Enquête initiale");
        assertThat(response.getDescription()).isEqualTo("Description de l'enquête");
        assertThat(response.getStatus()).isEqualTo(CaseStatus.OPEN);
        assertThat(response.getPriority()).isEqualTo(CasePriority.HIGH);
        assertThat(response.getOpenedAt()).isEqualTo(now);
        assertThat(response.getClosedAt()).isEqualTo(now.plus(10, ChronoUnit.DAYS));
        assertThat(response.getIncidentDate()).isEqualTo(today);
        assertThat(response.getLocation()).isEqualTo("Luxembourg");
    }

    @Test
    @DisplayName("Mise à jour complète UpdateCaseFileRequest → CaseFile (PUT)")
    void shouldUpdateEntity() {
        CaseFile entity = new CaseFile();
        entity.setId(1L);
        entity.setCaseNumber("PCMS_CASE_001");
        entity.setTitle("Ancien titre");
        entity.setDescription("Ancienne description");
        entity.setStatus(CaseStatus.OPEN);
        entity.setPriority(CasePriority.LOW);
        entity.setOpenedAt(Instant.now().minus(5, ChronoUnit.DAYS));

        Instant now = Instant.now();
        LocalDate today = LocalDate.now();

        UpdateCaseFileRequest request = new UpdateCaseFileRequest(
                "Nouveau titre",
                "Nouvelle description",
                CaseStatus.IN_PROGRESS,
                CasePriority.HIGH,
                now,
                today,
                "Paris"
        );

        mapper.updateEntity(request, entity);

        assertThat(entity.getTitle()).isEqualTo("Nouveau titre");
        assertThat(entity.getDescription()).isEqualTo("Nouvelle description");
        assertThat(entity.getStatus()).isEqualTo(CaseStatus.IN_PROGRESS);
        assertThat(entity.getPriority()).isEqualTo(CasePriority.HIGH);
        assertThat(entity.getClosedAt()).isEqualTo(now);
        assertThat(entity.getIncidentDate()).isEqualTo(today);
        assertThat(entity.getLocation()).isEqualTo("Paris");
        assertThat(entity.getCaseNumber()).isEqualTo("PCMS_CASE_001");
        assertThat(entity.getOpenedAt()).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Mise à jour partielle PatchCaseFileRequest → CaseFile (PATCH)")
    void shouldPatchEntity() {
        Instant now = Instant.now();

        CaseFile entity = new CaseFile();
        entity.setId(1L);
        entity.setCaseNumber("PCMS_CASE_001");
        entity.setTitle("Titre original");
        entity.setDescription("Description originale");
        entity.setStatus(CaseStatus.OPEN);
        entity.setPriority(CasePriority.MEDIUM);
        entity.setOpenedAt(now.minus(5, ChronoUnit.DAYS));
        entity.setLocation("Luxembourg");

        PatchCaseFileRequest request = new PatchCaseFileRequest(
                "Titre modifié",
                null,
                null,
                null,
                null,
                null,
                null
        );

        mapper.patchEntity(request, entity);

        assertThat(entity.getTitle()).isEqualTo("Titre modifié");
        assertThat(entity.getDescription()).isEqualTo("Description originale");
        assertThat(entity.getStatus()).isEqualTo(CaseStatus.OPEN);
        assertThat(entity.getPriority()).isEqualTo(CasePriority.MEDIUM);
        assertThat(entity.getLocation()).isEqualTo("Luxembourg");
        assertThat(entity.getCaseNumber()).isEqualTo("PCMS_CASE_001");
        assertThat(entity.getOpenedAt()).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Patch avec tous les champs à null ne modifie rien")
    void shouldPatchWithNullsDoNothing() {
        CaseFile entity = new CaseFile();
        entity.setId(1L);
        entity.setCaseNumber("PCMS_CASE_001");
        entity.setTitle("Titre original");
        entity.setDescription("Description originale");
        entity.setStatus(CaseStatus.OPEN);
        entity.setPriority(CasePriority.MEDIUM);

        PatchCaseFileRequest request = new PatchCaseFileRequest(
                null, null, null, null, null, null, null
        );

        mapper.patchEntity(request, entity);

        assertThat(entity.getTitle()).isEqualTo("Titre original");
        assertThat(entity.getDescription()).isEqualTo("Description originale");
        assertThat(entity.getStatus()).isEqualTo(CaseStatus.OPEN);
        assertThat(entity.getPriority()).isEqualTo(CasePriority.MEDIUM);
        assertThat(entity.getCaseNumber()).isEqualTo("PCMS_CASE_001");
        assertThat(entity.getId()).isEqualTo(1L);
    }
}