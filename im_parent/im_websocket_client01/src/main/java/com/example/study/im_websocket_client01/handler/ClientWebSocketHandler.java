package com.example.study.im_websocket_client01.handler;

import com.example.study.im_websocket_client01.manager.WsSessionManager;
import com.example.study.im_websocket_client01.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * 修正构造器参数类型，明确接收 WsSessionManager
 */
public class ClientWebSocketHandler extends WebSocketClient {
    // 持有 WsSessionManager 引用（核心）
    private final WsSessionManager wsSessionManager;
    private final LogUtil logUtil;
    private final ObjectMapper objectMapper;

    // 修正构造器：第一个参数是URI，第二个是WsSessionManager（关键）
    public ClientWebSocketHandler(URI uri, WsSessionManager wsSessionManager, LogUtil logUtil, ObjectMapper objectMapper) {
        super(uri);
        this.wsSessionManager = wsSessionManager; // 正确赋值
        this.logUtil = logUtil;
        this.objectMapper = objectMapper;
    }

    // ========== 重写WS核心方法（示例） ==========
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logUtil.info("WS连接已打开，状态码：{}", handshakedata.getHttpStatus());
    }

    @Override
    public void onMessage(String message) {
        logUtil.info("收到服务端消息：{}", message);
        // 可通过wsSessionManager调用其他方法
        // wsSessionManager.sendMessageToDefaultServer("收到消息确认");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logUtil.info("WS连接关闭：code={}, reason={}", code, reason);
        // 触发重连逻辑
        if (wsSessionManager != null) {
            wsSessionManager.connectToRandomServer();
        }
    }

    @Override
    public void onError(Exception ex) {
        logUtil.error("WS连接异常：{}", ex.getMessage());
    }
}