package com.ultimate.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ultimate.chat.repository.ChatGroupRepository;

@Service
public class ChatGroupService {

	private final ChatGroupRepository chatGroupRepository;

	public ChatGroupService(ChatGroupRepository chatGroupRepository) {
		this.chatGroupRepository = chatGroupRepository;
	}

	public List<String> findAllGroupIds() {
		return chatGroupRepository.findAllGroupIds();
	}
	
	
}
