package com.ultimate.chat.service;

import com.ultimate.chat.dto.ChatAttachmentReq;
import com.ultimate.chat.dto.ChatMessageResponse;
import com.ultimate.chat.entity.ChatAttachment;
import com.ultimate.chat.entity.ChatMessage;
import com.ultimate.chat.entity.ChatMessageRecipient;
import com.ultimate.chat.repository.ChatAttachmentRepository;
import com.ultimate.chat.repository.ChatMessageRecipientRepository;
import com.ultimate.chat.repository.ChatMessageRepository;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PendingMessageService {

	private final ChatMessageRecipientRepository recipientRepository;

	private final ChatMessageRepository messageRepository;

	private final SimpMessagingTemplate messagingTemplate;
	private final com.ultimate.chat.util.EncryptionService encryptionService;

	private final ChatAttachmentRepository attachmentRepository;

	public PendingMessageService(ChatMessageRecipientRepository recipientRepository,
			ChatMessageRepository messageRepository, ChatAttachmentRepository attachmentRepository,
			SimpMessagingTemplate messagingTemplate, com.ultimate.chat.util.EncryptionService encryptionService) {

		this.recipientRepository = recipientRepository;
		this.messageRepository = messageRepository;
		this.attachmentRepository = attachmentRepository;
		this.messagingTemplate = messagingTemplate;
		this.encryptionService = encryptionService;
	}

	@Transactional
	public void deliverPendingMessages(String userId) {

		System.out.println("Checking pending messages for: " + userId);

		List<ChatMessageRecipient> pendingMessages = recipientRepository
				.findByReceiverIdAndStatusOrderByRecipientIdAsc(userId, "SENT");

		System.out.println("Pending message count = " + pendingMessages.size());

		for (ChatMessageRecipient recipient : pendingMessages) {

			System.out.println("Delivering messageId = " + recipient.getMessageId());

			// Get actual chat message
			ChatMessage message = messageRepository.findById(recipient.getMessageId()).orElse(null);

			if (message == null) {

				System.out.println("Message not found: " + recipient.getMessageId());

				continue;
			}

			Optional<ChatAttachment> attachmentOptional = attachmentRepository.findByMessageId(message.getMessageId());
			if (attachmentOptional.isPresent()) {

				ChatAttachment attachment = attachmentOptional.get();
				ChatAttachmentReq attachmentDTO = new ChatAttachmentReq();

				// fileId here was originally DB id; keep storagePath for client reference
				attachmentDTO.setFileId(attachment.getStoragePath());
				attachmentDTO.setFileName(attachment.getFileName());
				attachmentDTO.setFileType(attachment.getFileType());
				attachmentDTO.setFileSize(attachment.getFileSize());
				attachmentDTO.setStoragePath(attachment.getStoragePath());
				attachmentDTO.setDownloadUrl(attachment.getDownloadUrl());
				//message.setAttachment(attachmentDTO);

			} else {

				System.out.println("No attachment for messageId = " + message.getMessageId());
			}

			ChatMessageResponse res = createResponse(message, userId);
			// Send to USER002
			messagingTemplate.convertAndSend("/Ix-topic/user/" + userId, res);

			// UPDATE DATABASE
			recipient.setStatus("DELIVERED");

			recipient.setDeliveredAt(LocalDateTime.now());

			recipientRepository.save(recipient);

			System.out.println("Message delivered. recipientId = " + recipient.getRecipientId());
		}
	}
	
	private ChatMessageResponse createResponse(
	        ChatMessage message,
	        String receiverId) {

	    ChatMessageResponse response =
	            new ChatMessageResponse();

	    response.setMessageId(message.getMessageId());
	    response.setSenderId(message.getSenderId());
	    response.setReceiverId(receiverId);
	    response.setEncrypted(message.getEncrypted());
					try {
						if (message.getEncrypted() !=null && message.getEncrypted()==1) {
							String decrypted = encryptionService.decryptString(message.getMessage(), message.getIv());
							response.setMessage(decrypted);
						} else {
							response.setMessage(message.getMessage());
						}
					} catch (Exception e) {
						// in case decryption fails, fall back to raw message
						response.setMessage(message.getMessage());
					}
	    response.setGroupId(message.getGroupId());
	    response.setMessageType(message.getMessageType());
	    response.setCreatedAt(message.getCreatedAt());


	    Optional<ChatAttachment> attachmentOptional =
	            attachmentRepository.findByMessageId(
	                    message.getMessageId()
	            );

	    if (attachmentOptional.isPresent()) {

	        ChatAttachment attachment =
	                attachmentOptional.get();

	        ChatAttachmentReq dto =
	                new ChatAttachmentReq();

	        dto.setFileId(
	                String.valueOf(attachment.getFileId())
	        );

	        dto.setFileName(attachment.getFileName());
	        dto.setFileType(attachment.getFileType());
	        dto.setFileSize(attachment.getFileSize());
	        dto.setStoragePath(attachment.getStoragePath());
	        dto.setDownloadUrl(attachment.getDownloadUrl());

	        dto.setEncrypted(attachment.getEncrypted());
	        dto.setIv(attachment.getIv());
	        dto.setFileData(attachment.getFileData());

	        response.setAttachment(dto);
	    }

	    return response;
	}
}