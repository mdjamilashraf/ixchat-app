package com.ultimate.chat.config;

import com.ultimate.chat.service.PendingMessageService;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Component
public class WebSocketEventListener {

    private final PendingMessageService pendingMessageService;

    public WebSocketEventListener(
            PendingMessageService pendingMessageService) {

        this.pendingMessageService = pendingMessageService;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        // Get userId sent by client during STOMP CONNECT
        String userId =
                accessor.getFirstNativeHeader("userId");

        if (userId == null || userId.isBlank()) {
            return;
        }

        System.out.println(
                "WebSocket connected: " + userId
        );

        // THIS IS THE CALL
        pendingMessageService.deliverPendingMessages(userId);
    }
}