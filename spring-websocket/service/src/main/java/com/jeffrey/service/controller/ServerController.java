package com.jeffrey.service.controller;

import com.jeffrey.service.handler.MyMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketHandler;

import javax.websocket.server.PathParam;

/**
 * @version 1.0
 * @Aythor allen
 * @date 2022/11/20 0:59
 * @description
 */
@RestController
public class ServerController {
    @Autowired
    private WebSocketHandler webSocketHandler;

    @GetMapping("/sendAllUser")
    public String sendAllUser(@PathParam("content") String content) {
        ((MyMessageHandler)webSocketHandler).sendMessageToAllUsers(content);
        return content;
    }
}
