package com.jeffrey.websocketclient.controller;

import com.alibaba.fastjson.JSON;
import com.jeffrey.websocketclient.entity.P2pMessage;
import com.jeffrey.websocketclient.entity.PubSubMessage;
import com.jeffrey.websocketclient.service.p2p.StompP2pClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/1/3 16:47
 */
@RestController
@RequestMapping("/websocket/p2p")
public class P2pController {

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage() throws Exception {

        // 服务端WebSocket地址
        String serverUrl = "ws://localhost:9001/ws/p2p";

        // 1. 启动user1客户端（订阅消息 + 发送消息）
        StompP2pClient user1Client = new StompP2pClient("user1");
        user1Client.connect(serverUrl);

        // 2. 启动user2客户端（订阅消息 + 发送消息）
        StompP2pClient user2Client = new StompP2pClient("user2");
        user2Client.connect(serverUrl);

        // 3. 模拟点对点消息发送
        Thread.sleep(1000); // 等待连接稳定
        user1Client.sendP2pMessage("user2", "你好，user2！我是user1");
        user2Client.sendP2pMessage("user1", "你好，user1！我收到消息了");


        // 5. 断开连接
        user1Client.disconnect();
        user2Client.disconnect();
        return ResponseEntity.ok("执行成功");
    }
}
