package br.com.nextagon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DietPlanRequestDto {

    @NotBlank(message = "ID do contrato é obrigatório")
    private String contractId;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private Integer totalCalories;
    private Double proteinG;
    private Double carbsG;
    private Double fatsG;
    private String notes;
}