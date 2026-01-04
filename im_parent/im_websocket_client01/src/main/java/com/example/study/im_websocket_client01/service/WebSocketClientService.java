package com.example.study.im_websocket_client01.service;


import com.example.study.im_websocket_client01.config.PropertiesConfig;
import com.example.study.im_websocket_client01.handler.ClientWebSocketHandler;
import com.example.study.im_websocket_client01.manager.WsSessionManager;
import com.example.study.im_websocket_client01.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.java_websocket.client.WebSocketClient;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class WebSocketClientService {

    private final PropertiesConfig propertiesConfig;
    private final FailoverService failoverService;
    private final LogUtil logUtil;
    private final ObjectMapper objectMapper;
    private final WsSessionManager wsSessionManager;

    private WebSocketClient webSocketClient;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private Timer heartbeatTimer;

    /**
     * 初始化WebSocket连接
     */
    public void initWebSocket() {
        String server = failoverService.getNextServer();
        connect(server);
    }

    /**
     * 连接指定服务端
     * @param server 服务端地址（如http://localhost:8080）
     */
    private void connect(String server) {
        try {
            // 核心修复：正确构建原生WebSocket URI
            // 1. 替换http为ws，https为wss
            String wsProtocol = server.startsWith("https") ? "wss" : "ws";
            String wsServer = wsProtocol + server.substring(server.indexOf("://"));
            // 2. 拼接路径和参数：ws://localhost:8080/im/ws?userId=test_user_001
            String wsUri = wsServer + propertiesConfig.getWebsocket().getPath() +
                    "?userId=" + propertiesConfig.getClient().getUserId();
            
            logUtil.info("尝试连接WebSocket服务端，URI={}", wsUri);
            URI uri = new URI(wsUri);

            // 创建WebSocket客户端
            webSocketClient = new ClientWebSocketHandler(uri, wsSessionManager, logUtil, objectMapper);
            
            // 清除旧连接（避免重连时状态异常）
            if (webSocketClient.isClosed()) {
                webSocketClient.reconnect();
            } else {
                webSocketClient.connect();
            }

            // 等待连接（最多等待timeout时间）
            int timeout = propertiesConfig.getServer().getTimeout();
            long start = System.currentTimeMillis();
            while (!isConnected.get() && System.currentTimeMillis() - start < timeout) {
                Thread.sleep(100);
            }

            if (isConnected.get()) {
                // 启动心跳
                startHeartbeat();
                failoverService.resetRetryCount();
                logUtil.info("WebSocket连接成功，服务端地址={}", server);
            } else {
                throw new RuntimeException("连接超时（" + timeout + "ms）");
            }
        } catch (URISyntaxException e) {
            logUtil.error("URI格式错误，server={}, error={}", server, e.getMessage());
            failover();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logUtil.error("连接等待被中断，error={}", e.getMessage());
            failover();
        } catch (Exception e) {
            logUtil.error("连接服务端失败，server={}, error={}", server, e.getMessage());
            failover();
        }
    }

    /**
     * 故障转移
     */
    private void failover() {
        if (!failoverService.isFailoverEnabled()) {
            logUtil.error("故障转移未启用，连接失败后退出");
            return;
        }

        // 检查重试次数
        boolean exceedMaxRetry = failoverService.incrementRetryCount();
        if (exceedMaxRetry) {
            logUtil.error("超过最大重试次数（{}），故障转移失败", propertiesConfig.getFailover().getMaxRetry());
            return;
        }

        // 延迟后重试
        try {
            Thread.sleep(propertiesConfig.getServer().getRetryInterval());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 连接下一个服务端
        String nextServer = failoverService.getNextServer();
        connect(nextServer);
    }

    /**
     * 启动心跳
     */
    private void startHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
        }

        heartbeatTimer = new Timer();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isConnected.get() && webSocketClient != null && webSocketClient.isOpen()) {
                    // 发送心跳消息（兼容服务端消息类型）
                    String heartbeatMsg = "{\"type\":\"HEARTBEAT\",\"senderId\":\"" + 
                            propertiesConfig.getClient().getUserId() + "\",\"content\":\"ping\"}";
                    webSocketClient.send(heartbeatMsg);
                    logUtil.info("发送心跳消息：{}", heartbeatMsg);
                } else {
                    logUtil.warn("连接已断开，停止心跳");
                    heartbeatTimer.cancel();
                    isConnected.set(false);
                    // 触发故障转移
                    failover();
                }
            }
        }, 0, propertiesConfig.getWebsocket().getHeartbeatInterval());
    }

    /**
     * 发送消息
     * @param message 消息内容（JSON字符串）
     */
    public void sendMessage(String message) {
        if (isConnected.get() && webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
            logUtil.info("发送消息：{}", message);
        } else {
            logUtil.error("连接未建立，无法发送消息");
            // 触发重连
            failover();
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (webSocketClient != null) {
            webSocketClient.close(1000, "客户端主动关闭");
            isConnected.set(false);
            if (heartbeatTimer != null) {
                heartbeatTimer.cancel();
            }
            logUtil.info("关闭WebSocket连接");
        }
    }

    /**
     * 设置连接状态
     * @param connected 连接状态
     */
    public void setConnected(boolean connected) {
        isConnected.set(connected);
    }

    /**
     * 获取连接状态
     * @return 连接状态
     */
    public boolean isConnected() {
        return isConnected.get();
    }
}