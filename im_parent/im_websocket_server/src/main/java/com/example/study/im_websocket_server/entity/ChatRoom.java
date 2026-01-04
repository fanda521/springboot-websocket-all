package com.example.study.im_websocket_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom implements Serializable {
    private String roomId;        // 聊天室ID
    private String creator;       // 创建者ID
    private String roomName;      // 聊天室名称
    private Set<String> userIds;  // 房间内用户ID
    private Long createTime;      // 创建时间
    private Boolean isActive;     // 是否有效
}