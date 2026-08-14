package lu.police.pcms.casecomment.service;

import lu.police.pcms.casecomment.dto.CaseCommentResponse;
import lu.police.pcms.casecomment.dto.CreateCaseCommentRequest;
import lu.police.pcms.casecomment.dto.PatchCaseCommentRequest;
import lu.police.pcms.casecomment.dto.UpdateCaseCommentRequest;
import lu.police.pcms.casecomment.entity.CaseComment;
import lu.police.pcms.casecomment.mapper.CaseCommentMapper;
import lu.police.pcms.casecomment.repository.CaseCommentRepository;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.user.entity.User;
import lu.police.pcms.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service CaseCommentService")
class CaseCommentServiceTest {

    @Mock
    private CaseCommentRepository commentRepository;

    @Mock
    private CaseFileRepository caseFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CaseCommentMapper commentMapper;

    @InjectMocks
    private CaseCommentService commentService;

    // ========== DONNÉES DE TEST ==========

    private static final Long COMMENT_ID = 1L;
    private static final Long CASE_FILE_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final String CONTENT = "Ceci est un commentaire";

    private CaseFile mockCaseFile() {
        CaseFile cf = new CaseFile();
        cf.setId(CASE_FILE_ID);
        cf.setCaseNumber("PCMS_CASE_001");
        return cf;
    }

    private User mockUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("alice@pcms.lu");
        return user;
    }

    private CaseComment mockComment(boolean deleted) {
        CaseComment comment = new CaseComment();
        comment.setId(COMMENT_ID);
        comment.setCaseFile(mockCaseFile());
        comment.setUser(mockUser());
        comment.setContent(CONTENT);
        comment.setDeleted(deleted);
        return comment;
    }

    private CaseCommentResponse mockResponse(CaseComment comment) {
        CaseCommentResponse response = new CaseCommentResponse();
        response.setId(comment.getId());
        response.setCaseFileId(comment.getCaseFile().getId());
        response.setUserId(comment.getUser().getId());
        response.setContent(comment.getContent());
        response.setDeleted(comment.getDeleted());
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'un commentaire avec succès")
    void shouldCreateCommentSuccessfully() {
        // Arrange
        CreateCaseCommentRequest request = new CreateCaseCommentRequest(CASE_FILE_ID, USER_ID, CONTENT);
        CaseFile caseFile = mockCaseFile();
        User user = mockUser();
        CaseComment entity = new CaseComment();
        CaseComment saved = mockComment(false);
        CaseCommentResponse expected = mockResponse(saved);

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(commentMapper.toEntity(request)).thenReturn(entity);
        when(commentRepository.save(entity)).thenReturn(saved);
        when(commentMapper.toResponse(saved)).thenReturn(expected);

        // Act
        CaseCommentResponse actual = commentService.createComment(request);

        // Assert
        assertThat(actual).isEqualTo(expected);
        assertThat(entity.getCaseFile()).isEqualTo(caseFile);
        assertThat(entity.getUser()).isEqualTo(user);
        verify(commentRepository).save(entity);
    }

    @Test
    @DisplayName("Création avec dossier inexistant → exception")
    void shouldThrowExceptionWhenCaseFileNotFound() {
        CreateCaseCommentRequest request = new CreateCaseCommentRequest(CASE_FILE_ID, USER_ID, CONTENT);
        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dossier");
    }

    @Test
    @DisplayName("Création avec utilisateur inexistant → exception")
    void shouldThrowExceptionWhenUserNotFound() {
        CreateCaseCommentRequest request = new CreateCaseCommentRequest(CASE_FILE_ID, USER_ID, CONTENT);
        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(mockCaseFile()));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Utilisateur");
    }

    @Test
    @DisplayName("Récupération d'un commentaire par ID avec succès")
    void shouldGetCommentByIdSuccessfully() {
        CaseComment comment = mockComment(false);
        CaseCommentResponse expected = mockResponse(comment);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
        when(commentMapper.toResponse(comment)).thenReturn(expected);

        CaseCommentResponse actual = commentService.getCommentById(COMMENT_ID);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'un commentaire supprimé → exception")
    void shouldThrowExceptionWhenCommentDeleted() {
        CaseComment comment = mockComment(true);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.getCommentById(COMMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération de tous les commentaires actifs")
    void shouldGetAllComments() {
        CaseComment c1 = mockComment(false);
        CaseComment c2 = mockComment(false);
        c2.setId(2L);
        c2.setContent("Autre commentaire");
        List<CaseComment> list = List.of(c1, c2);

        when(commentRepository.findByDeletedFalse()).thenReturn(list);
        when(commentMapper.toResponse(any(CaseComment.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<CaseCommentResponse> responses = commentService.getAllComments();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CaseCommentResponse::getContent)
                .containsExactlyInAnyOrder(CONTENT, "Autre commentaire");
    }

    @Test
    @DisplayName("Récupération des commentaires d'un dossier")
    void shouldGetCommentsByCaseFile() {
        CaseFile caseFile = mockCaseFile();
        CaseComment c1 = mockComment(false);
        CaseComment c2 = mockComment(false);
        c2.setId(2L);
        List<CaseComment> list = List.of(c1, c2);

        when(caseFileRepository.findById(CASE_FILE_ID)).thenReturn(Optional.of(caseFile));
        when(commentRepository.findByCaseFile(caseFile)).thenReturn(list);
        when(commentMapper.toResponse(any(CaseComment.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<CaseCommentResponse> responses = commentService.getCommentsByCaseFile(CASE_FILE_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CaseCommentResponse::getCaseFileId)
                .containsOnly(CASE_FILE_ID);
    }

    @Test
    @DisplayName("Récupération des commentaires d'un utilisateur")
    void shouldGetCommentsByUser() {
        User user = mockUser();
        CaseComment c1 = mockComment(false);
        CaseComment c2 = mockComment(false);
        c2.setId(2L);
        List<CaseComment> list = List.of(c1, c2);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(commentRepository.findByUser(user)).thenReturn(list);
        when(commentMapper.toResponse(any(CaseComment.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<CaseCommentResponse> responses = commentService.getCommentsByUser(USER_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CaseCommentResponse::getUserId)
                .containsOnly(USER_ID);
    }

    @Test
    @DisplayName("Mise à jour complète (PUT) avec succès")
    void shouldUpdateCommentSuccessfully() {
        // Arrange
        CaseComment existing = mockComment(false);
        UpdateCaseCommentRequest request = new UpdateCaseCommentRequest("Nouveau contenu");

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            CaseComment c = inv.getArgument(1);
            c.setContent("Nouveau contenu");
            return null;
        }).when(commentMapper).updateEntity(request, existing);

        when(commentRepository.save(existing)).thenReturn(existing);
        when(commentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        CaseCommentResponse actual = commentService.updateComment(COMMENT_ID, request);

        // Assert
        assertThat(actual.getContent()).isEqualTo("Nouveau contenu");
        verify(commentRepository).save(existing);
    }

    @Test
    @DisplayName("Mise à jour partielle (PATCH) avec succès")
    void shouldPatchCommentSuccessfully() {
        // Arrange
        CaseComment existing = mockComment(false);
        PatchCaseCommentRequest request = new PatchCaseCommentRequest("Contenu modifié");

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            CaseComment c = inv.getArgument(1);
            c.setContent("Contenu modifié");
            return null;
        }).when(commentMapper).patchEntity(request, existing);

        when(commentRepository.save(existing)).thenReturn(existing);
        when(commentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        CaseCommentResponse actual = commentService.patchComment(COMMENT_ID, request);

        // Assert
        assertThat(actual.getContent()).isEqualTo("Contenu modifié");
        verify(commentRepository).save(existing);
    }

    @Test
    @DisplayName("Patch avec content = null ne modifie rien")
    void shouldPatchWithNullContentDoNothing() {
        // Arrange
        CaseComment existing = mockComment(false);
        PatchCaseCommentRequest request = new PatchCaseCommentRequest(null);

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            // Ne rien modifier
            return null;
        }).when(commentMapper).patchEntity(request, existing);

        when(commentRepository.save(existing)).thenReturn(existing);
        when(commentMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        CaseCommentResponse actual = commentService.patchComment(COMMENT_ID, request);

        // Assert
        assertThat(actual.getContent()).isEqualTo(CONTENT); // inchangé
        verify(commentRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression logique avec succès")
    void shouldDeleteCommentSuccessfully() {
        CaseComment existing = mockComment(false);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        commentService.deleteComment(COMMENT_ID);

        assertThat(existing.getDeleted()).isTrue();
        verify(commentRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression d'un commentaire déjà supprimé ne fait rien")
    void shouldDoNothingWhenDeletingAlreadyDeleted() {
        CaseComment existing = mockComment(true);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        commentService.deleteComment(COMMENT_ID);

        verify(commentRepository, never()).save(existing);
    }
}