package com.ultimate.chat.dto;

@lombok.Data
public class GroupChatMessage {

	private String senderId;
	private String groupId;
	private String message;
	private String messageType;
	private ChatAttachmentReq attachment;

	public GroupChatMessage() {
	}

	public GroupChatMessage(String senderId, String groupId, String message, ChatAttachmentReq attachment) {

		this.senderId = senderId;
		this.groupId = groupId;
		this.message = message;
		this.attachment = attachment;
	}

}
