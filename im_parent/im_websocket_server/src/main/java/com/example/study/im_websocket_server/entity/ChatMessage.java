package com.example.study.im_websocket_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage implements Serializable {
    public enum MessageType {
        CREATE_ROOM, JOIN_ROOM, SEND_MSG, LEAVE_ROOM, DISSOLVE_ROOM, AUTO_LEAVE,HEARTBEAT
    }

    private String messageId;     // 消息ID
    private String roomId;        // 聊天室ID
    private String senderId;      // 发送者ID
    private String content;       // 消息内容
    private MessageType type;     // 消息类型
    private Long sendTime;        // 发送时间
    private String serverInstanceId; // 处理实例ID
}