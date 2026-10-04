package com.ultimate.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_group")
@lombok.Data
public class ChatGroup {

	@Id
	@Column(name = "group_id", nullable = false, unique = true, length = 50)
    private String groupId;

    @Column(name = "group_name", nullable = false, length = 150)
    private String groupName;

    @Column(length = 500)
    private String description;
}
