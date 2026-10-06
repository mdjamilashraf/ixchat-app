package com.ultimate.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ultimate.chat.dto.ChatHistoryResponse;
import com.ultimate.chat.service.ChatHistoryService;
import com.ultimate.chat.service.GroupHistoryService;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

	private final ChatHistoryService chatHistoryService;
	private final GroupHistoryService groupHistoryService;

	public ChatHistoryController(ChatHistoryService chatHistoryService, GroupHistoryService groupHistoryService) {
		this.chatHistoryService = chatHistoryService;
		this.groupHistoryService = groupHistoryService;
	}

	@GetMapping("/history/{userId}/{receiverId}")
	public ResponseEntity<List<ChatHistoryResponse>> getPrivateChatHistory(@PathVariable String userId,
			@PathVariable String receiverId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size) {

		// Basic validation
		if (userId.equals(receiverId)) {
			return ResponseEntity.badRequest().build();
		}

		if (size > 100) {
			size = 100;
		}

		List<ChatHistoryResponse> history = chatHistoryService.getPrivateChatHistory(userId, receiverId, page, size);

		return ResponseEntity.ok(history);
	}

	@GetMapping("/group/{groupId}/history/{userId}")
	public ResponseEntity<List<ChatHistoryResponse>> getGroupHistory(@PathVariable String groupId,
			@PathVariable String userId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size) {

		return ResponseEntity.ok(groupHistoryService.getGroupChatHistory(groupId, userId, page, size));
	}
}