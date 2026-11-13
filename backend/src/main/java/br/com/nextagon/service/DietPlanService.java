
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
    public DietPlan createPlan(
            String professionalId,
            DietPlanRequestDto dto
    ) {
        validateRequest(dto);

        Contract contract = getActiveContractForProfessional(
                dto.getContractId(),
                professionalId
        );

        DietPlan plan = DietPlan.builder()
                .contract(contract)
                .title(dto.getTitle().trim())
                .totalCalories(dto.getTotalCalories())
                .proteinG(dto.getProteinG())
                .carbsG(dto.getCarbsG())
                .fatsG(dto.getFatsG())
                .notes(dto.getNotes())
                .active(true)
                .build();

        return dietPlanRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<DietPlan> getPlansByContract(
            String contractId,
            String requesterId
    ) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Contrato não encontrado"
                        )
                );

        validateParticipant(contract, requesterId);
        validateActiveUser(contract, requesterId);

        return dietPlanRepository.findByContractIdAndActiveTrue(
                contractId
        );
    }

    @Transactional
    public DietPlan updatePlan(
            String planId,
            String professionalId,
            DietPlanRequestDto dto
    ) {
        validateRequest(dto);

        DietPlan plan = dietPlanRepository.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Plano de dieta não encontrado"
                        )
                );

        Contract contract = plan.getContract();

        validateProfessionalOwnership(contract, professionalId);
        validateActiveContract(contract);
        validateActiveParticipants(contract);

        if (!plan.isActive()) {
            throw new IllegalStateException(
                    "Não é possível atualizar um plano desativado"
            );
        }

        if (!contract.getId().equals(dto.getContractId())) {
            throw new IllegalArgumentException(
                    "Não é permitido transferir o plano para outro contrato"
            );
        }

        plan.setTitle(dto.getTitle().trim());
        plan.setTotalCalories(dto.getTotalCalories());
        plan.setProteinG(dto.getProteinG());
        plan.setCarbsG(dto.getCarbsG());
        plan.setFatsG(dto.getFatsG());
        plan.setNotes(dto.getNotes());

        return dietPlanRepository.save(plan);
    }

    @Transactional
    public void deactivatePlan(
            String planId,
            String professionalId
    ) {
        DietPlan plan = dietPlanRepository.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Plano não encontrado"
                        )
                );

        Contract contract = plan.getContract();

        validateProfessionalOwnership(contract, professionalId);
        validateActiveParticipants(contract);

        if (!plan.isActive()) {
            return;
        }

        plan.setActive(false);
        dietPlanRepository.save(plan);
    }

    private Contract getActiveContractForProfessional(
            String contractId,
            String professionalId
    ) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Contrato não encontrado"
                        )
                );

        validateProfessionalOwnership(contract, professionalId);
        validateActiveContract(contract);
        validateActiveParticipants(contract);

        return contract;
    }

    private void validateActiveContract(Contract contract) {
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Contrato não está ACTIVE"
            );
        }
    }

    private void validateProfessionalOwnership(
            Contract contract,
            String professionalId
    ) {
        if (!contract.getProfessional().getId()
                .equals(professionalId)) {
            throw new SecurityException(
                    "Acesso negado: você não é o profissional deste contrato"
            );
        }
    }

    private void validateParticipant(
            Contract contract,
            String userId
    ) {
        boolean isParticipant =
                contract.getAthlete().getId().equals(userId)
                        || contract.getProfessional().getId().equals(userId);

        if (!isParticipant) {
            throw new SecurityException(
                    "Acesso negado: você não é participante deste contrato"
            );
        }
    }

    private void validateActiveParticipants(Contract contract) {
        if (!contract.getAthlete().isActive()
                || !contract.getProfessional().isActive()) {
            throw new SecurityException(
                    "Não é possível modificar planos com participantes inativos"
            );
        }
    }

    private void validateActiveUser(
            Contract contract,
            String userId
    ) {
        User user = contract.getAthlete().getId().equals(userId)
                ? contract.getAthlete()
                : contract.getProfessional();

        if (!user.isActive()) {
            throw new SecurityException(
                    "Usuário inativo não pode acessar os planos"
            );
        }
    }

    private void validateRequest(DietPlanRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do plano são obrigatórios"
            );
        }

        if (dto.getContractId() == null
                || dto.getContractId().isBlank()) {
            throw new IllegalArgumentException(
                    "ID do contrato é obrigatório"
            );
        }

        if (dto.getTitle() == null
                || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Título é obrigatório"
            );
        }
    }
}
