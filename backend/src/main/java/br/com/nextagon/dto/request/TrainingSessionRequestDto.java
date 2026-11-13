
package br.com.nextagon.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TrainingSessionRequestDto {

    @NotBlank(message = "Título da sessão é obrigatório")
    @Size(max = 255)
    private String title;

    @Min(value = 0, message = "Dia da semana inválido")
    @Max(value = 6, message = "Dia da semana inválido")
    private int dayOfWeek;

    @Size(max = 5000)
    private String notes;

    @Valid
    private List<ExerciseEntryRequestDto> exercises;
}
