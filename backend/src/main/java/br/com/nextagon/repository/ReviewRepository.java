package br.com.nextagon.repository;

import br.com.nextagon.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    boolean existsByContractId(String contractId);

    List<Review> findByProfessionalId(String professionalId);

    // Calcula a média real diretamente no banco
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.professional.id = :professionalId")
    Double calculateAverageRatingByProfessional(@Param("professionalId") String professionalId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.professional.id = :professionalId")
    Long countByProfessionalId(@Param("professionalId") String professionalId);
}