package com.ultimate.chat.controller;

import com.ultimate.chat.dto.ChatDeliveredReq;
import com.ultimate.chat.dto.ChatMessageReq;
import com.ultimate.chat.dto.ChatReadyReq;
import com.ultimate.chat.dto.GroupChatMessage;
import com.ultimate.chat.dto.GroupMessageResponse;
import com.ultimate.chat.entity.ChatMessage;
import com.ultimate.chat.repository.ChatMessageRepository;
import com.ultimate.chat.service.ChatMessageService;
import com.ultimate.chat.service.GroupMessageService;
import com.ultimate.chat.service.GroupService;
import com.ultimate.chat.service.PendingMessageService;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final GroupService groupService;
	private final ChatMessageService chatMessageService;
	private final PendingMessageService pendingMessageService;
	private final GroupMessageService groupMessageService;

	public ChatController(SimpMessagingTemplate messagingTemplate, GroupService groupService,
			ChatMessageService chatMessageService, PendingMessageService pendingMessageService,
			GroupMessageService groupMessageService) {
		this.messagingTemplate = messagingTemplate;
		this.groupService = groupService;
		this.chatMessageService = chatMessageService;
		this.pendingMessageService = pendingMessageService;
		this.groupMessageService = groupMessageService;
	}

	@MessageMapping("/chat.send")
	public void sendMessage(ChatMessageReq message) {

		System.out.println("From:: " + message.getSenderId() + " To:: " + message.getReceiverId() + " Message:: "
				+ message.getMessage());
		
		ChatMessage savedMsg =  chatMessageService.saveMessage(message);
		message.setMessageId(savedMsg.getMessageId());
		messagingTemplate.convertAndSend("/Ix-topic/user/" + message.getReceiverId(), message);
		

	}

	@MessageMapping("/group.send")
	public void sendGroupMessage(GroupChatMessage message) {
		
		System.out.println("Group: " + message.getGroupId() + " From: " + message.getSenderId() + " Message: "
				+ message.getMessage());


		// Check group membership
		if (!groupService.isMember(message.getGroupId(), message.getSenderId())) {
			System.out.println("User is not a member of group");
			return;
		}
		
		GroupMessageResponse savedGrpMsg =
				groupMessageService.saveGrpMessage(message);

		System.out.println("Group: " + message.getGroupId() + " From: " + message.getSenderId() + " Message: "
				+ message.getMessage());

		// Broadcast to group topic (for clients subscribed by group)
		messagingTemplate.convertAndSend("/Ix-topic/group/" + message.getGroupId(), savedGrpMsg);

		// Also send per-user so clients subscribed to user-specific topic receive it
		groupService.getMembers(message.getGroupId()).forEach(memberId -> {
			if (!memberId.equals(message.getSenderId())) {
				messagingTemplate.convertAndSend("/Ix-topic/user/" + memberId, savedGrpMsg);
			}
		});
	}
	
	@MessageMapping("/chat.ready")
	public void chatReady(ChatReadyReq request) {

	    System.out.println(
	            "Chat ready received for user: "
	                    + request.getUserId()
	    );

	    pendingMessageService.deliverPendingMessages(
	            request.getUserId()
	    );
	}
	
	@MessageMapping("/chat.delivered")
	public void messageDelivered(
	        ChatDeliveredReq request) {

	    System.out.println(
	            "DELIVERED ACK: messageId="
	                    + request.getMessageId()
	                    + ", receiver="
	                    + request.getReceiverId()
	    );

	    chatMessageService.markAsDelivered(
	            request.getMessageId(),
	            request.getReceiverId()
	    );
	}
}