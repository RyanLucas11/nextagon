
package br.com.nextagon.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TrainingPlanRequestDto {

    @NotBlank(message = "ID do contrato é obrigatório")
    private String contractId;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título muito longo")
    private String title;

    @Size(max = 5000, message = "Descrição muito longa")
    private String description;

    @Valid
    private List<TrainingSessionRequestDto> sessions;
}
