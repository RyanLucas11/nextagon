package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.MessageRepository;
import br.com.nextagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Contract createContractRequest(String athleteId, String professionalId) {
        User athlete = userRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Atleta não encontrado"));

        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        if (athlete.getRole() != Role.ATHLETE) {
            throw new SecurityException("Apenas atletas podem solicitar serviços");
        }

        if (professional.getRole() != Role.PROFESSIONAL) {
            throw new IllegalArgumentException("O destinatário não é um profissional");
        }

        contractRepository.findByAthleteIdAndProfessionalIdAndStatus(
                        athleteId, professionalId, ContractStatus.ACTIVE)
                .ifPresent(c -> { throw new IllegalStateException("Já existe um contrato ativo com este profissional"); });

        Contract contract = Contract.builder()
                .athlete(athlete)
                .professional(professional)
                .status(ContractStatus.PENDING)
                .build();

        return contractRepository.save(contract);
    }

    @Transactional
    public Contract respondToContract(String contractId, String professionalId, boolean accept) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contract.getProfessional().getId().equals(professionalId)) {
            throw new SecurityException("Apenas o profissional vinculado pode responder este contrato");
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new IllegalStateException("Contrato não está mais em estado PENDING");
        }

        if (accept) {
            contract.setStatus(ContractStatus.ACTIVE);
            contract.setStartedAt(LocalDateTime.now());
        } else {
            contract.setStatus(ContractStatus.CANCELLED);
        }

        return contractRepository.save(contract);
    }

    @Transactional
    public Contract completeContract(String contractId, String professionalId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contract.getProfessional().getId().equals(professionalId)) {
            throw new SecurityException("Apenas o profissional pode concluir o contrato");
        }

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Apenas contratos ACTIVE podem ser concluídos");
        }

        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletedAt(LocalDateTime.now());

        return contractRepository.save(contract);
    }

    public List<Contract> getContractsByAthlete(String athleteId) {
        return contractRepository.findByAthleteId(athleteId);
    }

    public List<Contract> getContractsByProfessional(String professionalId) {
        return contractRepository.findByProfessionalId(professionalId);
    }

    public List<Message> getMessagesByContract(String contractId, String requesterId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        boolean isParticipant = contract.getAthlete().getId().equals(requesterId)
                || contract.getProfessional().getId().equals(requesterId);

        if (!isParticipant) {
            throw new SecurityException("Você não tem acesso a este chat");
        }

        return messageRepository.findByContractIdOrderByCreatedAtAsc(contractId);
    }
}