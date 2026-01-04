package com.example.study.im_websocket_server.util;

import java.util.UUID;

public class IdGenerator {
    // 生成唯一ID
    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    // 生成聊天室ID
    public static String generateRoomId() {
        return "ROOM_" + System.currentTimeMillis() + "_" + generateId().substring(0, 8);
    }
    
    // 生成消息ID
    public static String generateMessageId() {
        return "MSG_" + System.currentTimeMillis() + "_" + generateId().substring(0, 8);
    }
}