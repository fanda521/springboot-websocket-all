package com.example.study.im_websocket_server.service;


import com.example.study.im_websocket_server.entity.ChatMessage;
import com.example.study.im_websocket_server.entity.ChatRoom;

public interface ChatRoomService {
    /**
     * 创建聊天室
     * @param creatorId 创建者ID
     * @param roomName 聊天室名称
     * @return 聊天室信息
     */
    ChatRoom createChatRoom(String creatorId, String roomName);

    /**
     * 加入聊天室
     * @param userId 用户ID
     * @param roomId 聊天室ID
     * @return 是否成功
     */
    boolean joinChatRoom(String userId, String roomId);

    /**
     * 退出聊天室
     * @param userId 用户ID
     * @param roomId 聊天室ID
     * @return 是否成功
     */
    boolean leaveChatRoom(String userId, String roomId);

    /**
     * 解散聊天室
     * @param creatorId 创建者ID
     * @param roomId 聊天室ID
     * @return 是否成功
     */
    boolean dissolveChatRoom(String creatorId, String roomId);

    /**
     * 发送消息到聊天室
     * @param message 消息内容
     * @return 是否成功
     */
    boolean sendMessage(ChatMessage message);

    /**
     * 自动退出聊天室（5分钟无发言）
     * @param userId 用户ID
     * @param roomId 聊天室ID
     */
    void autoLeaveChatRoom(String userId, String roomId);
}