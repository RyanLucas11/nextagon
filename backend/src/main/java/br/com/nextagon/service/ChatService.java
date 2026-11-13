
package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.MessageRepository;
import br.com.nextagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    @Transactional
    public Message sendMessage(
            String contractId,
            String senderId,
            String content,
            String attachmentUrl
    ) {
        Contract contract = findContract(contractId);

        validateParticipant(contract, senderId);

        User sender = findActiveUser(senderId);

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O envio de mensagens está disponível apenas em contratos ativos"
            );
        }

        if ((content == null || content.isBlank())
                && (attachmentUrl == null || attachmentUrl.isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe uma mensagem ou um anexo"
            );
        }

        User receiver = contract.getAthlete().getId().equals(senderId)
                ? contract.getProfessional()
                : contract.getAthlete();

        if (!receiver.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O destinatário não está ativo"
            );
        }

        Message message = Message.builder()
                .contract(contract)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .attachmentUrl(attachmentUrl)
                .build();

        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessagesByContract(
            String contractId,
            String requesterId
    ) {
        Contract contract = findContract(contractId);

        validateParticipant(contract, requesterId);
        findActiveUser(requesterId);

        return messageRepository
                .findByContractIdOrderByCreatedAtAsc(contractId);
    }

    private Contract findContract(String contractId) {
        if (contractId == null || contractId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID do contrato é obrigatório"
            );
        }

        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contrato não encontrado"
                ));
    }

    private User findActiveUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário não encontrado"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuário inativo não pode acessar o chat"
            );
        }

        return user;
    }

    private void validateParticipant(
            Contract contract,
            String userId
    ) {
        boolean isAthlete =
                contract.getAthlete().getId().equals(userId);

        boolean isProfessional =
                contract.getProfessional().getId().equals(userId);

        if (!isAthlete && !isProfessional) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar este chat"
            );
        }
    }
}
