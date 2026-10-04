package com.ultimate.chat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "chat_group_member")
@lombok.Data
public class ChatGroupMember {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_grp_mem_id", nullable = false)
    private Integer chatGrpMemId;

    @Column(name = "group_id", nullable = false, length = 50)
    private String groupId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
