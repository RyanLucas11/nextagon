package br.com.nextagon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExerciseEntryRequestDto {

    @NotBlank(message = "Nome do exercício é obrigatório")
    private String name;

    private Integer sets;
    private String reps;    // "8-12" ou "AMRAP"
    private Double weightKg;
    private Integer restSecs;
    private String notes;
    private String videoUrl;
}