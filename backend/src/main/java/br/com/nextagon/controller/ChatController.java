package br.com.nextagon.controller;

import br.com.nextagon.dto.request.SendMessageDto;
import br.com.nextagon.dto.response.MessageResponseDto;
import br.com.nextagon.security.AuthenticatedUser;
import br.com.nextagon.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @Valid @RequestBody SendMessageDto dto) {

        String senderId = AuthenticatedUser.getId();
        var message = chatService.sendMessage(dto.getContractId(), senderId, dto.getContent(), dto.getAttachmentUrl());
        return ResponseEntity.ok(new MessageResponseDto(message));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<MessageResponseDto>> getChatHistory(
            @PathVariable String contractId) {

        String requesterId = AuthenticatedUser.getId();
        return ResponseEntity.ok(
                chatService.getMessagesByContract(contractId, requesterId)
                        .stream()
                        .map(MessageResponseDto::new)
                        .toList()
        );
    }
}