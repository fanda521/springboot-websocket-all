package com.jeffrey.websocketclient.controller;

import com.alibaba.fastjson.JSON;
import com.jeffrey.websocketclient.config.stomp.StompSessionManager;
import com.jeffrey.websocketclient.entity.PubSubMessage;
import com.jeffrey.websocketclient.service.stomp.StompMessageSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/1/3 1:35
 */
@RestController
@RequestMapping("/websocket/stomp")
public class StompController {

    private final StompMessageSender messageSender;
    private final StompSessionManager sessionManager;

    public StompController(StompMessageSender messageSender,
                             StompSessionManager sessionManager) {
        this.messageSender = messageSender;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody PubSubMessage message) {
        if (!sessionManager.isConnected()) {
            try {
                sessionManager.connect();
            } catch (InterruptedException e) {
                return ResponseEntity.status(500).body("连接失败: " + e.getMessage());
            }
        }
        String s = JSON.toJSONString(message);
        boolean success = messageSender.sendTo("/app/chat", s);
        return success ?
                ResponseEntity.ok("消息发送成功") :
                ResponseEntity.status(500).body("消息发送失败");
    }
}
