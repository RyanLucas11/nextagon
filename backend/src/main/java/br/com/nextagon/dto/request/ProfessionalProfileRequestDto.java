package br.com.nextagon.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProfessionalProfileRequestDto {

    @NotBlank(message = "Nome de exibição é obrigatório")
    private String displayName;

    private String bio;

    private List<String> specialties;

    private List<String> certificates;

    @DecimalMin(value = "0.0", message = "Valor hora não pode ser negativo")
    private Double hourlyRate;

    private Boolean available;
}