
package br.com.nextagon.service;

import br.com.nextagon.dto.request.CommunityPostRequestDto;
import br.com.nextagon.dto.response.CommunityPostResponseDto;
import br.com.nextagon.model.CommunityFeed;
import br.com.nextagon.model.CommunityPost;
import br.com.nextagon.model.CommunityPostType;
import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.CommunityPostRepository;
import br.com.nextagon.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityPostServiceTest {

    private static final String USER_ID = "user-1";
    private static final String POST_ID = "post-1";

    @Mock
    private CommunityPostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommunityPostService communityPostService;

    private User buildUser(boolean active) {
        User user = new User();

        user.setId(USER_ID);
        user.setName("Usuário Teste");
        user.setRole(Role.ATHLETE);
        user.setActive(active);

        return user;
    }

    private CommunityPost buildPost() {
        CommunityPost post = new CommunityPost();

        post.setId(POST_ID);
        post.setFeed(CommunityFeed.NETWORK);
        post.setType(CommunityPostType.OPPORTUNITY);
        post.setTitle("Oportunidade");
        post.setContent("Publicação de teste");
        post.setAuthor(buildUser(true));
        post.setCreatedAt(LocalDateTime.now());

        return post;
    }

    private CommunityPostRequestDto buildRequest() {
        CommunityPostRequestDto dto =
                new CommunityPostRequestDto();

        dto.setFeed("network");
        dto.setType("opportunity");
        dto.setTitle("Oportunidade");
        dto.setText("Estamos procurando profissionais");
        dto.setLocation("Brasília");
        dto.setEmploymentType("Presencial");

        return dto;
    }

    @Test
    @DisplayName("Usuário ativo pode criar publicação")
    void deveCriarPublicacao() {

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(buildUser(true)));

        when(postRepository.save(any(CommunityPost.class)))
                .thenAnswer(invocation -> {
                    CommunityPost post = invocation.getArgument(0);
                    post.setId(POST_ID);
                    post.setCreatedAt(LocalDateTime.now());
                    return post;
                });

        CommunityPostResponseDto response =
                communityPostService.create(
                        buildRequest(),
                        USER_ID
                );

        assertNotNull(response);
        assertEquals(POST_ID, response.id());
        assertEquals("network", response.feed());
        assertEquals("opportunity", response.type());
        assertEquals(0, response.likeCount());
        assertFalse(response.likedByMe());

        verify(postRepository).save(any(CommunityPost.class));
    }

    @Test
    @DisplayName("Usuário inativo não pode publicar")
    void naoDevePermitirUsuarioInativoPublicar() {

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(buildUser(false)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> communityPostService.create(
                        buildRequest(),
                        USER_ID
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Usuário pode curtir publicação")
    void deveCurtirPublicacao() {

        CommunityPost post = buildPost();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(buildUser(true)));

        when(postRepository.findByIdForUpdate(POST_ID))
                .thenReturn(Optional.of(post));

        when(postRepository.save(any(CommunityPost.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommunityPostResponseDto response =
                communityPostService.toggleLike(
                        POST_ID,
                        USER_ID
                );

        assertEquals(1, response.likeCount());
        assertTrue(response.likedByMe());
        assertTrue(post.getLikedBy().contains(USER_ID));
    }

    @Test
    @DisplayName("Curtir novamente remove a curtida")
    void deveRemoverCurtidaExistente() {

        CommunityPost post = buildPost();
        post.getLikedBy().add(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(buildUser(true)));

        when(postRepository.findByIdForUpdate(POST_ID))
                .thenReturn(Optional.of(post));

        when(postRepository.save(any(CommunityPost.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommunityPostResponseDto response =
                communityPostService.toggleLike(
                        POST_ID,
                        USER_ID
                );

        assertEquals(0, response.likeCount());
        assertFalse(response.likedByMe());
        assertFalse(post.getLikedBy().contains(USER_ID));
    }

    @Test
    @DisplayName("Usuário inativo não pode curtir")
    void naoDevePermitirUsuarioInativoCurtir() {

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(buildUser(false)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> communityPostService.toggleLike(
                        POST_ID,
                        USER_ID
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("Publicação inexistente retorna 404")
    void deveRejeitarPublicacaoInexistente() {

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(buildUser(true)));

        when(postRepository.findByIdForUpdate(POST_ID))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> communityPostService.toggleLike(
                        POST_ID,
                        USER_ID
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    @DisplayName("Deve listar publicações com paginação")
    void deveListarPublicacoesPaginadas() {

        CommunityPost post = buildPost();

        Page<CommunityPost> page =
                new PageImpl<>(List.of(post));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(buildUser(true)));

        when(postRepository.findByFeedOrderByCreatedAtDesc(
                eq(CommunityFeed.NETWORK),
                any(Pageable.class)
        )).thenReturn(page);

        List<CommunityPostResponseDto> result =
                communityPostService.list(
                        "network",
                        USER_ID,
                        0,
                        20
                );

        assertEquals(1, result.size());
        assertEquals(POST_ID, result.get(0).id());

        verify(postRepository)
                .findByFeedOrderByCreatedAtDesc(
                        eq(CommunityFeed.NETWORK),
                        argThat(pageable ->
                                pageable.getPageNumber() == 0
                                        && pageable.getPageSize() == 20
                        )
                );
    }

    @Test
    @DisplayName("Não deve aceitar paginação inválida")
    void naoDevePermitirPaginacaoInvalida() {

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(buildUser(true)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> communityPostService.list(
                        "network",
                        USER_ID,
                        -1,
                        20
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("Não deve aceitar feed inválido")
    void naoDevePermitirFeedInvalido() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> communityPostService.list(
                        "feed-inexistente",
                        USER_ID,
                        0,
                        20
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("Não deve criar publicação sem texto")
    void naoDeveCriarPublicacaoSemTexto() {

        CommunityPostRequestDto dto = buildRequest();
        dto.setText(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> communityPostService.create(
                        dto,
                        USER_ID
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(postRepository);
    }
}
