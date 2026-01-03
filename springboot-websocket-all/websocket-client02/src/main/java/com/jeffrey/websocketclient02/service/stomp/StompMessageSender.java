package com.jeffrey.websocketclient02.service.stomp;

import com.jeffrey.websocketclient02.config.stomp.StompSessionManager;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Service;

@Service
public class StompMessageSender {
    
    private final StompSessionManager sessionManager;
    
    public StompMessageSender(StompSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    
    public boolean sendTo(String destination, Object message) {
        if (!sessionManager.isConnected()) {
            System.err.println("STOMP 会话未连接，无法发送消息");
            return false;
        }
        
        try {
            StompSession session = sessionManager.getActiveSession();
            StompHeaders headers = new StompHeaders();
            headers.setDestination(destination);
            session.send(headers, message);
            return true;
        } catch (Exception e) {
            System.err.println("发送消息失败: " + e.getMessage());
            return false;
        }
    }
    
    public boolean subscribe(String destination, StompFrameHandler handler) {
        if (!sessionManager.isConnected()) {
            System.err.println("STOMP 会话未连接，无法订阅");
            return false;
        }
        
        try {
            StompSession session = sessionManager.getActiveSession();
            session.subscribe(destination, handler);
            return true;
        } catch (Exception e) {
            System.err.println("订阅失败: " + e.getMessage());
            return false;
        }
    }
}
