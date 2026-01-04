package com.example.study.im_websocket_server.config;

import com.example.study.im_websocket_server.handler.ImWebSocketHandler;
import com.example.study.im_websocket_server.util.UserSessionRedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 核心：强制注册 WS 处理器，暴露 /im/ws 端口
 */
@Configuration
@EnableWebSocket // 启用原生 WebSocket，不依赖 SockJS
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper ;

    /**
     * 手动创建 Handler Bean，避免扫描失败
     */
    @Bean
    public ImWebSocketHandler imWebSocketHandler() {
        return new ImWebSocketHandler(userSessionRedisUtil(), objectMapper);
    }

    @Bean
    public UserSessionRedisUtil userSessionRedisUtil() {
    	return new UserSessionRedisUtil(redisTemplate, objectMapper);
    }

    /**
     * 注册 WS 处理器，明确暴露路径
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 核心：注册处理器 + 暴露路径 + 允许所有跨域（测试环境）
        registry.addHandler(imWebSocketHandler(), "/im/ws")
                .setAllowedOrigins("*"); // 必须加，否则客户端跨域连接失败

        // 打印强制日志，确认注册成功
        System.out.println("[WS 配置] ✅ WebSocket 处理器注册成功，暴露路径：/im/ws");
        System.out.println("[WS 配置] ✅ 允许所有跨域请求，测试环境专用");
    }



}