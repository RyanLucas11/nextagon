package br.com.nextagon.service;

import br.com.nextagon.dto.request.CommunityPostRequestDto;
import br.com.nextagon.dto.response.CommunityPostResponseDto;
import br.com.nextagon.model.CommunityFeed;
import br.com.nextagon.model.CommunityPost;
import br.com.nextagon.model.CommunityPostType;
import br.com.nextagon.repository.CommunityPostRepository;
import br.com.nextagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommunityPostResponseDto> list(String feedValue, String currentUserId) {
        CommunityFeed feed = parseFeed(feedValue);
        return postRepository.findByFeedOrderByCreatedAtDesc(feed).stream()
                .map(post -> new CommunityPostResponseDto(post, currentUserId))
                .toList();
    }

    @Transactional
    public CommunityPostResponseDto create(CommunityPostRequestDto request, String currentUserId) {
        CommunityFeed feed = parseFeed(request.getFeed());
        CommunityPostType type = parseType(request.getType());
        validateContent(request, feed, type);

        var author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));

        CommunityPost post = new CommunityPost();
        post.setFeed(feed);
        post.setType(type);
        post.setTitle(clean(request.getTitle()) == null ? "" : request.getTitle().trim());
        post.setContent(request.getText().trim());
        post.setCategory(clean(request.getCategory()));
        post.setLocation(clean(request.getLocation()));
        post.setEmploymentType(clean(request.getEmploymentType()));
        post.setAuthor(author);

        return new CommunityPostResponseDto(postRepository.save(post), currentUserId);
    }

    @Transactional
    public CommunityPostResponseDto toggleLike(String postId, String currentUserId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publicação não encontrada."));
        if (!post.getLikedBy().remove(currentUserId)) {
            post.getLikedBy().add(currentUserId);
        }
        return new CommunityPostResponseDto(postRepository.save(post), currentUserId);
    }

    private CommunityFeed parseFeed(String value) {
        try {
            return CommunityFeed.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Feed inválido. Use network ou training.");
        }
    }

    private CommunityPostType parseType(String value) {
        try {
            return CommunityPostType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Tipo de publicação inválido.");
        }
    }

    private void validateContent(CommunityPostRequestDto request, CommunityFeed feed, CommunityPostType type) {
        if (feed == CommunityFeed.TRAINING && type != CommunityPostType.TRAINING) {
            throw new IllegalArgumentException("O feed Treinos aceita apenas publicações de treino.");
        }
        if (feed == CommunityFeed.NETWORK && type == CommunityPostType.TRAINING) {
            throw new IllegalArgumentException("Publicações de treino devem ser feitas no feed Treinos.");
        }
        if (type == CommunityPostType.TRAINING) {
            require(request.getTitle(), "Informe um título para o treino.");
            require(request.getCategory(), "Selecione a modalidade do treino.");
        }
        if (type == CommunityPostType.OPPORTUNITY) {
            require(request.getTitle(), "Informe o cargo ou serviço da oportunidade.");
            require(request.getLocation(), "Informe a localização da oportunidade.");
            require(request.getEmploymentType(), "Informe o formato da oportunidade.");
        }
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
