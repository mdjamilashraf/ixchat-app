package com.ultimate.chat.repository;

import com.ultimate.chat.entity.ChatMessage;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

	/*
	 * @Query(value = """ SELECT cm.* FROM chat_message cm INNER JOIN
	 * chat_message_recipient r ON r.message_id = cm.message_id WHERE cm.group_id IS
	 * NULL AND ( (cm.sender_id = :userId AND r.receiver_id = :receiverId) OR
	 * (cm.sender_id = :receiverId AND r.receiver_id = :userId) ) ORDER BY
	 * cm.created_at DESC """, nativeQuery = true)
	 */
	@Query("""
			SELECT cm
			FROM ChatMessage cm
			INNER JOIN ChatMessageRecipient r
			    ON r.messageId = cm.messageId
			WHERE cm.groupId IS NULL
			  AND (
			        (cm.senderId = :userId AND r.receiverId = :receiverId)
			     OR (cm.senderId = :receiverId AND r.receiverId = :userId)
			  )
			ORDER BY cm.createdAt DESC
			""")
	List<ChatMessage> findPrivateChatHistory(@Param("userId") String userId, @Param("receiverId") String receiverId,
			Pageable pageable);

	@Query("""
			SELECT cm
			FROM ChatMessage cm
			WHERE cm.groupId = :groupId
			ORDER BY cm.createdAt DESC
			""")
	List<ChatMessage> findGroupChatHistory(@Param("groupId") String groupId, Pageable pageable);
}
