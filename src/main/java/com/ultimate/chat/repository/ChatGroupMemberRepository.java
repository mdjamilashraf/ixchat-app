package com.ultimate.chat.repository;

import com.ultimate.chat.entity.ChatGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatGroupMemberRepository
        extends JpaRepository<ChatGroupMember, Long> {

    boolean existsByGroupIdAndUserId(
            String groupId,
            String userId
    );

    List<ChatGroupMember> findByGroupId(
            String groupId
    );
}