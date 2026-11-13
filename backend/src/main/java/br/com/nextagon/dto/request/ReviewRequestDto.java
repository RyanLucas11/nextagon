
package br.com.nextagon.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto {

    @NotBlank(message = "ID do contrato é obrigatório")
    private String contractId;

    @Min(value = 1, message = "A nota mínima é 1")
    @Max(value = 5, message = "A nota máxima é 5")
    private int rating;

    @Size(
            max = 2000,
            message = "O comentário deve ter no máximo 2000 caracteres"
    )
    private String comment;
}
