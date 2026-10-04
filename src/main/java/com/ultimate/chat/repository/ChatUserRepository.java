package com.ultimate.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ultimate.chat.entity.ChatUser;

public interface ChatUserRepository extends JpaRepository<ChatUser, Integer> {
	ChatUser findByUserId(String userId);

	@Query("SELECT u.userId FROM ChatUser u")
	List<String> findAllUserIds();
}
