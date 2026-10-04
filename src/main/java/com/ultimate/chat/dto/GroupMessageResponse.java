package com.ultimate.chat.dto;

import java.time.LocalDateTime;

@lombok.Data
public class GroupMessageResponse {

	private Integer messageId;
	private String senderId;
	private String message;
	private String messageType;
	private String groupId;
	private LocalDateTime createdAt;
}
