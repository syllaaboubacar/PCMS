package lu.police.pcms.suspect.service;

import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.suspect.dto.CreateSuspectRequest;
import lu.police.pcms.suspect.dto.PatchSuspectRequest;
import lu.police.pcms.suspect.dto.SuspectResponse;
import lu.police.pcms.suspect.dto.UpdateSuspectRequest;
import lu.police.pcms.suspect.entity.Suspect;
import lu.police.pcms.suspect.mapper.SuspectMapper;
import lu.police.pcms.suspect.repository.SuspectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service SuspectService")
class SuspectServiceTest {

    @Mock
    private SuspectRepository suspectRepository;

    @Mock
    private CaseFileRepository caseFileRepository;

    @Mock
    private SuspectMapper suspectMapper;

    @InjectMocks
    private SuspectService suspectService;

    // ========== DONNÉES DE TEST ==========

    private static final Long SUSPECT_ID = 1L;
    private static final Long CASE_FILE_ID = 10L;
    private static final String FIRST_NAME = "Jean";
    private static final String LAST_NAME = "Dupont";

    private CaseFile mockCaseFile() {
        CaseFile cf = new CaseFile();
        cf.setId(CASE_FILE_ID);
        cf.setCaseNumber("PCMS_CASE_001");
        return cf;
    }

    private Suspect mockSuspect(boolean deleted) {
        Suspect suspect = new Suspect();
        suspect.setId(SUSPECT_ID);
        suspect.setCaseFile(mockCaseFile());
        suspect.setFirstName(FIRST_NAME);
        suspect.setLastName(LAST_NAME);
        suspect.setBirthDate(LocalDate.of(1980, 5, 15));
        suspect.setNationality("Française");
        suspect.setNotes("Suspect principal");
        suspect.setDeleted(deleted);
        return suspect;
    }

    private SuspectResponse mockResponse(Suspect suspect) {
        SuspectResponse response = new SuspectResponse();
        response.setId(suspect.getId());
        response.setCaseFileId(suspect.getCaseFile().getId());
        response.setFirstName(suspect.getFirstName());
        response.setLastName(suspect.getLastName());
        response.setBirthDate(suspect.getBirthDate());
        response.setNationality(suspect.getNationality());
        response.setNotes(suspect.getNotes());
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'un suspect avec succès")
    void shouldCreateSuspectSuccessfully() {
        // Arrange
        CreateSuspectRequest request = new CreateSuspectRequest(
                CASE_FILE_ID, FIRST_NAME, LAST_NAME,
                LocalDate.of(1980, 5, 15), "Française", "Suspect principal"
        );
        CaseFile caseFile = mockCaseFile();
        Suspect entity = new Suspect();
        Suspect saved = mockSuspect(false);
        SuspectResponse expected = mockResponse(saved);

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                CASE_FILE_ID, LAST_NAME, FIRST_NAME)).thenReturn(false);
        when(suspectMapper.toEntity(request)).thenReturn(entity);
        when(suspectRepository.save(entity)).thenReturn(saved);
        when(suspectMapper.toResponse(saved)).thenReturn(expected);

        // Act
        SuspectResponse actual = suspectService.createSuspect(request);

        // Assert
        assertThat(actual).isEqualTo(expected);
        assertThat(entity.getCaseFile()).isEqualTo(caseFile);
        verify(suspectRepository).save(entity);
    }

    @Test
    @DisplayName("Création avec doublon (nom/prénom dans le même dossier) → exception")
    void shouldThrowExceptionWhenDuplicateInSameCaseFile() {
        CreateSuspectRequest request = new CreateSuspectRequest(
                CASE_FILE_ID, FIRST_NAME, LAST_NAME,
                null, null, null
        );
        CaseFile caseFile = mockCaseFile();

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                CASE_FILE_ID, LAST_NAME, FIRST_NAME)).thenReturn(true);

        assertThatThrownBy(() -> suspectService.createSuspect(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Suspect");

        verify(suspectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Création avec dossier inexistant → exception")
    void shouldThrowExceptionWhenCaseFileNotFound() {
        CreateSuspectRequest request = new CreateSuspectRequest(
                CASE_FILE_ID, FIRST_NAME, LAST_NAME,
                null, null, null
        );
        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> suspectService.createSuspect(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dossier");
    }

    @Test
    @DisplayName("Récupération d'un suspect par ID avec succès")
    void shouldGetSuspectByIdSuccessfully() {
        Suspect suspect = mockSuspect(false);
        SuspectResponse expected = mockResponse(suspect);
        when(suspectRepository.findById(SUSPECT_ID)).thenReturn(Optional.of(suspect));
        when(suspectMapper.toResponse(suspect)).thenReturn(expected);

        SuspectResponse actual = suspectService.getSuspectById(SUSPECT_ID);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'un suspect supprimé → exception")
    void shouldThrowExceptionWhenSuspectDeleted() {
        Suspect suspect = mockSuspect(true);
        when(suspectRepository.findById(SUSPECT_ID)).thenReturn(Optional.of(suspect));

        assertThatThrownBy(() -> suspectService.getSuspectById(SUSPECT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération de tous les suspects actifs")
    void shouldGetAllSuspects() {
        Suspect s1 = mockSuspect(false);
        Suspect s2 = mockSuspect(false);
        s2.setId(2L);
        s2.setFirstName("Marie");
        List<Suspect> list = List.of(s1, s2);

        when(suspectRepository.findByDeletedFalse()).thenReturn(list);
        when(suspectMapper.toResponse(any(Suspect.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<SuspectResponse> responses = suspectService.getAllSuspects();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(SuspectResponse::getFirstName)
                .containsExactlyInAnyOrder("Jean", "Marie");
    }

    @Test
    @DisplayName("Récupération des suspects d'un dossier")
    void shouldGetSuspectsByCaseFile() {
        CaseFile caseFile = mockCaseFile();
        Suspect s1 = mockSuspect(false);
        Suspect s2 = mockSuspect(false);
        s2.setId(2L);
        List<Suspect> list = List.of(s1, s2);

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(suspectRepository.findByCaseFile(caseFile)).thenReturn(list);
        when(suspectMapper.toResponse(any(Suspect.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<SuspectResponse> responses = suspectService.getSuspectsByCaseFile(CASE_FILE_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(SuspectResponse::getCaseFileId)
                .containsOnly(CASE_FILE_ID);
    }

    @Test
    @DisplayName("Mise à jour complète (PUT) avec succès")
    void shouldUpdateSuspectSuccessfully() {
        // Arrange
        Suspect existing = mockSuspect(false);
        UpdateSuspectRequest request = new UpdateSuspectRequest(
                "Marie", "Dupont", LocalDate.of(1990, 1, 1),
                "Belge", "Nouveau suspect"
        );

        when(suspectRepository.findById(SUSPECT_ID)).thenReturn(Optional.of(existing));
        when(suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                CASE_FILE_ID, "Dupont", "Marie")).thenReturn(false);

        doAnswer(inv -> {
            Suspect s = inv.getArgument(1);
            s.setFirstName("Marie");
            s.setLastName("Dupont");
            s.setBirthDate(LocalDate.of(1990, 1, 1));
            s.setNationality("Belge");
            s.setNotes("Nouveau suspect");
            return null;
        }).when(suspectMapper).updateEntity(request, existing);

        when(suspectRepository.save(existing)).thenReturn(existing);
        when(suspectMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        SuspectResponse actual = suspectService.updateSuspect(SUSPECT_ID, request);

        // Assert
        assertThat(actual.getFirstName()).isEqualTo("Marie");
        assertThat(actual.getLastName()).isEqualTo("Dupont");
        assertThat(actual.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(actual.getNationality()).isEqualTo("Belge");
        assertThat(actual.getNotes()).isEqualTo("Nouveau suspect");
        verify(suspectRepository).save(existing);
    }

    @Test
    @DisplayName("PUT avec doublon (nom/prénom) → exception")
    void shouldThrowExceptionWhenUpdatingToDuplicate() {
        Suspect existing = mockSuspect(false);
        UpdateSuspectRequest request = new UpdateSuspectRequest(
                "Marie", "Dupont", null, null, null
        );

        when(suspectRepository.findById(SUSPECT_ID)).thenReturn(Optional.of(existing));
        when(suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                CASE_FILE_ID, "Dupont", "Marie")).thenReturn(true);

        assertThatThrownBy(() -> suspectService.updateSuspect(SUSPECT_ID, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Suspect");
    }

    @Test
    @DisplayName("Mise à jour partielle (PATCH) avec succès")
    void shouldPatchSuspectSuccessfully() {
        // Arrange
        Suspect existing = mockSuspect(false);
        PatchSuspectRequest request = new PatchSuspectRequest(
                "Marie", null, null, null, null
        ); // seul le prénom change

        when(suspectRepository.findById(SUSPECT_ID)).thenReturn(Optional.of(existing));
        when(suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                CASE_FILE_ID, LAST_NAME, "Marie")).thenReturn(false);

        doAnswer(inv -> {
            Suspect s = inv.getArgument(1);
            s.setFirstName("Marie");
            return null;
        }).when(suspectMapper).patchEntity(request, existing);

        when(suspectRepository.save(existing)).thenReturn(existing);
        when(suspectMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        SuspectResponse actual = suspectService.patchSuspect(SUSPECT_ID, request);

        // Assert
        assertThat(actual.getFirstName()).isEqualTo("Marie");
        assertThat(actual.getLastName()).isEqualTo(LAST_NAME); // inchangé
        verify(suspectRepository).save(existing);
    }

    @Test
    @DisplayName("Patch avec doublon → exception")
    void shouldThrowExceptionWhenPatchingToDuplicate() {
        Suspect existing = mockSuspect(false);
        PatchSuspectRequest request = new PatchSuspectRequest(
                "Marie", null, null, null, null
        );

        when(suspectRepository.findById(SUSPECT_ID)).thenReturn(Optional.of(existing));
        when(suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                CASE_FILE_ID, LAST_NAME, "Marie")).thenReturn(true);

        assertThatThrownBy(() -> suspectService.patchSuspect(SUSPECT_ID, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Suspect");
    }

    @Test
    @DisplayName("Suppression logique avec succès")
    void shouldDeleteSuspectSuccessfully() {
        Suspect existing = mockSuspect(false);
        when(suspectRepository.findById(SUSPECT_ID)).thenReturn(Optional.of(existing));

        suspectService.deleteSuspect(SUSPECT_ID);

        assertThat(existing.getDeleted()).isTrue();
        verify(suspectRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression d'un suspect déjà supprimé ne fait rien")
    void shouldDoNothingWhenDeletingAlreadyDeleted() {
        Suspect existing = mockSuspect(true);
        when(suspectRepository.findById(SUSPECT_ID)).thenReturn(Optional.of(existing));

        suspectService.deleteSuspect(SUSPECT_ID);

        verify(suspectRepository, never()).save(existing);
    }
}