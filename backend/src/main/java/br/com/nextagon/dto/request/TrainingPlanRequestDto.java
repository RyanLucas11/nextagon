package br.com.nextagon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TrainingPlanRequestDto {

    @NotBlank(message = "ID do contrato é obrigatório")
    private String contractId;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;
    private List<TrainingSessionRequestDto> sessions;
}