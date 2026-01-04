package com.example.study.im_websocket_server.handler;


import com.example.study.im_websocket_server.entity.ChatMessage;
import com.example.study.im_websocket_server.util.UserSessionRedisUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 完整 WS 处理器：对齐原有 ChatMessage 传参规范，补全 sendMessageToUser
 */
public class ImWebSocketHandler extends TextWebSocketHandler {

    // 存储在线会话（用户ID -> 会话），线程安全
    private final Map<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();
    // 实例ID，用于日志区分
    private final String instanceId = "IM_WS_" + System.currentTimeMillis();

    private final UserSessionRedisUtil userSessionRedisUtil;
    // JSON 序列化工具（与原有逻辑一致）

    private final ObjectMapper objectMapper;

    public ImWebSocketHandler(UserSessionRedisUtil userSessionRedisUtil, ObjectMapper objectMapper) {
        this.userSessionRedisUtil = userSessionRedisUtil;
        this.objectMapper = objectMapper;
    }

    /**
     * 【核心】根据用户ID推送 ChatMessage 消息给指定客户端（对齐原有传参）
     * @param userId 目标用户ID
     * @param chatMessage 要发送的消息实体（与原有逻辑一致）
     * @return true-发送成功，false-发送失败（用户不在线/连接异常）
     */
    public boolean sendMessageToUser(String userId, ChatMessage chatMessage) {
        // 1. 校验基础参数
        if (userId == null || userId.isEmpty() || chatMessage == null) {
            System.out.println("[" + instanceId + "] ❌ 推送消息失败：参数为空（userId=" + userId + "）");
            return false;
        }

        // 2. 获取用户会话
        WebSocketSession session = onlineSessions.get(userId);
        if (session == null) {
            System.out.println("[" + instanceId + "] ❌ 推送消息失败：用户不在线（userId=" + userId + "）");
            return false;
        }

        // 3. 校验会话状态
        if (!session.isOpen()) {
            System.out.println("[" + instanceId + "] ❌ 推送消息失败：用户会话已关闭（userId=" + userId + "）");
            onlineSessions.remove(userId); // 清理无效会话
            return false;
        }

        // 4. 序列化 ChatMessage 为 JSON（与原有逻辑一致）
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(chatMessage);
        } catch (Exception e) {
            System.out.println("[" + instanceId + "] ❌ 推送消息失败：ChatMessage 序列化异常");
            System.out.println("异常信息：" + e.getMessage());
            return false;
        }

        // 5. 发送JSON格式消息
        try {
            TextMessage textMessage = new TextMessage(messageJson);
            session.sendMessage(textMessage);
            System.out.println("\n[" + instanceId + "] 📤 消息推送成功");
            System.out.println("目标用户ID：" + userId);
            System.out.println("消息类型：" + chatMessage.getType());
            System.out.println("消息内容：" + chatMessage.getContent());
            System.out.println("服务端实例ID：" + chatMessage.getServerInstanceId());
            return true;
        } catch (IOException e) {
            System.out.println("\n[" + instanceId + "] ❌ 推送消息失败：IO异常（userId=" + userId + "）");
            System.out.println("异常信息：" + e.getMessage());
            onlineSessions.remove(userId); // 清理异常会话
            return false;
        }
    }

    /**
     * 客户端连接成功时触发（对齐原有参数解析逻辑）
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 解析用户ID（从URL参数：ws://xxx/im/ws?userId=xxx，与原有逻辑一致）
        String query = session.getUri().getQuery();
        if (query == null || !query.contains("userId=")) {
            System.out.println("[" + instanceId + "] ❌ 连接失败：缺少 userId 参数");
            session.close(CloseStatus.BAD_DATA); // 关闭无效连接
            return;
        }

        String userId = query.split("=")[1];
        if (userId.isEmpty()) {
            System.out.println("[" + instanceId + "] ❌ 连接失败：userId 为空");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // 保存会话（覆盖旧会话，处理重连场景）
        onlineSessions.put(userId, session);
        // 2. 写入Redis（核心：解决多实例Session共享）
        userSessionRedisUtil.saveUserSession(userId, instanceId, session.getId());

        // 日志
        System.out.println("[实例:" + instanceId + "] 用户" + userId + "连接成功，已写入Redis");

        // 打印连接日志（与原有格式一致）
        System.out.println("\n[" + instanceId + "] 🟢 客户端连接成功");
        System.out.println("用户ID：" + userId);
        System.out.println("会话ID：" + session.getId());
        System.out.println("客户端IP：" + session.getRemoteAddress());
        System.out.println("当前在线数：" + onlineSessions.size());

        // 主动回复客户端（构造 ChatMessage 实体，与原有逻辑一致）
        ChatMessage connectMsg = new ChatMessage();
        connectMsg.setType(ChatMessage.MessageType.HEARTBEAT); // 对齐原有枚举
        connectMsg.setContent("连接成功！你的用户ID：" + userId);
        connectMsg.setServerInstanceId(instanceId);
        sendMessageToUser(userId, connectMsg);
    }

    /**
     * 接收客户端消息时触发（解析 ChatMessage 实体，与原有逻辑一致）
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 解析发送者ID
        String senderId = null;
        for (Map.Entry<String, WebSocketSession> entry : onlineSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                senderId = entry.getKey();
                break;
            }
        }

        if (senderId == null) {
            System.out.println("[" + instanceId + "] ❌ 接收消息失败：发送者会话不存在");
            return;
        }

        // 解析客户端发送的 ChatMessage JSON（与原有逻辑一致）
        String msgJson = message.getPayload();
        ChatMessage chatMessage;
        try {
            chatMessage = objectMapper.readValue(msgJson, ChatMessage.class);
        } catch (Exception e) {
            System.out.println("[" + instanceId + "] ❌ 接收消息失败：ChatMessage 反序列化异常");
            System.out.println("异常信息：" + e.getMessage());
            // 回复错误消息
            ChatMessage errorMsg = new ChatMessage();
            errorMsg.setType(ChatMessage.MessageType.HEARTBEAT);
            errorMsg.setContent("消息格式错误，请发送合法的 ChatMessage JSON");
            errorMsg.setServerInstanceId(instanceId);
            sendMessageToUser(senderId, errorMsg);
            return;
        }

        // 打印消息日志（对齐原有格式）
        System.out.println("\n[" + instanceId + "] 📩 收到客户端消息");
        System.out.println("发送者ID：" + senderId);
        System.out.println("消息类型：" + chatMessage.getType());
        System.out.println("消息内容：" + chatMessage.getContent());

        // 回复客户端（构造 ChatMessage 实体，与原有逻辑一致）
        ChatMessage responseMsg = new ChatMessage();
        responseMsg.setType(chatMessage.getType()); // 同类型回复
        responseMsg.setContent("服务端已收到：" + chatMessage.getContent());
        responseMsg.setServerInstanceId(instanceId);
        sendMessageToUser(senderId, responseMsg);
    }

    /**
     * 客户端断开连接时触发
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除会话
        String removeUserId = null;
        for (Map.Entry<String, WebSocketSession> entry : onlineSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                removeUserId = entry.getKey();
                break;
            }
        }

        if (removeUserId != null) {
            onlineSessions.remove(removeUserId);
            // 打印断开日志（与原有格式一致）
            System.out.println("\n[" + instanceId + "] 🔴 客户端连接断开");
            System.out.println("用户ID：" + removeUserId);
            System.out.println("断开状态码：" + status.getCode());
            System.out.println("断开原因：" + status.getReason());
            System.out.println("当前在线数：" + onlineSessions.size());
            // 2. 从Redis删除（核心）
            userSessionRedisUtil.deleteUserSession(removeUserId, instanceId);

            // 日志
            System.out.println("[实例:" + instanceId + "] 用户" + removeUserId + "断开连接，已从Redis删除Session");
        }
    }

    /**
     * 处理传输错误（避免处理器崩溃）
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("\n[" + instanceId + "] ❌ WS 传输错误");
        System.out.println("会话ID：" + session.getId());
        System.out.println("错误信息：" + exception.getMessage());

        // 清理异常会话
        String errorUserId = null;
        for (Map.Entry<String, WebSocketSession> entry : onlineSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                errorUserId = entry.getKey();
                break;
            }
        }
        if (errorUserId != null) {
            onlineSessions.remove(errorUserId);
            System.out.println("已清理异常会话（userId=" + errorUserId + "）");
            // 2. 从Redis删除（核心）
            userSessionRedisUtil.deleteUserSession(errorUserId, instanceId);

            // 日志
            System.out.println("[实例:" + instanceId + "] 用户" + errorUserId + "异常会话，已从Redis删除Session");
        }
    }

    /**
     * 是否支持部分消息（默认false，必须实现）
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // ========== 兼容原有扩展方法 ==========
    /**
     * 重载：支持字符串消息（兼容原有简单调用）
     */
    public boolean sendMessageToUser(String userId, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.SEND_MSG);
        chatMessage.setContent(content);
        chatMessage.setServerInstanceId(instanceId);
        return sendMessageToUser(userId, chatMessage);
    }

    /**
     * 广播消息给所有在线用户（ChatMessage 实体）
     */
    public void broadcastMessage(ChatMessage chatMessage) {
        for (String userId : onlineSessions.keySet()) {
            sendMessageToUser(userId, chatMessage);
        }
    }
}