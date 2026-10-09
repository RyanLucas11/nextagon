package br.com.nextagon.repository;

import br.com.nextagon.model.CommunityFeed;
import br.com.nextagon.model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, String> {
    List<CommunityPost> findByFeedOrderByCreatedAtDesc(CommunityFeed feed);
}
