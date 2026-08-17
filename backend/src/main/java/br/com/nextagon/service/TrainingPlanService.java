package br.com.nextagon.service;

import br.com.nextagon.dto.request.ExerciseEntryRequestDto;
import br.com.nextagon.dto.request.TrainingPlanRequestDto;
import br.com.nextagon.dto.request.TrainingSessionRequestDto;
import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.TrainingPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final ContractRepository contractRepository;

    @Transactional
    public TrainingPlan createPlan(String professionalId, TrainingPlanRequestDto dto) {
        Contract contract = getActiveContractForProfessional(dto.getContractId(), professionalId);

        TrainingPlan plan = TrainingPlan.builder()
                .contract(contract)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .sessions(new ArrayList<>())
                .build();

        if (dto.getSessions() != null) {
            int order = 0;
            for (TrainingSessionRequestDto sessionDto : dto.getSessions()) {
                TrainingSession session = buildSession(sessionDto, plan, order++);
                plan.getSessions().add(session);
            }
        }

        return trainingPlanRepository.save(plan);
    }

    // Profissional busca planos do contrato dele
    public List<TrainingPlan> getPlansByContract(String contractId, String requesterId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        validateParticipant(contract, requesterId);

        return trainingPlanRepository.findByContractIdAndActiveTrue(contractId);
    }

    @Transactional
    public TrainingPlan updatePlan(String planId, String professionalId, TrainingPlanRequestDto dto) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado"));

        // Garante que o profissional é dono do contrato
        validateProfessionalOwnership(plan.getContract(), professionalId);

        plan.setTitle(dto.getTitle());
        plan.setDescription(dto.getDescription());

        if (dto.getSessions() != null) {
            plan.getSessions().clear();
            int order = 0;
            for (TrainingSessionRequestDto sessionDto : dto.getSessions()) {
                plan.getSessions().add(buildSession(sessionDto, plan, order++));
            }
        }

        return trainingPlanRepository.save(plan);
    }

    @Transactional
    public void deactivatePlan(String planId, String professionalId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado"));

        validateProfessionalOwnership(plan.getContract(), professionalId);
        plan.setActive(false);
        trainingPlanRepository.save(plan);
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

    private TrainingSession buildSession(TrainingSessionRequestDto dto, TrainingPlan plan, int order) {
        TrainingSession session = TrainingSession.builder()
                .trainingPlan(plan)
                .title(dto.getTitle())
                .dayOfWeek(dto.getDayOfWeek())
                .notes(dto.getNotes())
                .orderIndex(order)
                .exercises(new ArrayList<>())
                .build();

        if (dto.getExercises() != null) {
            int exOrder = 0;
            for (ExerciseEntryRequestDto exDto : dto.getExercises()) {
                session.getExercises().add(ExerciseEntry.builder()
                        .session(session)
                        .name(exDto.getName())
                        .sets(exDto.getSets())
                        .reps(exDto.getReps())
                        .weightKg(exDto.getWeightKg())
                        .restSecs(exDto.getRestSecs())
                        .notes(exDto.getNotes())
                        .videoUrl(exDto.getVideoUrl())
                        .orderIndex(exOrder++)
                        .build());
            }
        }

        return session;
    }
}