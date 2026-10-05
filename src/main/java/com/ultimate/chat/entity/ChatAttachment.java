package com.ultimate.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_attachment")
@lombok.Data
public class ChatAttachment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "file_id")
	private Integer fileId;

	@Column(name = "message_id", nullable = false)
	private Integer messageId;

	@Column(name = "file_name", length = 500, nullable = false)
	private String fileName;

	@Column(name = "file_type", length = 200)
	private String fileType;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "storage_path", length = 1000)
	private String storagePath;

	@Column(name = "download_url", length = 1000)
	private String downloadUrl;

//	@Column(name = "file_data", columnDefinition = "text")
//	private String fileData;

	@Column(name = "encrypted", nullable = false)
	private Short encrypted;

	@Column(name = "encryption_iv", length = 200)
	private String iv;

	// Default constructor
	public ChatAttachment() {
	}

	
}
