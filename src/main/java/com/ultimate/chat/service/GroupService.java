package com.ultimate.chat.service;

import com.ultimate.chat.entity.ChatGroupMember;
import com.ultimate.chat.repository.ChatGroupMemberRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {

	private final ChatGroupMemberRepository groupMemberRepository;

	public GroupService(ChatGroupMemberRepository groupMemberRepository) {
		this.groupMemberRepository = groupMemberRepository;
	}

	public boolean isMember(String groupId, String userId) {

		return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
	}

	public Set<String> getMembers(String groupId) {

		return groupMemberRepository.findByGroupId(groupId).stream().map(ChatGroupMember::getUserId)
				.collect(Collectors.toSet());
	}
}