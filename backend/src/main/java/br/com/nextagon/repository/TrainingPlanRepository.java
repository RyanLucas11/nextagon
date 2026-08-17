package br.com.nextagon.repository;

import br.com.nextagon.model.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, String> {

    List<TrainingPlan> findByContractIdAndActiveTrue(String contractId);

    List<TrainingPlan> findByContractId(String contractId);
}