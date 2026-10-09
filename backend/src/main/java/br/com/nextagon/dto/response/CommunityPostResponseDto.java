package br.com.nextagon.dto.response;

import br.com.nextagon.model.CommunityPost;

import java.time.LocalDateTime;

public record CommunityPostResponseDto(
        String id,
        String feed,
        String type,
        String title,
        String text,
        String category,
        String location,
        String employmentType,
        String author,
        String role,
        LocalDateTime createdAt,
        int likeCount,
        boolean likedByMe
) {
    public CommunityPostResponseDto(CommunityPost post, String currentUserId) {
        this(
                post.getId(),
                post.getFeed().name().toLowerCase(),
                post.getType().name().toLowerCase(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getLocation(),
                post.getEmploymentType(),
                post.getAuthor().getName(),
                post.getAuthor().getRole().name().toLowerCase(),
                post.getCreatedAt(),
                post.getLikedBy().size(),
                post.getLikedBy().contains(currentUserId)
        );
    }
}
