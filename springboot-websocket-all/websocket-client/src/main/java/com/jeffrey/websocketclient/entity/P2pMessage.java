package com.jeffrey.websocketclient.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点对点消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class P2pMessage {
    private String toUser; // 目标用户（如user2）
    private String fromUser; // 发送方用户（如user1）
    private String content; // 消息内容
}