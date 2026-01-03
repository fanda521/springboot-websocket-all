package com.jeffrey.websocketclient.config.stomp;

import com.jeffrey.websocketclient.entity.PubSubMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class StompSessionManager {
    
    private volatile StompSession activeSession;
    private final WebSocketStompClient stompClient;
    private boolean isConnecting = false;

    private static final String TEST_TOPIC = "chatroom";
    
    public StompSessionManager(@Qualifier("webSocketStompClient") WebSocketStompClient stompClient) {
        this.stompClient = stompClient;
    }
    
    public synchronized void connect() throws InterruptedException {
        if (isConnecting || isConnected()) {
            return; // 避免重复连接
        }
        
        isConnecting = true;
        CountDownLatch connectLatch = new CountDownLatch(1);
        CustomStompSessionHandler sessionHandler = new CustomStompSessionHandler(connectLatch) {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                super.afterConnected(session, connectedHeaders);
                activeSession = session;
                isConnecting = false;
                System.out.println("STOMP 连接成功，Session ID: " + session.getSessionId());
                connectLatch.countDown();
            }
        };
        
        stompClient.connect("ws://localhost:9001/ws/stomp", sessionHandler);
        
        if (!connectLatch.await(10, TimeUnit.SECONDS)) {
            isConnecting = false;
            System.err.println("STOMP 连接超时");
        }

        // 4. 订阅主题：/topic/test（接收所有发布到该主题的消息）
        String subscribeTopic = "/topic/" + TEST_TOPIC;
        activeSession.subscribe(subscribeTopic, new StompFrameHandler() {
            // 指定消息类型为自定义实体
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return PubSubMessage.class;
            }

            // 接收消息回调（订阅的核心逻辑）
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                PubSubMessage message = (PubSubMessage) payload;
                System.out.println("\n=== 收到订阅消息 ===");
                System.out.println("主题：" + message.getTopic());
                System.out.println("发送者：" + message.getSender());
                System.out.println("内容：" + message.getContent());
                System.out.println("发送时间：" + message.getSendTime());
                System.out.println("===================\n");
            }
        });
        System.out.println("客户端已订阅主题：" + subscribeTopic);


    }
    
    public StompSession getActiveSession() {
        return activeSession;
    }
    
    public boolean isConnected() {
        return activeSession != null && activeSession.isConnected();
    }
    
    public void disconnect() {
        if (activeSession != null && activeSession.isConnected()) {
            activeSession.disconnect();
            activeSession = null;
        }
    }
}
