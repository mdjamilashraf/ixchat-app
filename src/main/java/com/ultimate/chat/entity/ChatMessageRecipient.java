package com.ultimate.chat.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message_recipient")
@lombok.Data
public class ChatMessageRecipient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipient_id")
	private Integer recipientId;

	@Column(name = "message_id", nullable = false)
	private Integer messageId;

	@Column(name = "receiver_id", length = 100, nullable = false)
	private String receiverId;

	@Column(name = "status", length = 20, nullable = false)
	private String status;

	@Column(name = "delivered_at")
	private LocalDateTime deliveredAt;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	// Default constructor
	public ChatMessageRecipient() {
	}

	
}
