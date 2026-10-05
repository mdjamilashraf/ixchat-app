package com.ultimate.chat.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ultimate.chat.dto.ChatAttachmentReq;
import com.ultimate.chat.dto.GroupChatMessage;
import com.ultimate.chat.dto.GroupMessageResponse;
import com.ultimate.chat.entity.ChatAttachment;
import com.ultimate.chat.entity.ChatMessage;
import com.ultimate.chat.entity.ChatMessageRecipient;
import com.ultimate.chat.repository.ChatAttachmentRepository;
import com.ultimate.chat.repository.ChatGroupMemberRepository;
import com.ultimate.chat.repository.ChatMessageRecipientRepository;
import com.ultimate.chat.repository.ChatMessageRepository;
import com.ultimate.chat.util.EncryptionService;

@Service
public class GroupMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatGroupMemberRepository groupMemberRepository;
	private final ChatMessageRecipientRepository recipientRepository;
	private final ChatAttachmentRepository attachmentRepository;
	private final EncryptionService encryptionService;

	public GroupMessageService(ChatMessageRepository chatMessageRepository,
			ChatGroupMemberRepository groupMemberRepository, ChatMessageRecipientRepository recipientRepository,
			ChatAttachmentRepository attachmentRepository, EncryptionService encryptionService) {

		this.chatMessageRepository = chatMessageRepository;
		this.groupMemberRepository = groupMemberRepository;
		this.recipientRepository = recipientRepository;
		this.attachmentRepository = attachmentRepository;
		this.encryptionService = encryptionService;
	}

	@Transactional
	public GroupMessageResponse saveGrpMessage(GroupChatMessage request) {

	    ChatMessage entity = new ChatMessage();

	    entity.setSenderId(request.getSenderId());
	    entity.setMessage(request.getMessage());

	    // =========================
	    // ENCRYPT MESSAGE
	    // =========================

	    if (request.getMessage() != null &&
	            !request.getMessage().isBlank()) {

	        try {

	            EncryptionService.StringEncryptionResult enc =
	                    encryptionService.encryptString(
	                            request.getMessage());

	            entity.setMessage(enc.cipherTextBase64);
	            entity.setEncrypted((short) 1);
	            entity.setIv(enc.ivBase64);

	        } catch (Exception e) {
	            throw new IllegalStateException(
	                    "Failed to encrypt group message", e);
	        }

	    } else {

	        entity.setEncrypted((short) 0);
	    }

	    // =========================
	    // MESSAGE TYPE
	    // =========================

	    String messageType = "TEXT";

	    if (request.getAttachment() != null) {

	        if (request.getMessage() != null &&
	                !request.getMessage().isBlank()) {

	            messageType = "TEXT_FILE";

	        } else {

	            messageType = "FILE";
	        }
	    }

	    entity.setMessageType(messageType);
	    entity.setGroupId(request.getGroupId());
	    entity.setCreatedAt(LocalDateTime.now());

	    // =========================
	    // SAVE MESSAGE
	    // =========================

	    ChatMessage saved =
	            chatMessageRepository.save(entity);

	    // =========================
	    // SAVE RECIPIENTS
	    // =========================

	    groupMemberRepository
	        .findByGroupId(request.getGroupId())
	        .forEach(member -> {

	            ChatMessageRecipient recipient =
	                    new ChatMessageRecipient();

	            recipient.setMessageId(
	                    saved.getMessageId());

	            recipient.setReceiverId(
	                    member.getUserId());

	            if (member.getUserId()
	                    .equals(request.getSenderId())) {

	                recipient.setStatus("DELIVERED");
	                recipient.setDeliveredAt(
	                        LocalDateTime.now());

	            } else {

	                recipient.setStatus("SENT");
	            }

	            recipientRepository.save(recipient);
	        });

	    // =========================
	    // SAVE ATTACHMENT
	    // =========================

	    ChatAttachmentReq attachmentDTO =
	            request.getAttachment();

	    if (attachmentDTO != null) {

	        ChatAttachment attachment =
	                new ChatAttachment();

	        attachment.setMessageId(
	                saved.getMessageId());

	        attachment.setFileName(
	                attachmentDTO.getFileName());

	        attachment.setFileType(
	                attachmentDTO.getFileType());

	        attachment.setFileSize(
	                attachmentDTO.getFileSize());

	        attachment.setStoragePath(
	                attachmentDTO.getStoragePath());

	        attachment.setDownloadUrl(
	                attachmentDTO.getDownloadUrl());

	        attachment.setEncrypted(
	                attachmentDTO.getEncrypted());

	        attachment.setIv(
	                attachmentDTO.getIv());

	        // IMPORTANT
//	        attachment.setFileData(
//	                attachmentDTO.getFileData());

	        attachmentRepository.save(attachment);
	    }

	    // =========================
	    // BUILD RESPONSE
	    // =========================

	    GroupMessageResponse response =
	            new GroupMessageResponse();

	    response.setMessageId(
	            saved.getMessageId());

	    response.setGroupId(
	            saved.getGroupId());

	    response.setSenderId(
	            saved.getSenderId());

	    response.setMessageType(
	            saved.getMessageType());

	    response.setCreatedAt(
	            saved.getCreatedAt());

	    // =========================
	    // DECRYPT MESSAGE FOR LIVE
	    // =========================

	    try {

	        if (saved.getEncrypted() != null &&
	                saved.getEncrypted() == 1) {

	            String decrypted =
	                    encryptionService.decryptString(
	                            saved.getMessage(),
	                            saved.getIv());

	            response.setMessage(decrypted);

	        } else {

	            response.setMessage(
	                    saved.getMessage());
	        }

	    } catch (Exception e) {

	        response.setMessage(
	                saved.getMessage());
	    }

	    // =========================
	    // ATTACHMENT RESPONSE
	    // =========================

	    if (attachmentDTO != null) {
	        response.setAttachment(
	                attachmentDTO);
	    }

	    return response;
	}
}