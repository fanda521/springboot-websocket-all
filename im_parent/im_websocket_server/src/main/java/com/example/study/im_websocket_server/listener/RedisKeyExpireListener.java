package com.example.study.im_websocket_server.listener;


import com.example.study.im_websocket_server.service.ChatRoomService;
import com.example.study.im_websocket_server.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisKeyExpireListener implements MessageListener {

    private final ChatRoomService chatRoomService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LogUtil logUtil;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        logUtil.info("Redis键过期，key={}", expiredKey);

        // 匹配用户房间活跃键：im:user:room:active:{userId}:{roomId}
        if (expiredKey.startsWith("im:user:room:active:")) {
            String[] parts = expiredKey.split(":");
            if (parts.length == 6) {
                String userId = parts[4];
                String roomId = parts[5];
                // 执行自动退群逻辑
                chatRoomService.autoLeaveChatRoom(userId, roomId);
            }
        }
    }
}