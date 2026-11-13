
package br.com.nextagon.repository;

import br.com.nextagon.model.ProfessionalProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessionalProfileRepository
        extends JpaRepository<ProfessionalProfile, String> {

    Optional<ProfessionalProfile> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM ProfessionalProfile p
            WHERE p.user.id = :userId
            """)
    Optional<ProfessionalProfile> findByUserIdForUpdate(
            @Param("userId") String userId
    );
}
