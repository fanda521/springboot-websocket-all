package com.jeffrey.websocketclient.service;
 
/**
 * @Auther: liaoshiyao
 * @Date: 2019/1/12 10:57
 * @Description: websocket 接口
 */
public interface WebSocketService {
 
    /**
     * 群发
     * @param message
     */
     void groupSending(String message);
 
    /**
     * 指定发送
     * @param name
     * @param message
     */
     void appointSending(String name,String message);
}