package lu.police.pcms.casefile.service;

import lu.police.pcms.casefile.dto.CaseFileResponse;
import lu.police.pcms.casefile.dto.CreateCaseFileRequest;
import lu.police.pcms.casefile.dto.PatchCaseFileRequest;
import lu.police.pcms.casefile.dto.UpdateCaseFileRequest;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import lu.police.pcms.casefile.mapper.CaseFileMapper;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service CaseFileService")
class CaseFileServiceTest {

    @Mock
    private CaseFileRepository caseFileRepository;

    @Mock
    private CaseFileMapper caseFileMapper;

    @InjectMocks
    private CaseFileService caseFileService;

    // ========== DONNÉES DE TEST ==========

    private static final Long CASE_ID = 1L;
    private static final String CASE_NUMBER = "PCMS_CASE_001";

    private CaseFile mockCaseFile(boolean deleted) {
        CaseFile cf = new CaseFile();
        cf.setId(CASE_ID);
        cf.setCaseNumber(CASE_NUMBER);
        cf.setTitle("Enquête initiale");
        cf.setDescription("Description de l'enquête");
        cf.setStatus(CaseStatus.OPEN);
        cf.setPriority(CasePriority.MEDIUM);
        cf.setOpenedAt(Instant.now());
        cf.setIncidentDate(LocalDate.now());
        cf.setLocation("Luxembourg");
        cf.setDeleted(deleted);
        return cf;
    }

    private CaseFileResponse mockResponse(CaseFile cf) {
        CaseFileResponse response = new CaseFileResponse();
        response.setId(cf.getId());
        response.setCaseNumber(cf.getCaseNumber());
        response.setTitle(cf.getTitle());
        response.setDescription(cf.getDescription());
        response.setStatus(cf.getStatus());
        response.setPriority(cf.getPriority());
        response.setOpenedAt(cf.getOpenedAt());
        response.setClosedAt(cf.getClosedAt());
        response.setIncidentDate(cf.getIncidentDate());
        response.setLocation(cf.getLocation());
        response.setDeleted(cf.getDeleted());
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'un dossier avec succès")
    void shouldCreateCaseFileSuccessfully() {
        // Arrange
        CreateCaseFileRequest request = new CreateCaseFileRequest(
                CASE_NUMBER,
                "Enquête initiale",
                "Description",
                CaseStatus.OPEN,
                CasePriority.HIGH,
                Instant.now(),
                LocalDate.now(),
                "Luxembourg"
        );
        CaseFile entity = new CaseFile();
        CaseFile saved = mockCaseFile(false);
        CaseFileResponse expected = mockResponse(saved);

        when(caseFileRepository.existsByCaseNumber(CASE_NUMBER)).thenReturn(false);
        when(caseFileMapper.toEntity(request)).thenReturn(entity);
        when(caseFileRepository.save(entity)).thenReturn(saved);
        when(caseFileMapper.toResponse(saved)).thenReturn(expected);

        // Act
        CaseFileResponse actual = caseFileService.createCaseFile(request);

        // Assert
        assertThat(actual).isEqualTo(expected);
        verify(caseFileRepository).save(entity);
    }

    @Test
    @DisplayName("Création avec numéro existant → exception")
    void shouldThrowExceptionWhenCaseNumberExists() {
        CreateCaseFileRequest request = new CreateCaseFileRequest(
                CASE_NUMBER,
                "Titre",
                "Desc",
                CaseStatus.OPEN,
                CasePriority.MEDIUM,
                Instant.now(),
                null,
                null
        );
        when(caseFileRepository.existsByCaseNumber(CASE_NUMBER)).thenReturn(true);

        assertThatThrownBy(() -> caseFileService.createCaseFile(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("caseNumber");

        verify(caseFileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Récupération d'un dossier par ID avec succès")
    void shouldGetCaseFileByIdSuccessfully() {
        CaseFile cf = mockCaseFile(false);
        CaseFileResponse expected = mockResponse(cf);
        when(caseFileRepository.findById(CASE_ID)).thenReturn(Optional.of(cf));
        when(caseFileMapper.toResponse(cf)).thenReturn(expected);

        CaseFileResponse actual = caseFileService.getCaseFileById(CASE_ID);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'un dossier supprimé → exception")
    void shouldThrowExceptionWhenCaseFileDeleted() {
        CaseFile cf = mockCaseFile(true);
        when(caseFileRepository.findById(CASE_ID)).thenReturn(Optional.of(cf));

        assertThatThrownBy(() -> caseFileService.getCaseFileById(CASE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération par numéro avec succès")
    void shouldGetCaseFileByNumberSuccessfully() {
        CaseFile cf = mockCaseFile(false);
        CaseFileResponse expected = mockResponse(cf);
        when(caseFileRepository.findByCaseNumber(CASE_NUMBER)).thenReturn(Optional.of(cf));
        when(caseFileMapper.toResponse(cf)).thenReturn(expected);

        CaseFileResponse actual = caseFileService.getCaseFileByNumber(CASE_NUMBER);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération de tous les dossiers actifs")
    void shouldGetAllCaseFiles() {
        CaseFile cf1 = mockCaseFile(false);
        CaseFile cf2 = mockCaseFile(false);
        cf2.setId(2L);
        cf2.setCaseNumber("PCMS_CASE_002");
        List<CaseFile> list = List.of(cf1, cf2);

        when(caseFileRepository.findByDeletedFalse()).thenReturn(list);
        when(caseFileMapper.toResponse(any(CaseFile.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<CaseFileResponse> responses = caseFileService.getAllCaseFiles();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CaseFileResponse::getCaseNumber)
                .containsExactlyInAnyOrder(CASE_NUMBER, "PCMS_CASE_002");
    }

    @Test
    @DisplayName("Mise à jour complète (PUT) avec succès")
    void shouldUpdateCaseFileSuccessfully() {
        // Arrange
        CaseFile existing = mockCaseFile(false);
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

        when(caseFileRepository.findById(CASE_ID)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            CaseFile c = inv.getArgument(1);
            c.setTitle("Nouveau titre");
            c.setDescription("Nouvelle description");
            c.setStatus(CaseStatus.IN_PROGRESS);
            c.setPriority(CasePriority.HIGH);
            c.setClosedAt(now);
            c.setIncidentDate(today);
            c.setLocation("Paris");
            return null;
        }).when(caseFileMapper).updateEntity(request, existing);

        when(caseFileRepository.save(existing)).thenReturn(existing);
        when(caseFileMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        CaseFileResponse actual = caseFileService.updateCaseFile(CASE_ID, request);

        // Assert
        assertThat(actual.getTitle()).isEqualTo("Nouveau titre");
        assertThat(actual.getDescription()).isEqualTo("Nouvelle description");
        assertThat(actual.getStatus()).isEqualTo(CaseStatus.IN_PROGRESS);
        assertThat(actual.getPriority()).isEqualTo(CasePriority.HIGH);
        assertThat(actual.getClosedAt()).isEqualTo(now);
        assertThat(actual.getIncidentDate()).isEqualTo(today);
        assertThat(actual.getLocation()).isEqualTo("Paris");
        verify(caseFileRepository).save(existing);
    }

    @Test
    @DisplayName("Mise à jour partielle (PATCH) avec succès")
    void shouldPatchCaseFileSuccessfully() {
        // Arrange
        CaseFile existing = mockCaseFile(false);
        PatchCaseFileRequest request = new PatchCaseFileRequest(
                "Titre modifié",
                null,
                null,
                null,
                null,
                null,
                null
        ); // seul le titre change

        when(caseFileRepository.findById(CASE_ID)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            CaseFile c = inv.getArgument(1);
            c.setTitle("Titre modifié");
            return null;
        }).when(caseFileMapper).patchEntity(request, existing);

        when(caseFileRepository.save(existing)).thenReturn(existing);
        when(caseFileMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        CaseFileResponse actual = caseFileService.patchCaseFile(CASE_ID, request);

        // Assert
        assertThat(actual.getTitle()).isEqualTo("Titre modifié");
        assertThat(actual.getDescription()).isEqualTo("Description de l'enquête"); // inchangé
        assertThat(actual.getStatus()).isEqualTo(CaseStatus.OPEN); // inchangé
        verify(caseFileRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression logique avec succès")
    void shouldDeleteCaseFileSuccessfully() {
        CaseFile existing = mockCaseFile(false);
        when(caseFileRepository.findById(CASE_ID)).thenReturn(Optional.of(existing));

        caseFileService.deleteCaseFile(CASE_ID);

        assertThat(existing.getDeleted()).isTrue();
        verify(caseFileRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression d'un dossier déjà supprimé ne fait rien")
    void shouldDoNothingWhenDeletingAlreadyDeleted() {
        CaseFile existing = mockCaseFile(true);
        when(caseFileRepository.findById(CASE_ID)).thenReturn(Optional.of(existing));

        caseFileService.deleteCaseFile(CASE_ID);

        verify(caseFileRepository, never()).save(existing);
    }
}