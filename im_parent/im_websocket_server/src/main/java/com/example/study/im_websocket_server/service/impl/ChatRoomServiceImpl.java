package com.example.study.im_websocket_server.service.impl;


import com.example.study.im_websocket_server.config.PropertiesConfig;
import com.example.study.im_websocket_server.entity.ChatMessage;
import com.example.study.im_websocket_server.entity.ChatRoom;
import com.example.study.im_websocket_server.service.ChatRoomService;
import com.example.study.im_websocket_server.service.SessionService;
import com.example.study.im_websocket_server.util.IdGenerator;
import com.example.study.im_websocket_server.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionService sessionService;
    private final PropertiesConfig propertiesConfig;
    private final LogUtil logUtil;

    // 分布式锁脚本
    private static final String LOCK_SCRIPT = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then return redis.call('expire', KEYS[1], ARGV[2]) else return 0 end";
    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Override
    public ChatRoom createChatRoom(String creatorId, String roomName) {
        String roomId = IdGenerator.generateRoomId();
        String lockKey = "im:lock:room:" + roomId;
        String lockValue = IdGenerator.generateId();
        Long lockExpire = 30L; // 锁过期时间30秒

        try {
            // 获取分布式锁
            RedisScript<Long> lockRedisScript = new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);
            Long lockResult = redisTemplate.execute(lockRedisScript, Collections.singletonList(lockKey), lockValue, lockExpire);

            if (lockResult == 1) {
                // 创建聊天室
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setRoomId(roomId);
                chatRoom.setCreator(creatorId);
                chatRoom.setRoomName(roomName);
                chatRoom.setUserIds(new HashSet<>(Collections.singletonList(creatorId)));
                chatRoom.setCreateTime(System.currentTimeMillis());
                chatRoom.setIsActive(true);

                // 保存聊天室信息
                String roomKey = propertiesConfig.getChatRoom().getPrefix() + roomId;
                redisTemplate.opsForValue().set(roomKey, chatRoom);

                // 房间-用户映射
                String roomUserKey = propertiesConfig.getUserSession().getRoomUserPrefix() + roomId;
                redisTemplate.opsForSet().add(roomUserKey, creatorId);

                // 创建者加入房间
                sessionService.addUserToRoom(creatorId, roomId);

                logUtil.info("创建聊天室成功，roomId={}, creatorId={}", roomId, creatorId);
                return chatRoom;
            } else {
                logUtil.error("创建聊天室失败，获取分布式锁失败，roomId={}", roomId);
                return null;
            }
        } finally {
            // 释放分布式锁
            RedisScript<Long> unlockRedisScript = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
            redisTemplate.execute(unlockRedisScript, Collections.singletonList(lockKey), lockValue);
        }
    }

    @Override
    public boolean joinChatRoom(String userId, String roomId) {
        ChatRoom chatRoom = null;
        String roomKey = propertiesConfig.getChatRoom().getPrefix() + roomId;
        Object o = redisTemplate.opsForValue().get(roomKey);
        if (o != null && o instanceof ChatRoom)
            chatRoom = (ChatRoom) o;
        if (chatRoom == null || !chatRoom.getIsActive()) {
            logUtil.error("加入聊天室失败，聊天室不存在或已解散，roomId={}", roomId);
            return false;
        }

        // 添加用户到房间
        String roomUserKey = propertiesConfig.getUserSession().getRoomUserPrefix() + roomId;
        redisTemplate.opsForSet().add(roomUserKey, userId);

        // 更新聊天室用户列表
        chatRoom.getUserIds().add(userId);
        redisTemplate.opsForValue().set(roomKey, chatRoom);

        // 更新用户会话
        sessionService.addUserToRoom(userId, roomId);

        // 设置用户房间活跃过期时间
        String userRoomActiveKey = "im:user:room:active:" + userId + ":" + roomId;
        redisTemplate.opsForValue().set(userRoomActiveKey, System.currentTimeMillis(),
                propertiesConfig.getChatRoom().getExpireTime(), TimeUnit.SECONDS);

        logUtil.info("用户加入聊天室成功，userId={}, roomId={}", userId, roomId);
        return true;
    }

    @Override
    public boolean leaveChatRoom(String userId, String roomId) {
        String roomKey = propertiesConfig.getChatRoom().getPrefix() + roomId;
        ChatRoom chatRoom = (ChatRoom) redisTemplate.opsForValue().get(roomKey);

        if (chatRoom == null) {
            logUtil.error("退出聊天室失败，聊天室不存在，roomId={}", roomId);
            return false;
        }

        // 移除房间内用户
        String roomUserKey = propertiesConfig.getUserSession().getRoomUserPrefix() + roomId;
        redisTemplate.opsForSet().remove(roomUserKey, userId);

        // 更新聊天室用户列表
        chatRoom.getUserIds().remove(userId);
        redisTemplate.opsForValue().set(roomKey, chatRoom);

        // 更新用户会话
        sessionService.removeUserFromRoom(userId, roomId);

        // 删除用户房间活跃键
        String userRoomActiveKey = "im:user:room:active:" + userId + ":" + roomId;
        redisTemplate.delete(userRoomActiveKey);

        logUtil.info("用户退出聊天室成功，userId={}, roomId={}", userId, roomId);
        return true;
    }

    @Override
    public boolean dissolveChatRoom(String creatorId, String roomId) {
        String roomKey = propertiesConfig.getChatRoom().getPrefix() + roomId;
        ChatRoom chatRoom = null;

        Object o = redisTemplate.opsForValue().get(roomKey);
        if (o != null && o instanceof ChatRoom)
            chatRoom = (ChatRoom) o;

        if (chatRoom == null || !chatRoom.getCreator().equals(creatorId)) {
            logUtil.error("解散聊天室失败，聊天室不存在或非创建者操作，roomId={}, creatorId={}", roomId, creatorId);
            return false;
        }

        // 标记聊天室为无效
        chatRoom.setIsActive(false);
        redisTemplate.opsForValue().set(roomKey, chatRoom);

        // 获取房间内所有用户
        String roomUserKey = propertiesConfig.getUserSession().getRoomUserPrefix() + roomId;
        Set<Object> userIds = redisTemplate.opsForSet().members(roomUserKey);

        // 所有用户退出房间
        if (!CollectionUtils.isEmpty(userIds)) {
            for (Object userId : userIds) {
                sessionService.removeUserFromRoom((String) userId, roomId);
                // 删除用户房间活跃键
                String userRoomActiveKey = "im:user:room:active:" + userId + ":" + roomId;
                redisTemplate.delete(userRoomActiveKey);
            }
        }

        // 删除房间-用户映射
        redisTemplate.delete(roomUserKey);

        logUtil.info("解散聊天室成功，roomId={}, creatorId={}", roomId, creatorId);
        return true;
    }

    @Override
    public boolean sendMessage(ChatMessage message) {
        String roomId = message.getRoomId();
        String userId = message.getSenderId();

        // 校验房间是否存在
        String roomKey = propertiesConfig.getChatRoom().getPrefix() + roomId;
        ChatRoom chatRoom = null;
        Object o = redisTemplate.opsForValue().get(roomKey);
        if (o != null && o instanceof ChatRoom)
            chatRoom = (ChatRoom) o;
        if (chatRoom == null || !chatRoom.getIsActive()) {
            logUtil.error("发送消息失败，聊天室不存在或已解散，roomId={}", roomId);
            return false;
        }

        // 校验用户是否在房间内
        String roomUserKey = propertiesConfig.getUserSession().getRoomUserPrefix() + roomId;
        boolean isInRoom = redisTemplate.opsForSet().isMember(roomUserKey, userId);
        if (!isInRoom) {
            logUtil.error("发送消息失败，用户不在房间内，userId={}, roomId={}", userId, roomId);
            return false;
        }

        // 设置消息基本信息
        message.setMessageId(IdGenerator.generateMessageId());
        message.setSendTime(System.currentTimeMillis());
        message.setServerInstanceId(logUtil.getInstanceId());

        // 更新用户房间活跃时间
        String userRoomActiveKey = "im:user:room:active:" + userId + ":" + roomId;
        redisTemplate.opsForValue().set(userRoomActiveKey, System.currentTimeMillis(),
                propertiesConfig.getChatRoom().getExpireTime(), TimeUnit.SECONDS);

        // 发布消息到Redis通道（跨实例广播）
        redisTemplate.convertAndSend(propertiesConfig.getRedis().getChannel(), message);

        logUtil.info("发送消息成功，messageId={}, roomId={}, senderId={}", 
                message.getMessageId(), roomId, userId);
        return true;
    }

    @Override
    public void autoLeaveChatRoom(String userId, String roomId) {
        logUtil.warn("用户5分钟无发言，自动退出聊天室，userId={}, roomId={}", userId, roomId);
        // 执行退出逻辑
        leaveChatRoom(userId, roomId);

        // 发送自动退群消息
        ChatMessage autoLeaveMsg = new ChatMessage();
        autoLeaveMsg.setRoomId(roomId);
        autoLeaveMsg.setSenderId("system");
        autoLeaveMsg.setContent("用户" + userId + "因5分钟未发言，已自动退出聊天室");
        autoLeaveMsg.setType(ChatMessage.MessageType.AUTO_LEAVE);
        autoLeaveMsg.setMessageId(IdGenerator.generateMessageId());
        autoLeaveMsg.setSendTime(System.currentTimeMillis());
        autoLeaveMsg.setServerInstanceId(logUtil.getInstanceId());

        redisTemplate.convertAndSend(propertiesConfig.getRedis().getChannel(), autoLeaveMsg);
    }
}