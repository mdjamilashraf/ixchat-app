package com.ultimate.chat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ultimate.chat.service.ChatUserService;

@RestController
@RequestMapping("/api/chat")
public class ChatUserController {

    private final ChatUserService chatUserService;

    public ChatUserController(ChatUserService chatUserService) {
        this.chatUserService = chatUserService;
    }

    @GetMapping("/users")
    public List<String> getAllUsers() {
        return chatUserService.getAllUserIds();
    }
}
