package com.example.study.im_websocket_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSession implements Serializable {
    private String userId;        // 用户ID
    private String sessionId;     // WebSocket会话ID
    private String instanceId; // 连接的服务实例ID
    private Set<String> joinRoomIds; // 加入的聊天室ID
    private Long lastActiveTime;  // 最后活跃时间
}