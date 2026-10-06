package com.ultimate.chat.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@lombok.Data
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Integer messageId;

	@Column(name = "sender_id", length = 100, nullable = false)
	private String senderId;

	//@Lob
	@Column(name = "message_text", columnDefinition = "text")
	private String message;

	@Column(name = "encrypted", nullable = false)
	private Short encrypted;

	@Column(name = "encryption_iv", length = 200)
	private String iv;

	@Column(name = "message_type", length = 20, nullable = false)
	private String messageType;

	@Column(name = "group_id", length = 100)
	private String groupId;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	// Default constructor
	public ChatMessage() {
	}

	
}
