package br.com.nextagon.repository;

import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    // Query dedicada para marketplace — não carrega todos os usuários na memória
    @Query("SELECT u FROM User u JOIN FETCH u.professionalProfile p " +
            "WHERE u.role = :role AND u.active = true AND p.available = true " +
            "AND p.averageRating >= :minRating " +
            "ORDER BY p.averageRating DESC")
    Page<User> findProfessionalsForMarketplace(
            @Param("role") Role role,
            @Param("minRating") double minRating,
            Pageable pageable);
}