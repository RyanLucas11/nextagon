package br.com.nextagon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ContractRequestDto {

    @NotBlank(message = "ID do profissional é obrigatório")
    private String professionalId;

    private String message; // mensagem inicial do atleta (opcional)
}