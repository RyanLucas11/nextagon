package br.com.nextagon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendMessageDto {

    @NotBlank(message = "ID do contrato é obrigatório")
    private String contractId;

    @NotBlank(message = "Conteúdo da mensagem é obrigatório")
    private String content;

    private String attachmentUrl;
}