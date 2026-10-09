package br.com.nextagon.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "community_posts", indexes = {
        @Index(name = "idx_community_posts_feed_created", columnList = "feed, created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityFeed feed;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    private CommunityPostType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 60)
    private String category;

    @Column(length = 120)
    private String location;

    @Column(name = "employment_type", length = 40)
    private String employmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ElementCollection
    @CollectionTable(name = "community_post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
    @Column(name = "user_id", nullable = false, length = 36)
    private Set<String> likedBy = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (likedBy == null) likedBy = new LinkedHashSet<>();
    }
}
