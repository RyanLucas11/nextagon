package br.com.nextagon.dto.response;

import br.com.nextagon.model.Contract;
import br.com.nextagon.model.ContractStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ContractResponseDto {

    private final String id;
    private final String athleteId;
    private final String athleteName;
    private final String professionalId;
    private final String professionalName;
    private final ContractStatus status;
    private final String athleteMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;

    public ContractResponseDto(Contract contract) {
        this.id = contract.getId();
        this.athleteId = contract.getAthlete().getId();
        this.athleteName = contract.getAthlete().getName();
        this.professionalId = contract.getProfessional().getId();
        this.professionalName = contract.getProfessional().getName();
        this.status = contract.getStatus();
        this.athleteMessage = contract.getAthleteMessage();
        this.createdAt = contract.getCreatedAt();
        this.startedAt = contract.getStartedAt();
        this.completedAt = contract.getCompletedAt();
    }
}