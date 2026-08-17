package br.com.nextagon.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AthleteProfileRequestDto {

    @NotBlank(message = "Nome de exibição é obrigatório")
    private String displayName;

    @DecimalMin(value = "50.0", message = "Altura mínima: 50cm")
    @DecimalMax(value = "250.0", message = "Altura máxima: 250cm")
    private Double height;

    @DecimalMin(value = "20.0", message = "Peso mínimo: 20kg")
    @DecimalMax(value = "500.0", message = "Peso máximo: 500kg")
    private Double weight;

    private List<String> goals;
    private String healthNotes;
    private String fitnessLevel; // beginner, intermediate, advanced
}