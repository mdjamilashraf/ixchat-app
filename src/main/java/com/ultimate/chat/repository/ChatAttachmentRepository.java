package com.ultimate.chat.repository;

import com.ultimate.chat.entity.ChatAttachment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatAttachmentRepository
        extends JpaRepository<ChatAttachment, Integer> {
	
	Optional<ChatAttachment> findByMessageId(Integer messageId);
	Optional<ChatAttachment> findByStoragePath(String storagePath);
}
