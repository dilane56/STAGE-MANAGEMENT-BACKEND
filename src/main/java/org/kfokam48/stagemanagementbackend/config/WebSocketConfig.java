package org.kfokam48.stagemanagementbackend.config;

import org.kfokam48.stagemanagementbackend.chat.ChatMessageHandler;
import org.kfokam48.stagemanagementbackend.notification.CandidatureNotificationHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CandidatureNotificationHandler notificationHandler;
    private final ChatMessageHandler chatMessageHandler; // ✅ Ajout du gestionnaire de chat

    public WebSocketConfig(CandidatureNotificationHandler notificationHandler, ChatMessageHandler chatMessageHandler) {
        this.notificationHandler = notificationHandler;
        this.chatMessageHandler = chatMessageHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationHandler, "/ws/candidature")
                .setAllowedOrigins("*"); // 🔔 Notifications candidature

        registry.addHandler(chatMessageHandler, "/ws/chat")
                .setAllowedOrigins("*"); // 💬 Messagerie instantanée
    }


}


