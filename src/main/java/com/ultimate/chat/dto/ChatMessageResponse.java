package com.ultimate.chat.dto;

import java.time.LocalDateTime;

@lombok.Data
public class ChatMessageResponse {

	private Integer messageId;

	private String senderId;

	private String receiverId;

	private String message;

	private Short encrypted;

	private ChatAttachmentReq attachment;

	private String groupId;

	private String messageType;

	private LocalDateTime createdAt;

	public ChatMessageResponse() {
	}

}
