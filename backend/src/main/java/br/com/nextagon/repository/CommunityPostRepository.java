
package br.com.nextagon.repository;

import br.com.nextagon.model.CommunityFeed;
import br.com.nextagon.model.CommunityPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityPostRepository
        extends JpaRepository<CommunityPost, String> {

    Page<CommunityPost> findByFeedOrderByCreatedAtDesc(
            CommunityFeed feed,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CommunityPost p WHERE p.id = :id")
    Optional<CommunityPost> findByIdForUpdate(
            @Param("id") String id
    );
}
