package br.com.nextagon.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TrainingSessionRequestDto {

    @NotBlank(message = "Título da sessão é obrigatório")
    private String title;

    @Min(0) @Max(6)
    private int dayOfWeek; // 0=Dom, 6=Sáb

    private String notes;
    private List<ExerciseEntryRequestDto> exercises;
}