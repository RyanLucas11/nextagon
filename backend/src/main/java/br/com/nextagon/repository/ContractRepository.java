package br.com.nextagon.repository;

import br.com.nextagon.model.Contract;
import br.com.nextagon.model.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {

    List<Contract> findByAthleteId(String athleteId);

    List<Contract> findByProfessionalId(String professionalId);

    List<Contract> findByAthleteIdAndStatus(String athleteId, ContractStatus status);

    List<Contract> findByProfessionalIdAndStatus(String professionalId, ContractStatus status);

    // Verifica contrato ACTIVE entre par atleta/profissional (regra de unicidade)
    Optional<Contract> findByAthleteIdAndProfessionalIdAndStatus(
            String athleteId, String professionalId, ContractStatus status);

    // Busca contrato verificando se userId é participante (atleta ou profissional)
    @Query("SELECT c FROM Contract c WHERE c.id = :contractId " +
            "AND (c.athlete.id = :userId OR c.professional.id = :userId)")
    Optional<Contract> findByIdAndParticipant(@Param("contractId") String contractId,
                                              @Param("userId") String userId);
}