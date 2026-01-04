package com.example.study.im_websocket_server.service.impl;


import com.alibaba.fastjson.JSON;
import com.example.study.im_websocket_server.config.PropertiesConfig;
import com.example.study.im_websocket_server.entity.UserSession;
import com.example.study.im_websocket_server.service.SessionService;
import com.example.study.im_websocket_server.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PropertiesConfig propertiesConfig;
    private final LogUtil logUtil;

    @Override
    public void saveUserSession(String userId, String sessionId) {
        String sessionKey = propertiesConfig.getUserSession().getPrefix() + userId;
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setSessionId(sessionId);
        session.setInstanceId(logUtil.getInstanceId());
        session.setJoinRoomIds(new HashSet<>());
        session.setLastActiveTime(System.currentTimeMillis());

        redisTemplate.opsForValue().set(sessionKey, session);
        logUtil.info("保存用户会话成功，userId={}, sessionId={}", userId, sessionId);
    }

    @Override
    public UserSession getUserSession(String userId) {
        String sessionKey = propertiesConfig.getUserSession().getPrefix() + userId;
        Object o = redisTemplate.opsForValue().get(sessionKey);
        if (o != null && (o instanceof UserSession || o instanceof String)) {
            if (o instanceof String) {
                UserSession userSession = JSON.parseObject((String) o, UserSession.class);
                return userSession;
            } else {
                return (UserSession) o;
            }
        }
        return null;
    }

    @Override
    public void addUserToRoom(String userId, String roomId) {
        String sessionKey = propertiesConfig.getUserSession().getPrefix() + userId;
        UserSession session = getUserSession(userId);

        if (session == null) {
            logUtil.error("添加用户到房间失败，用户会话不存在，userId={}", userId);
            return;
        }
        if (session.getJoinRoomIds() == null)
            session.setJoinRoomIds(new HashSet<>());
        session.getJoinRoomIds().add(roomId);
        session.setLastActiveTime(System.currentTimeMillis());
        redisTemplate.opsForValue().set(sessionKey, session);

        logUtil.info("添加用户到房间成功，userId={}, roomId={}", userId, roomId);
    }

    @Override
    public void removeUserFromRoom(String userId, String roomId) {
        String sessionKey = propertiesConfig.getUserSession().getPrefix() + userId;
        UserSession session = getUserSession(userId);

        if (session == null) {
            logUtil.error("从房间移除用户失败，用户会话不存在，userId={}", userId);
            return;
        }

        session.getJoinRoomIds().remove(roomId);
        session.setLastActiveTime(System.currentTimeMillis());
        redisTemplate.opsForValue().set(sessionKey, session);

        logUtil.info("从房间移除用户成功，userId={}, roomId={}", userId, roomId);
    }

    @Override
    public void removeUserSession(String userId) {
        String sessionKey = propertiesConfig.getUserSession().getPrefix() + userId;
        redisTemplate.delete(sessionKey);
        logUtil.info("移除用户会话成功，userId={}", userId);
    }
}