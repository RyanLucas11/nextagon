package br.com.nextagon.dto.response;

import br.com.nextagon.model.Message;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponseDto {

    private final String id;
    private final String contractId;
    private final String senderId;
    private final String senderName;
    private final String receiverId;
    private final String content;
    private final String attachmentUrl;
    private final boolean read;
    private final LocalDateTime createdAt;

    public MessageResponseDto(Message message) {
        this.id = message.getId();
        this.contractId = message.getContract().getId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getName();
        this.receiverId = message.getReceiver().getId();
        this.content = message.getContent();
        this.attachmentUrl = message.getAttachmentUrl();
        this.read = message.isRead();
        this.createdAt = message.getCreatedAt();
    }
}