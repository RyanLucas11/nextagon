package br.com.nextagon.service;

import br.com.nextagon.dto.request.DietPlanRequestDto;
import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.DietPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;
    private final ContractRepository contractRepository;

    @Transactional
    public DietPlan createPlan(String professionalId, DietPlanRequestDto dto) {
        Contract contract = getActiveContractForProfessional(dto.getContractId(), professionalId);

        DietPlan plan = DietPlan.builder()
                .contract(contract)
                .title(dto.getTitle())
                .totalCalories(dto.getTotalCalories())
                .proteinG(dto.getProteinG())
                .carbsG(dto.getCarbsG())
                .fatsG(dto.getFatsG())
                .notes(dto.getNotes())
                .build();

        return dietPlanRepository.save(plan);
    }

    public List<DietPlan> getPlansByContract(String contractId, String requesterId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        validateParticipant(contract, requesterId);
        return dietPlanRepository.findByContractIdAndActiveTrue(contractId);
    }

    @Transactional
    public DietPlan updatePlan(String planId, String professionalId, DietPlanRequestDto dto) {
        DietPlan plan = dietPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano de dieta não encontrado"));

        validateProfessionalOwnership(plan.getContract(), professionalId);

        plan.setTitle(dto.getTitle());
        plan.setTotalCalories(dto.getTotalCalories());
        plan.setProteinG(dto.getProteinG());
        plan.setCarbsG(dto.getCarbsG());
        plan.setFatsG(dto.getFatsG());
        plan.setNotes(dto.getNotes());

        return dietPlanRepository.save(plan);
    }

    @Transactional
    public void deactivatePlan(String planId, String professionalId) {
        DietPlan plan = dietPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado"));

        validateProfessionalOwnership(plan.getContract(), professionalId);
        plan.setActive(false);
        dietPlanRepository.save(plan);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Contract getActiveContractForProfessional(String contractId, String professionalId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Contrato não está ACTIVE");
        }

        validateProfessionalOwnership(contract, professionalId);
        return contract;
    }

    private void validateProfessionalOwnership(Contract contract, String professionalId) {
        if (!contract.getProfessional().getId().equals(professionalId)) {
            throw new SecurityException("Acesso negado: você não é o profissional deste contrato");
        }
    }

    private void validateParticipant(Contract contract, String userId) {
        boolean isParticipant = contract.getAthlete().getId().equals(userId)
                || contract.getProfessional().getId().equals(userId);
        if (!isParticipant) {
            throw new SecurityException("Acesso negado: você não é participante deste contrato");
        }
    }
}