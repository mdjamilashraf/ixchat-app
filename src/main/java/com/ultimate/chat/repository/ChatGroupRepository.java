package com.ultimate.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ultimate.chat.entity.ChatGroup;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, String> {

	@Query("SELECT g.groupId FROM ChatGroup g")
	List<String> findAllGroupIds();
}
