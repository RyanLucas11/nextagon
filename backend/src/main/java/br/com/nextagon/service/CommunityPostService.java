
package br.com.nextagon.service;

import br.com.nextagon.dto.request.CommunityPostRequestDto;
import br.com.nextagon.dto.response.CommunityPostResponseDto;
import br.com.nextagon.model.CommunityFeed;
import br.com.nextagon.model.CommunityPost;
import br.com.nextagon.model.CommunityPostType;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.CommunityPostRepository;
import br.com.nextagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommunityPostResponseDto> list(
            String feedValue,
            String currentUserId
    ) {
        return list(
                feedValue,
                currentUserId,
                0,
                DEFAULT_PAGE_SIZE
        );
    }

    @Transactional(readOnly = true)
    public List<CommunityPostResponseDto> list(
            String feedValue,
            String currentUserId,
            int page,
            int size
    ) {
        CommunityFeed feed = parseFeed(feedValue);

        findActiveUser(currentUserId);

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A página não pode ser negativa"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tamanho da página deve estar entre 1 e 50"
            );
        }

        PageRequest pageable = PageRequest.of(page, size);

        return postRepository
                .findByFeedOrderByCreatedAtDesc(feed, pageable)
                .getContent()
                .stream()
                .map(post -> new CommunityPostResponseDto(
                        post,
                        currentUserId
                ))
                .toList();
    }

    @Transactional
    public CommunityPostResponseDto create(
            CommunityPostRequestDto request,
            String currentUserId
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dados da publicação são obrigatórios"
            );
        }

        CommunityFeed feed = parseFeed(request.getFeed());
        CommunityPostType type = parseType(request.getType());

        validateContent(request, feed, type);

        User author = findActiveUser(currentUserId);

        CommunityPost post = new CommunityPost();

        post.setFeed(feed);
        post.setType(type);

        String title = clean(request.getTitle());
        post.setTitle(title != null ? title : "");

        post.setContent(request.getText().trim());
        post.setCategory(clean(request.getCategory()));
        post.setLocation(clean(request.getLocation()));
        post.setEmploymentType(clean(request.getEmploymentType()));
        post.setAuthor(author);

        CommunityPost savedPost = postRepository.save(post);

        return new CommunityPostResponseDto(
                savedPost,
                currentUserId
        );
    }

    @Transactional
    public CommunityPostResponseDto toggleLike(
            String postId,
            String currentUserId
    ) {
        if (postId == null || postId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID da publicação é obrigatório"
            );
        }

        findActiveUser(currentUserId);

        CommunityPost post = postRepository
                .findByIdForUpdate(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Publicação não encontrada"
                ));

        if (!post.getLikedBy().remove(currentUserId)) {
            post.getLikedBy().add(currentUserId);
        }

        CommunityPost savedPost = postRepository.save(post);

        return new CommunityPostResponseDto(
                savedPost,
                currentUserId
        );
    }

    private User findActiveUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário não encontrado"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Conta inativa"
            );
        }

        return user;
    }

    private CommunityFeed parseFeed(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Feed é obrigatório"
            );
        }

        try {
            return CommunityFeed.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Feed inválido. Use network ou training"
            );
        }
    }

    private CommunityPostType parseType(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de publicação é obrigatório"
            );
        }

        try {
            return CommunityPostType.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de publicação inválido"
            );
        }
    }

    private void validateContent(
            CommunityPostRequestDto request,
            CommunityFeed feed,
            CommunityPostType type
    ) {
        require(
                request.getText(),
                "O texto da publicação é obrigatório"
        );

        if (request.getText().length() > 1200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A publicação deve ter no máximo 1200 caracteres"
            );
        }

        if (feed == CommunityFeed.TRAINING
                && type != CommunityPostType.TRAINING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O feed Treinos aceita apenas publicações de treino"
            );
        }

        if (feed == CommunityFeed.NETWORK
                && type == CommunityPostType.TRAINING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Publicações de treino devem ser feitas no feed Treinos"
            );
        }

        if (type == CommunityPostType.TRAINING) {
            require(
                    request.getTitle(),
                    "Informe um título para o treino"
            );

            require(
                    request.getCategory(),
                    "Selecione a modalidade do treino"
            );
        }

        if (type == CommunityPostType.OPPORTUNITY) {
            require(
                    request.getTitle(),
                    "Informe o cargo ou serviço da oportunidade"
            );

            require(
                    request.getLocation(),
                    "Informe a localização da oportunidade"
            );

            require(
                    request.getEmploymentType(),
                    "Informe o formato da oportunidade"
            );
        }
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
