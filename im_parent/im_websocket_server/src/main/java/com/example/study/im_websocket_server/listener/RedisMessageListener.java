package com.example.study.im_websocket_server.listener;


import com.example.study.im_websocket_server.entity.ChatMessage;
import com.example.study.im_websocket_server.handler.ImWebSocketHandler;
import com.example.study.im_websocket_server.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisMessageListener implements MessageListener {

    private final ImWebSocketHandler webSocketHandler;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final LogUtil logUtil;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 反序列化消息
            byte[] body = message.getBody();
            ChatMessage chatMessage = objectMapper.readValue(body, ChatMessage.class);

            logUtil.info("接收Redis广播消息，messageId={}, roomId={}", 
                    chatMessage.getMessageId(), chatMessage.getRoomId());

            // 获取房间内所有用户
            String roomUserKey = "im:room:user:" + chatMessage.getRoomId();
            for (Object userId : redisTemplate.opsForSet().members(roomUserKey)) {
                // 发送消息给每个用户（当前实例）
                webSocketHandler.sendMessageToUser((String) userId, chatMessage);
            }
        } catch (Exception e) {
            logUtil.error("处理Redis消息失败，error={}", e.getMessage());
        }
    }
}