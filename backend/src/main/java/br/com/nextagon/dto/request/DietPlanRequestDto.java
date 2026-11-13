
package br.com.nextagon.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DietPlanRequestDto {

    @NotBlank(message = "ID do contrato é obrigatório")
    private String contractId;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255)
    private String title;

    @PositiveOrZero(message = "Calorias não podem ser negativas")
    private Integer totalCalories;

    @DecimalMin(value = "0.0", message = "Proteínas não podem ser negativas")
    private Double proteinG;

    @DecimalMin(value = "0.0", message = "Carboidratos não podem ser negativos")
    private Double carbsG;

    @DecimalMin(value = "0.0", message = "Gorduras não podem ser negativas")
    private Double fatsG;

    @Size(max = 5000)
    private String notes;
}
