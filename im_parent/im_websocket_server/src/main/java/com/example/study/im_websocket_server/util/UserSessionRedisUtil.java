package com.example.study.im_websocket_server.util;

import com.alibaba.fastjson.JSON;
import com.example.study.im_websocket_server.entity.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类：专门处理用户Session的存储/查询/删除
 */
public class UserSessionRedisUtil {
    // Redis Key前缀
    private static final String USER_SESSION_KEY_PREFIX = "im:user:session:"; // 用户ID -> 实例+Session信息
    private static final String ONLINE_USER_KEY = "im:online:users";          // 在线用户ID集合
    private static final String INSTANCE_USER_KEY_PREFIX = "im:instance:users:"; // 实例ID -> 用户ID列表

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper ;

    public UserSessionRedisUtil(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 存储用户Session信息到Redis
     * @param userId      用户ID
     * @param instanceId  服务端实例ID（如8080/8081）
     * @param sessionId   WebSocketSessionID
     */
    public void saveUserSession(String userId, String instanceId, String sessionId) {
        try {
            // 存储用户-Session映射（JSON格式）
            UserSession userSession = new UserSession();
            userSession.setUserId(userId);
            userSession.setSessionId(sessionId);
            userSession.setJoinRoomIds(new HashSet<>());
            userSession.setInstanceId(instanceId);
            userSession.setLastActiveTime(System.currentTimeMillis());
            String sessionJson = JSON.toJSONString(userSession);
            redisTemplate.opsForValue().set(USER_SESSION_KEY_PREFIX + userId, sessionJson, 24, TimeUnit.HOURS);

            // 添加到在线用户集合
            redisTemplate.opsForSet().add(ONLINE_USER_KEY, userId);

            // 添加到实例-用户映射
            redisTemplate.opsForSet().add(INSTANCE_USER_KEY_PREFIX + instanceId, userId);

        } catch (Exception e) {
            throw new RuntimeException("存储用户Session到Redis失败", e);
        }
    }

    /**
     * 查询用户的Session信息（实例ID+SessionID）
     */
    public UserSessionVO getUserSession(String userId) {
        String sessionJson = (String) redisTemplate.opsForValue().get(USER_SESSION_KEY_PREFIX + userId);
        if (sessionJson == null) {
            return null;
        }
        try {
            return objectMapper.readValue(sessionJson, UserSessionVO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析用户Session失败", e);
        }
    }

    /**
     * 删除用户Session（用户断开连接时）
     */
    public void deleteUserSession(String userId, String instanceId) {
        // 删除用户-Session映射
        redisTemplate.delete(USER_SESSION_KEY_PREFIX + userId);

        // 从在线用户集合移除
        redisTemplate.opsForSet().remove(ONLINE_USER_KEY, userId);

        // 从实例-用户映射移除
        redisTemplate.opsForSet().remove(INSTANCE_USER_KEY_PREFIX + instanceId, userId);
    }

    /**
     * 查询指定实例的在线用户列表
     */
    public Set<Object> getInstanceOnlineUsers(String instanceId) {
        return redisTemplate.opsForSet().members(INSTANCE_USER_KEY_PREFIX + instanceId);
    }

    /**
     * 查询所有在线用户ID
     */
    public Set<Object> getAllOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USER_KEY);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String userId) {
        return redisTemplate.opsForSet().isMember(ONLINE_USER_KEY, userId);
    }

    // Session信息VO（仅存储必要字段，不存储完整Session对象）
    public static class UserSessionVO {
        private String instanceId;  // 服务端实例ID（如localhost:8080）
        private String sessionId;   // WebSocketSessionID

        public UserSessionVO() {}
        public UserSessionVO(String instanceId, String sessionId) {
            this.instanceId = instanceId;
            this.sessionId = sessionId;
        }

        // Getter & Setter
        public String getInstanceId() { return instanceId; }
        public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}