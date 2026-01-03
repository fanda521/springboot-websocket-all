package com.jeffrey.websocketserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 启用消息代理
public class StompConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用内置消息代理（生产环境建议用 RabbitMQ/ActiveMQ）
        config.enableSimpleBroker("/topic", "/queue"); // 订阅前缀（广播/点对点）
        config.setApplicationDestinationPrefixes("/app"); // 业务请求前缀
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 暴露 STOMP 端点，允许跨域
        registry.addEndpoint("/ws/stomp").setAllowedOrigins("*").withSockJS();
    }
}