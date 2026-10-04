package com.ultimate.chat.repository;

import com.ultimate.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Integer> {
}
