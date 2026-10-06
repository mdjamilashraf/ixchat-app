package com.ultimate.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ultimate.chat.dto.ChatAttachmentReq;
import com.ultimate.chat.dto.ChatHistoryResponse;
import com.ultimate.chat.entity.ChatMessage;
import com.ultimate.chat.repository.ChatAttachmentRepository;
import com.ultimate.chat.repository.ChatMessageRepository;
import com.ultimate.chat.util.EncryptionService;

@Service
public class ChatHistoryService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatAttachmentRepository attachmentRepository;
	private final EncryptionService encryptionService;

	public ChatHistoryService(ChatMessageRepository chatMessageRepository,
			ChatAttachmentRepository attachmentRepository, EncryptionService encryptionService) {

		this.chatMessageRepository = chatMessageRepository;
		this.attachmentRepository = attachmentRepository;
		this.encryptionService = encryptionService;
	}

	public List<ChatHistoryResponse> getPrivateChatHistory(String userId, String receiverId, int page, int size) {

		// Protect against invalid values
		if (page < 0) {
			page = 0;
		}

		if (size <= 0) {
			size = 30;
		}

		if (size > 100) {
			size = 100;
		}

		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

		List<ChatMessage> messages = chatMessageRepository.findPrivateChatHistory(userId, receiverId, pageable);

		return messages.stream().map(message -> convertToResponse(message, userId, receiverId))
				.collect(Collectors.toList());
	}

	private ChatHistoryResponse convertToResponse(ChatMessage message, String userId, String receiverId) {

		ChatHistoryResponse response = new ChatHistoryResponse();

		response.setMessageId(message.getMessageId());

		response.setSenderId(message.getSenderId());

		/*
		 * Determine receiver for this private message.
		 */
		if (message.getSenderId().equals(userId)) {

			response.setReceiverId(receiverId);

		} else {

			response.setReceiverId(userId);
		}

		response.setMessageType(message.getMessageType());

		response.setCreatedAt(message.getCreatedAt());

		// ============================
		// DECRYPT MESSAGE
		// ============================

		if (message.getMessage() != null) {

			try {

				if (message.getEncrypted() != null && message.getEncrypted() == 1) {

					String decrypted = encryptionService.decryptString(message.getMessage(), message.getIv());

					response.setMessage(decrypted);

				} else {

					response.setMessage(message.getMessage());
				}

			} catch (Exception e) {

				throw new IllegalStateException("Failed to decrypt message " + message.getMessageId(), e);
			}
		}

		// ============================
		// ATTACHMENT
		// ============================

		attachmentRepository.findByMessageId(message.getMessageId()).ifPresent(attachment -> {

			ChatAttachmentReq dto = new ChatAttachmentReq();

			dto.setFileId(String.valueOf(attachment.getFileId()));

			dto.setFileName(attachment.getFileName());

			dto.setFileType(attachment.getFileType());

			dto.setFileSize(attachment.getFileSize());

			dto.setStoragePath(attachment.getStoragePath());

			dto.setDownloadUrl(attachment.getDownloadUrl());

			dto.setEncrypted(attachment.getEncrypted());

			dto.setIv(attachment.getIv());

			/*
			 * Do NOT set fileData here.
			 *
			 * fileData contains Base64 and can be very large.
			 */

			response.setAttachment(dto);
		});

		return response;
	}
}