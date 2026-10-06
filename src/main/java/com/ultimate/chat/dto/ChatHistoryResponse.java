package com.ultimate.chat.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatHistoryResponse {

    private Integer messageId;
    private String senderId;
    private String receiverId;
    private String groupId;
    private String message;
    private String messageType;
    private LocalDateTime createdAt;

    private ChatAttachmentReq attachment;
}