
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final ContractRepository contractRepository;

    @Transactional
    public TrainingPlan createPlan(
            String professionalId,
            TrainingPlanRequestDto dto
    ) {
        validateRequest(dto);

        Contract contract = getContract(dto.getContractId());

        validateProfessionalOwnership(contract, professionalId);
        validateActiveContract(contract);
        validateActiveParticipants(contract);

        TrainingPlan plan = TrainingPlan.builder()
                .contract(contract)
                .title(dto.getTitle().trim())
                .description(dto.getDescription())
                .sessions(new ArrayList<>())
                .active(true)
                .build();

        replaceSessions(plan, dto.getSessions());

        return trainingPlanRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<TrainingPlan> getPlansByContract(
            String contractId,
            String requesterId
    ) {
        Contract contract = getContract(contractId);

        validateParticipant(contract, requesterId);
        validateActiveRequester(contract, requesterId);

        return trainingPlanRepository
                .findByContractIdAndActiveTrue(contractId);
    }

    @Transactional
    public TrainingPlan updatePlan(
            String planId,
            String professionalId,
            TrainingPlanRequestDto dto
    ) {
        TrainingPlan plan = getPlan(planId);
        Contract contract = plan.getContract();

        // Verificar autorização antes de processar as alterações.
        validateProfessionalOwnership(contract, professionalId);
        validateActiveParticipants(contract);
        validateActiveContract(contract);

        if (!plan.isActive()) {
            throw new IllegalStateException(
                    "Não é possível atualizar um plano desativado"
            );
        }

        validateRequest(dto);

        if (!Objects.equals(contract.getId(), dto.getContractId())) {
            throw new IllegalArgumentException(
                    "Não é permitido transferir o plano para outro contrato"
            );
        }

        plan.setTitle(dto.getTitle().trim());
        plan.setDescription(dto.getDescription());

        // null mantém as sessões existentes.
        // Uma lista vazia remove todas as sessões.
        if (dto.getSessions() != null) {
            replaceSessions(plan, dto.getSessions());
        }

        return trainingPlanRepository.save(plan);
    }

    @Transactional
    public void deactivatePlan(
            String planId,
            String professionalId
    ) {
        TrainingPlan plan = getPlan(planId);
        Contract contract = plan.getContract();

        validateProfessionalOwnership(contract, professionalId);
        validateActiveParticipants(contract);

        // Operação idempotente: repetir DELETE não altera o resultado.
        if (!plan.isActive()) {
            return;
        }

        plan.setActive(false);
        trainingPlanRepository.save(plan);
    }

    private TrainingPlan getPlan(String planId) {
        return trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plano não encontrado"
                ));
    }

    private Contract getContract(String contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contrato não encontrado"
                ));
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

    private void validateActiveRequester(
            Contract contract,
            String userId
    ) {
        User requester = contract.getAthlete().getId().equals(userId)
                ? contract.getAthlete()
                : contract.getProfessional();

        if (!requester.isActive()) {
            throw new SecurityException(
                    "Usuário inativo não pode acessar os planos"
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

    private void validateRequest(TrainingPlanRequestDto dto) {
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

    private void replaceSessions(
            TrainingPlan plan,
            List<TrainingSessionRequestDto> sessions
    ) {
        if (sessions == null) {
            return;
        }

        // Mantém a coleção gerenciada pelo Hibernate.
        plan.getSessions().clear();

        int sessionOrder = 0;

        for (TrainingSessionRequestDto sessionDto : sessions) {
            if (sessionDto == null) {
                throw new IllegalArgumentException(
                        "Sessão de treino inválida"
                );
            }

            TrainingSession session = buildSession(
                    sessionDto,
                    plan,
                    sessionOrder++
            );

            plan.getSessions().add(session);
        }
    }

    private TrainingSession buildSession(
            TrainingSessionRequestDto dto,
            TrainingPlan plan,
            int order
    ) {
        if (dto.getTitle() == null
                || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Título da sessão é obrigatório"
            );
        }

        if (dto.getDayOfWeek() < 0 || dto.getDayOfWeek() > 6) {
            throw new IllegalArgumentException(
                    "Dia da semana inválido"
            );
        }

        TrainingSession session = TrainingSession.builder()
                .trainingPlan(plan)
                .title(dto.getTitle().trim())
                .dayOfWeek(dto.getDayOfWeek())
                .notes(dto.getNotes())
                .orderIndex(order)
                .exercises(new ArrayList<>())
                .build();

        if (dto.getExercises() != null) {
            int exerciseOrder = 0;

            for (ExerciseEntryRequestDto exerciseDto : dto.getExercises()) {
                if (exerciseDto == null
                        || exerciseDto.getName() == null
                        || exerciseDto.getName().isBlank()) {
                    throw new IllegalArgumentException(
                            "Nome do exercício é obrigatório"
                    );
                }

                validateExerciseValues(exerciseDto);

                ExerciseEntry exercise = ExerciseEntry.builder()
                        .session(session)
                        .name(exerciseDto.getName().trim())
                        .sets(exerciseDto.getSets())
                        .reps(exerciseDto.getReps())
                        .weightKg(exerciseDto.getWeightKg())
                        .restSecs(exerciseDto.getRestSecs())
                        .notes(exerciseDto.getNotes())
                        .videoUrl(exerciseDto.getVideoUrl())
                        .orderIndex(exerciseOrder++)
                        .build();

                session.getExercises().add(exercise);
            }
        }

        return session;
    }

    private void validateExerciseValues(
            ExerciseEntryRequestDto dto
    ) {
        if (dto.getSets() != null && dto.getSets() <= 0) {
            throw new IllegalArgumentException(
                    "Número de séries deve ser positivo"
            );
        }

        if (dto.getWeightKg() != null
                && (!Double.isFinite(dto.getWeightKg())
                || dto.getWeightKg() < 0)) {
            throw new IllegalArgumentException(
                    "Peso do exercício deve ser válido e não negativo"
            );
        }

        if (dto.getRestSecs() != null && dto.getRestSecs() < 0) {
            throw new IllegalArgumentException(
                    "Tempo de descanso não pode ser negativo"
            );
        }
    }
}
