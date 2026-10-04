package com.ultimate.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ultimate.chat.repository.ChatUserRepository;

@Service
public class ChatUserService {

    private final ChatUserRepository chatUserRepository;

    public ChatUserService(ChatUserRepository chatUserRepository) {
        this.chatUserRepository = chatUserRepository;
    }

    public List<String> getAllUserIds() {
        return chatUserRepository.findAllUserIds();
    }
}
