package com.ultimate.chat.repository;

import com.ultimate.chat.entity.ChatMessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRecipientRepository
        extends JpaRepository<ChatMessageRecipient, Integer> {

    List<ChatMessageRecipient> findByReceiverIdAndStatusOrderByRecipientIdAsc(
            String receiverId,
            String status
    );

    @Modifying
    @Query("""
        UPDATE ChatMessageRecipient r
           SET r.status = 'DELIVERED',
               r.deliveredAt = :deliveredAt
         WHERE r.messageId = :messageId
           AND r.receiverId = :receiverId
           AND r.status = 'SENT'
    """)
    int markAsDelivered(
            @Param("messageId") Integer messageId,
            @Param("receiverId") String receiverId,
            @Param("deliveredAt") LocalDateTime deliveredAt
    );
}
