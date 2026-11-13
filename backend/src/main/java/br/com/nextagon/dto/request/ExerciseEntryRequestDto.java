
package br.com.nextagon.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExerciseEntryRequestDto {

    @NotBlank(message = "Nome do exercício é obrigatório")
    @Size(max = 255)
    private String name;

    @Positive(message = "Número de séries deve ser positivo")
    private Integer sets;

    @Size(max = 100)
    private String reps;

    @DecimalMin(
            value = "0.0",
            message = "Peso não pode ser negativo"
    )
    private Double weightKg;

    @PositiveOrZero(
            message = "Tempo de descanso não pode ser negativo"
    )
    private Integer restSecs;

    @Size(max = 5000)
    private String notes;

    @Size(max = 2048)
    private String videoUrl;
}
