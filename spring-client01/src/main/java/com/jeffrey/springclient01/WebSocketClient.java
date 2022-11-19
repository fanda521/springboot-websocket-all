package com.jeffrey.springclient01;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@Component
@ClientEndpoint
public class WebSocketClient {

    @Value("${websocket.server.url:localhost:8080/testHandler.do}")
    private String serverUrl;

    @Value("${websocket.server.user:system@1}")
    private String user;

    private Session session;

    @PostConstruct
    void init() {
        try {
            // 本机地址
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String wsUrl = "ws://" + serverUrl + "?userId=" +user;
            URI uri = URI.create(wsUrl);
            session = container.connectToServer(WebSocketClient.class, uri);
        } catch (DeploymentException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开连接
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("打开");
        this.session = session;
    }

    /**
     * 接收消息
     * @param text
     */
    @OnMessage
    public void onMessage(String text) {
        System.out.println(text);

    }

    /**
     * 异常处理
     * @param throwable
     */
    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClosing() throws IOException {
        System.out.println("关闭");
        session.close();
    }

    /**
     * 主动发送消息
     */
    public void send(String message) {
        this.session.getAsyncRemote().sendText(message);
    }

    public void close() throws IOException{
        if(this.session.isOpen()){
            this.session.close();
        }
    }


}