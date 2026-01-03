package com.jeffrey.websocketclient03.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 与服务端一致的消息实体
 */
@Data
public class PubSubMessage {
    private String topic;
    private String content;
    private String sender;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    public PubSubMessage() {
        this.sendTime = new Date();
    }

    public PubSubMessage(String topic, String content, String sender) {
        this.topic = topic;
        this.content = content;
        this.sender = sender;
        this.sendTime = new Date();
    }
}