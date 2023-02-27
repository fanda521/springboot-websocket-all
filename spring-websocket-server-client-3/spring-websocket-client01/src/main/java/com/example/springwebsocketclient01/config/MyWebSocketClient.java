package com.example.springwebsocketclient01.config;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

/**
 * @author jeffrey
 * @version 1.0
 * @date 2023/2/27
 * @time 15:23
 * @week 星期一
 * @description 客户端处理器
 **/
@Slf4j
public class MyWebSocketClient extends WebSocketClient {
    public MyWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public MyWebSocketClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    public MyWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("[websocket] 连接成功");
    }

    @Override
    public void onMessage(String message) {
        log.info("[websocket] 收到消息={}",message);

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("[websocket] 退出连接");
    }

    @Override
    public void onError(Exception ex) {
        log.info("[websocket] 连接错误={}",ex.getMessage());
    }
}
