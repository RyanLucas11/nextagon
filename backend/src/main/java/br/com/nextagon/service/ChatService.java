package br.com.nextagon.service;

import br.com.nextagon.model.*;
import br.com.nextagon.repository.ContractRepository;
import br.com.nextagon.repository.MessageRepository;
import br.com.nextagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    public Message sendMessage(String contractId, String senderId, String content, String attachmentUrl) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Chat disponível apenas em contratos ACTIVE");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Remetente não encontrado"));

        boolean isParticipant = contract.getAthlete().getId().equals(senderId)
                || contract.getProfessional().getId().equals(senderId);

        if (!isParticipant) {
            throw new SecurityException("Você não tem permissão para enviar mensagem neste contrato");
        }

        User receiver = contract.getAthlete().getId().equals(senderId)
                ? contract.getProfessional()
                : contract.getAthlete();

        Message message = Message.builder()
                .contract(contract)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .attachmentUrl(attachmentUrl)
                .build();

        return messageRepository.save(message);
    }
    public List<Message> getMessagesByContract(String contractId, String requesterId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        boolean isParticipant = contract.getAthlete().getId().equals(requesterId)
                || contract.getProfessional().getId().equals(requesterId);

        if (!isParticipant) {
            throw new SecurityException("Você não tem acesso a este chat");
        }

        return messageRepository.findByContractIdOrderByCreatedAtAsc(contractId);
    }
}