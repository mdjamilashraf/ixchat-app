package com.ultimate.chat.dto;

@lombok.Data
public class ChatMessageReq {

	private Integer messageId;
	private String senderId;
	private String receiverId;
	private String message;
	private String messageType;
	private String groupId;
	
	private ChatAttachmentReq attachment;

	public ChatMessageReq() {
	}

	public ChatMessageReq(String senderId, String receiverId, String message, ChatAttachmentReq attachment) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.message = message;
		this.attachment = attachment;
	}

	
}
