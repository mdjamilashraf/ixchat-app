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
import com.ultimate.chat.repository.ChatGroupMemberRepository;
import com.ultimate.chat.repository.ChatMessageRepository;
import com.ultimate.chat.util.EncryptionService;

@Service
public class GroupHistoryService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatAttachmentRepository attachmentRepository;
	private final ChatGroupMemberRepository groupMemberRepository;
	private final EncryptionService encryptionService;

	public GroupHistoryService(ChatMessageRepository chatMessageRepository,
			ChatAttachmentRepository attachmentRepository, ChatGroupMemberRepository groupMemberRepository,
			EncryptionService encryptionService) {

		this.chatMessageRepository = chatMessageRepository;
		this.attachmentRepository = attachmentRepository;
		this.groupMemberRepository = groupMemberRepository;
		this.encryptionService = encryptionService;
	}

	public List<ChatHistoryResponse> getGroupChatHistory(String groupId, String userId, int page, int size) {

		// 1. Verify user is a group member
		boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);

		if (!isMember) {
			throw new IllegalStateException("User is not a member of this group");
		}

		// 2. Pagination
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

		// 3. Get group messages
		List<ChatMessage> messages = chatMessageRepository.findGroupChatHistory(groupId, pageable);

		// 4. Convert response
		return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
	}

	private ChatHistoryResponse convertToResponse(ChatMessage message) {

		ChatHistoryResponse response = new ChatHistoryResponse();

		response.setMessageId(message.getMessageId());

		response.setSenderId(message.getSenderId());

		response.setGroupId(message.getGroupId());

		response.setMessageType(message.getMessageType());

		response.setCreatedAt(message.getCreatedAt());

		// Decrypt message
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

		// Attachment metadata
		attachmentRepository.findByMessageId(message.getMessageId()).ifPresent(attachment -> {

			ChatAttachmentReq dto = new ChatAttachmentReq();

			dto.setFileId(String.valueOf(attachment.getFileId()));

			dto.setFileName(attachment.getFileName());

			dto.setFileType(attachment.getFileType());

			dto.setFileSize(attachment.getFileSize());

			dto.setDownloadUrl(attachment.getDownloadUrl());

			dto.setEncrypted(attachment.getEncrypted());

			dto.setIv(attachment.getIv());

			response.setAttachment(dto);
		});

		return response;
	}
}