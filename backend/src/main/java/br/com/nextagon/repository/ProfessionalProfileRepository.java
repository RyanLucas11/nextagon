package br.com.nextagon.repository;

import br.com.nextagon.model.ProfessionalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, String> {

    Optional<ProfessionalProfile> findByUserId(String userId);
}