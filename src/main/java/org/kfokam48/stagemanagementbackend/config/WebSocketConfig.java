package org.kfokam48.stagemanagementbackend.config;

import org.kfokam48.stagemanagementbackend.notification.CandidatureNotificationHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CandidatureNotificationHandler notificationHandler;

    public WebSocketConfig(CandidatureNotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationHandler, "/ws/candidature")
                .setAllowedOrigins("*"); // Autoriser toutes les connexions
    }
}

