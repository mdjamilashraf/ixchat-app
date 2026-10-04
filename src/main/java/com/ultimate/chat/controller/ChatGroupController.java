package com.ultimate.chat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ultimate.chat.service.ChatGroupService;

@RestController
@RequestMapping("/api/chat")
public class ChatGroupController {

	private final ChatGroupService chatGroupService;
	
	public ChatGroupController(ChatGroupService chatGroupService) {
		this.chatGroupService = chatGroupService;
	}
	
	@GetMapping("/groups")
    public List<String> getAllGroups() {
        return chatGroupService.findAllGroupIds();
    }
}
