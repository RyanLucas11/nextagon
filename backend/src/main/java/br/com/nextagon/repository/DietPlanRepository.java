package br.com.nextagon.repository;

import br.com.nextagon.model.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlan, String> {

    List<DietPlan> findByContractIdAndActiveTrue(String contractId);

    List<DietPlan> findByContractId(String contractId);
}