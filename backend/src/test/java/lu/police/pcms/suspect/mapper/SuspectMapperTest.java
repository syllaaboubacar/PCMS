package lu.police.pcms.suspect.mapper;

import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.suspect.dto.CreateSuspectRequest;
import lu.police.pcms.suspect.dto.PatchSuspectRequest;
import lu.police.pcms.suspect.dto.SuspectResponse;
import lu.police.pcms.suspect.dto.UpdateSuspectRequest;
import lu.police.pcms.suspect.entity.Suspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper {@link SuspectMapper}.
 *
 * <p>
 * Ces tests vérifient que la conversion entre les DTO et l'entité
 * fonctionne correctement, y compris pour les cas de mise à jour
 * partielle avec {@code null}.
 * </p>
 */
class SuspectMapperTest {

    private final SuspectMapper mapper = Mappers.getMapper(SuspectMapper.class);

    @Test
    @DisplayName("Conversion CreateSuspectRequest → Suspect")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        CreateSuspectRequest request = new CreateSuspectRequest(
                10L,
                "Jean",
                "Dupont",
                LocalDate.of(1980, 5, 15),
                "Française",
                "Suspect principal"
        );

        // Act
        Suspect entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getFirstName()).isEqualTo("Jean");
        assertThat(entity.getLastName()).isEqualTo("Dupont");
        assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1980, 5, 15));
        assertThat(entity.getNationality()).isEqualTo("Française");
        assertThat(entity.getNotes()).isEqualTo("Suspect principal");
        assertThat(entity.getId()).isNull();          // ignoré
        assertThat(entity.getCaseFile()).isNull();    // ignoré
        assertThat(entity.getCreatedAt()).isNull();   // ignoré
    }

    @Test
    @DisplayName("Conversion Suspect → SuspectResponse")
    void shouldMapEntityToResponse() {
        // Arrange
        CaseFile caseFile = new CaseFile();
        caseFile.setId(10L);

        Suspect entity = new Suspect();
        entity.setId(1L);
        entity.setCaseFile(caseFile);
        entity.setFirstName("Jean");
        entity.setLastName("Dupont");
        entity.setBirthDate(LocalDate.of(1980, 5, 15));
        entity.setNationality("Française");
        entity.setNotes("Suspect principal");

        // Act
        SuspectResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCaseFileId()).isEqualTo(10L);
        assertThat(response.getFirstName()).isEqualTo("Jean");
        assertThat(response.getLastName()).isEqualTo("Dupont");
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1980, 5, 15));
        assertThat(response.getNationality()).isEqualTo("Française");
        assertThat(response.getNotes()).isEqualTo("Suspect principal");
    }

    @Test
    @DisplayName("Mise à jour complète UpdateSuspectRequest → Suspect (PUT)")
    void shouldUpdateEntity() {
        // Arrange
        Suspect entity = new Suspect();
        entity.setId(1L);
        entity.setFirstName("Ancien");
        entity.setLastName("Nom");
        entity.setBirthDate(LocalDate.of(1990, 1, 1));
        entity.setNationality("Inconnue");

        UpdateSuspectRequest request = new UpdateSuspectRequest(
                "Jean",
                "Dupont",
                LocalDate.of(1980, 5, 15),
                "Française",
                "Suspect principal"
        );

        // Act
        mapper.updateEntity(request, entity);

        // Assert
        assertThat(entity.getFirstName()).isEqualTo("Jean");
        assertThat(entity.getLastName()).isEqualTo("Dupont");
        assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1980, 5, 15));
        assertThat(entity.getNationality()).isEqualTo("Française");
        assertThat(entity.getNotes()).isEqualTo("Suspect principal");
        assertThat(entity.getId()).isEqualTo(1L); // inchangé
    }

    @Test
    @DisplayName("Mise à jour partielle PatchSuspectRequest → Suspect (PATCH)")
    void shouldPatchEntity() {
        // Arrange
        Suspect entity = new Suspect();
        entity.setId(1L);
        entity.setFirstName("Jean");
        entity.setLastName("Dupont");
        entity.setNationality("Française");

        PatchSuspectRequest request = new PatchSuspectRequest(
                null,          // firstName inchangé
                "Durand",      // lastName modifié
                null,          // birthDate inchangé
                null,          // nationality inchangé
                "Nouvelle note" // notes ajoutées
        );

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getFirstName()).isEqualTo("Jean"); // inchangé
        assertThat(entity.getLastName()).isEqualTo("Durand");
        assertThat(entity.getNationality()).isEqualTo("Française"); // inchangé
        assertThat(entity.getNotes()).isEqualTo("Nouvelle note");
        assertThat(entity.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Patch avec tous les champs à null ne modifie rien")
    void shouldPatchWithNullsDoNothing() {
        // Arrange
        Suspect entity = new Suspect();
        entity.setId(1L);
        entity.setFirstName("Jean");
        entity.setLastName("Dupont");
        entity.setNotes("Note originale");

        PatchSuspectRequest request = new PatchSuspectRequest(null, null, null, null, null);

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getFirstName()).isEqualTo("Jean");
        assertThat(entity.getLastName()).isEqualTo("Dupont");
        assertThat(entity.getNotes()).isEqualTo("Note originale");
        assertThat(entity.getId()).isEqualTo(1L);
    }
}