package com.jeffrey.websocketserver.controller;

import com.jeffrey.websocketserver.entity.P2pMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 消息处理核心控制器
 */
@Controller
public class P2pMessageController {

    /**
     * 用于发送点对点消息的核心工具类
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 接收客户端发送的点对点消息
     * 客户端发送地址：/app/p2p/send
     */
    @MessageMapping("/p2p/send")
    public void handleP2pMessage(P2pMessage message) {
        // 点对点消息推送：目标地址 /user/{toUser}/queue/message
        // 第一个参数：目标用户的订阅地址；第二个参数：消息体
        messagingTemplate.convertAndSendToUser(
                message.getToUser(), // 接收方用户标识
                "/queue/message",    // 接收方订阅的队列后缀
                message              // 发送的消息内容
        );
        System.out.println("服务端转发点对点消息：" + message.getFromUser() + " -> " + message.getToUser() + "：" + message.getContent());
    }
}