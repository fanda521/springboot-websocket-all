package com.jeffrey.websocketserver.controller;

import com.alibaba.fastjson.JSON;
import com.jeffrey.websocketserver.entity.PubSubMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StompController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 客户端发送消息到 /app/chat，服务端处理
    @MessageMapping("/chat")
    @SendTo("/topic/chatroom") // 广播到所有订阅 /topic/chatroom 的客户端
    public PubSubMessage handleChat(String message) {
        System.out.println("server收到消息：" + message);
        PubSubMessage pubSubMessage = JSON.parseObject(message, PubSubMessage.class);
        return pubSubMessage; // 直接返回，自动广播
    }

    // 主动推送消息（点对点）
    public void sendToUser(String userId, String message) {
        // 推送给指定用户（需结合 Spring Security 认证）
        messagingTemplate.convertAndSendToUser(
            userId, "/queue/private", message
        );
    }
}