package com.ultimate.chat.dto;

@lombok.Data
public class ChatDeliveredReq {
	private Integer messageId;

	private String receiverId;

	public ChatDeliveredReq() {
	}

}
