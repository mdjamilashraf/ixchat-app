package com.ultimate.chat.service;

import com.ultimate.chat.entity.ChatAttachment;
import com.ultimate.chat.entity.ChatMessage;
import com.ultimate.chat.entity.ChatMessageRecipient;
import com.ultimate.chat.repository.ChatAttachmentRepository;
import com.ultimate.chat.repository.ChatGroupMemberRepository;
import com.ultimate.chat.repository.ChatMessageRecipientRepository;
import com.ultimate.chat.repository.ChatMessageRepository;
import com.ultimate.chat.dto.ChatAttachmentReq;
import com.ultimate.chat.dto.ChatMessageReq;
import com.ultimate.chat.dto.GroupChatMessage;
import com.ultimate.chat.dto.GroupMessageResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.ultimate.chat.util.EncryptionService;

@Service
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatMessageRecipientRepository recipientRepository;
	private final ChatAttachmentRepository attachmentRepository;
	private final ChatGroupMemberRepository groupMemberRepository;
	private final EncryptionService encryptionService;

	public ChatMessageService(ChatMessageRepository chatMessageRepository,
			ChatMessageRecipientRepository recipientRepository, ChatAttachmentRepository attachmentRepository,
			ChatGroupMemberRepository groupMemberRepository, EncryptionService encryptionService) {

		this.chatMessageRepository = chatMessageRepository;
		this.recipientRepository = recipientRepository;
		this.attachmentRepository = attachmentRepository;
		this.groupMemberRepository = groupMemberRepository;
		this.encryptionService = encryptionService;
	}

	@Transactional
	public ChatMessage saveMessage(ChatMessageReq request) {

		// --------------------------------
		// 1. SAVE CHAT MESSAGE
		// --------------------------------

		ChatMessage message = new ChatMessage();

		message.setSenderId(request.getSenderId());

		message.setMessage(request.getMessage());

		// Encrypt message text before saving, if present
		if (request.getMessage() != null && !request.getMessage().isBlank()) {
			try {
				EncryptionService.StringEncryptionResult enc = encryptionService.encryptString(request.getMessage());
				message.setMessage(enc.cipherTextBase64);
				message.setEncrypted((short) 1);
				//AES-GCM, the IV/nonce is normally generated randomly for each message.
				message.setIv(enc.ivBase64);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to encrypt message", e);
			}
		} else {
			message.setEncrypted((short) 0);
		}

		String messageType = "TEXT";

		if (request.getAttachment() != null) {
			if (request.getMessage() != null && !request.getMessage().isBlank()) {
				messageType = "TEXT_FILE";
			} else {
				messageType = "FILE";
			}
		}

		message.setMessageType(messageType);

		message.setGroupId(request.getGroupId());

		message.setCreatedAt(LocalDateTime.now());

		ChatMessage savedMessage = chatMessageRepository.save(message);

		System.out.println("Saved chat_message ID = " + savedMessage.getMessageId());

		// --------------------------------
		// 2. SAVE RECIPIENT
		// --------------------------------

		ChatMessageRecipient recipient = new ChatMessageRecipient();

		recipient.setMessageId(savedMessage.getMessageId());

		recipient.setReceiverId(request.getReceiverId());

		recipient.setStatus("SENT");

		recipientRepository.save(recipient);

		// --------------------------------
		// 3. SAVE ATTACHMENT
		// --------------------------------

		if (request.getAttachment() != null) {

			ChatAttachmentReq attachmentDTO = request.getAttachment();

			ChatAttachment attachment = new ChatAttachment();

			// IMPORTANT:
			// Link attachment with the newly
			// generated chat_message ID
			attachment.setMessageId(savedMessage.getMessageId());

			attachment.setFileName(attachmentDTO.getFileName());

			attachment.setFileType(attachmentDTO.getFileType());

			attachment.setFileSize(attachmentDTO.getFileSize());

			attachment.setStoragePath(attachmentDTO.getStoragePath());

			attachment.setDownloadUrl(attachmentDTO.getDownloadUrl());
			attachment.setEncrypted(attachmentDTO.getEncrypted());
			attachment.setIv(attachmentDTO.getIv());
			//attachment.setFileData(attachmentDTO.getFileData());

			ChatAttachment savedAttachment = attachmentRepository.save(attachment);

			System.out.println("Saved attachment ID = " + savedAttachment.getFileId());
		}

		return savedMessage;
	}

	@Transactional
	public void markAsDelivered(Integer messageId, String receiverId) {

		recipientRepository.markAsDelivered(messageId, receiverId, LocalDateTime.now());
	}

	public GroupMessageResponse saveGrpMessage(GroupChatMessage request) {

		ChatMessage entity = new ChatMessage();

		entity.setSenderId(request.getSenderId());

		entity.setMessage(request.getMessage());

		// Encrypt message text before saving, if present
		if (request.getMessage() != null && !request.getMessage().isBlank()) {
			try {
				EncryptionService.StringEncryptionResult enc = encryptionService.encryptString(request.getMessage());
				entity.setMessage(enc.cipherTextBase64);
				entity.setEncrypted((short) 1);
				entity.setIv(enc.ivBase64);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to encrypt message", e);
			}
		} else {
			entity.setEncrypted((short) 0);
		}

		String messageType = "TEXT";
		if (request.getAttachment() != null) {

			if (request.getMessage() != null && !request.getMessage().isBlank()) {

				messageType = "TEXT_FILE";

			} else {

				messageType = "FILE";
			}
		}
		entity.setMessageType(messageType);

		entity.setGroupId(request.getGroupId());

		entity.setCreatedAt(LocalDateTime.now());

		ChatMessage savedChatMsg = chatMessageRepository.save(entity);
		// --------------------------------
		// 2. SAVE RECIPIENT
		// --------------------------------

		groupMemberRepository.findByGroupId(request.getGroupId()).forEach(member -> {
			ChatMessageRecipient recipient = new ChatMessageRecipient();
			recipient.setMessageId(savedChatMsg.getMessageId());
			if (member.getUserId().equals(request.getSenderId())) {
				recipient.setStatus("DELIVERED");
				recipient.setDeliveredAt(LocalDateTime.now());
			} else {
				recipient.setStatus("SENT");
			}
			recipient.setReceiverId(member.getUserId());
			recipientRepository.save(recipient);
		});
		
		// --------------------------------
		// 3. SAVE ATTACHMENT
		// --------------------------------
		
		if (request.getAttachment() != null) {
			ChatAttachmentReq attachmentDTO = request.getAttachment();
			ChatAttachment attachment = new ChatAttachment();
			attachment.setMessageId(savedChatMsg.getMessageId());
			attachment.setFileName(attachmentDTO.getFileName());
			attachment.setFileType(attachmentDTO.getFileType());
			attachment.setFileSize(attachmentDTO.getFileSize());
			attachment.setStoragePath(attachmentDTO.getStoragePath());
			attachment.setDownloadUrl(attachmentDTO.getDownloadUrl());
			attachment.setEncrypted(attachmentDTO.getEncrypted());
			attachment.setIv(attachmentDTO.getIv());
			//attachment.setFileData(attachmentDTO.getFileData());
			ChatAttachment savedAttachment = attachmentRepository.save(attachment);
			System.out.println("Saved attachment ID = " + savedAttachment.getFileId());
		}

		GroupMessageResponse response = new GroupMessageResponse();

		response.setMessageId(savedChatMsg.getMessageId());

		response.setGroupId(savedChatMsg.getGroupId());

		response.setSenderId(savedChatMsg.getSenderId());

		response.setMessage(savedChatMsg.getMessage());

		response.setMessageType(savedChatMsg.getMessageType());

		response.setCreatedAt(savedChatMsg.getCreatedAt());

		return response;
	}
}
