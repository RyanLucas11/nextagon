
package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.MessageRepository;
import br.com.nextagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Contract createContractRequest(
            String athleteId,
            String professionalId,
            String message
    ) {
        User athlete = userRepository.findById(athleteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Atleta não encontrado"
                ));

        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profissional não encontrado"
                ));

        if (!athlete.isActive() || athlete.getRole() != Role.ATHLETE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas atletas ativos podem solicitar serviços"
            );
        }

        if (!professional.isActive()
                || professional.getRole() != Role.PROFESSIONAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O destinatário não é um profissional ativo"
            );
        }

        if (athleteId.equals(professionalId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível solicitar um contrato para si mesmo"
            );
        }

        contractRepository
                .findByAthleteIdAndProfessionalIdAndStatus(
                        athleteId,
                        professionalId,
                        ContractStatus.ACTIVE
                )
                .ifPresent(contract -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Já existe um contrato ativo com este profissional"
                    );
                });

        Contract contract = Contract.builder()
                .athlete(athlete)
                .professional(professional)
                .status(ContractStatus.PENDING)
                .athleteMessage(message)
                .build();

        return contractRepository.save(contract);
    }

    @Transactional
    public Contract respondToContract(
            String contractId,
            String professionalId,
            boolean accept
    ) {
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profissional não encontrado"
                ));

        if (!professional.isActive()
                || professional.getRole() != Role.PROFESSIONAL) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas profissionais ativos podem responder contratos"
            );
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contrato não encontrado"
                ));

        if (!contract.getProfessional().getId().equals(professionalId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode responder a este contrato"
            );
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Contrato não está mais pendente"
            );
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
    public Contract completeContract(
            String contractId,
            String professionalId
    ) {
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profissional não encontrado"
                ));

        if (!professional.isActive()
                || professional.getRole() != Role.PROFESSIONAL) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas profissionais ativos podem concluir contratos"
            );
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contrato não encontrado"
                ));

        if (!contract.getProfessional().getId().equals(professionalId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode concluir este contrato"
            );
        }

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Apenas contratos ativos podem ser concluídos"
            );
        }

        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletedAt(LocalDateTime.now());

        return contractRepository.save(contract);
    }

    @Transactional(readOnly = true)
    public List<Contract> getContractsByAthlete(String athleteId) {

        User athlete = userRepository.findById(athleteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Atleta não encontrado"
                ));

        if (!athlete.isActive() || athlete.getRole() != Role.ATHLETE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas atletas ativos podem consultar seus contratos"
            );
        }

        return contractRepository.findByAthleteId(athleteId);
    }

    @Transactional(readOnly = true)
    public List<Contract> getContractsByProfessional(String professionalId) {

        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profissional não encontrado"
                ));

        if (!professional.isActive()
                || professional.getRole() != Role.PROFESSIONAL) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas profissionais ativos podem consultar seus contratos"
            );
        }

        return contractRepository.findByProfessionalId(professionalId);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessagesByContract(
            String contractId,
            String requesterId
    ) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contrato não encontrado"
                ));

        boolean isParticipant =
                contract.getAthlete().getId().equals(requesterId)
                        || contract.getProfessional().getId().equals(requesterId);

        if (!isParticipant) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem acesso a este chat"
            );
        }

        return messageRepository.findByContractIdOrderByCreatedAtAsc(
                contractId
        );
    }
}