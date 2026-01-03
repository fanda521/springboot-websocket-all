package com.jeffrey.websocketclient02.config.stomp;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;


class CustomStompSessionHandler extends StompSessionHandlerAdapter {
    private final CountDownLatch connectLatch;
    private volatile StompSession stompSession;

    public CustomStompSessionHandler(CountDownLatch connectLatch) {
        this.connectLatch = connectLatch;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.stompSession = session;
        System.out.println("STOMP 连接成功！Session ID: " + session.getSessionId());
        connectLatch.countDown();
    }

    public StompSession getStompSession() {
        return stompSession;
    }
    
    @Override
    public void handleException(StompSession session, StompCommand command,
                               StompHeaders headers, byte[] payload, Throwable exception) {
        System.err.println("STOMP 会话异常：" + exception.getMessage());
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        System.err.println("STOMP 传输错误：" + exception.getMessage());
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Object.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        // 处理连接确认等消息
    }
}
