package com.example.study.im_websocket_server.controller;


import com.example.study.im_websocket_server.entity.ChatMessage;
import com.example.study.im_websocket_server.entity.ChatRoom;
import com.example.study.im_websocket_server.service.ChatRoomService;
import com.example.study.im_websocket_server.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatroom")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final LogUtil logUtil;

    /**
     * 创建聊天室
     * @param creatorId 创建者ID
     * @param roomName 聊天室名称
     * @return 聊天室信息
     */
    @PostMapping("/create")
    public ChatRoom createChatRoom(
            @RequestParam String creatorId,
            @RequestParam String roomName) {
        logUtil.info("调用创建聊天室接口，creatorId={}, roomName={}", creatorId, roomName);
        return chatRoomService.createChatRoom(creatorId, roomName);
    }

    /**
     * 加入聊天室
     * @param userId 加入者ID
     * @param roomId 聊天室id
     * @return 聊天室信息
     */
    @PostMapping("/join")
    public Boolean joinChatRoom(
            @RequestParam String userId,
            @RequestParam String roomId) {
        logUtil.info("调用加入聊天室接口，userId={}, roomId={}", userId, roomId);
        return chatRoomService.joinChatRoom(userId, roomId);
    }

    /**
     * 解散聊天室
     * @param creatorId 创建者ID
     * @param roomId 聊天室ID
     * @return 是否成功
     */
    @PostMapping("/dissolve")
    public Boolean dissolveChatRoom(
            @RequestParam String creatorId,
            @RequestParam String roomId) {
        logUtil.info("调用解散聊天室接口，creatorId={}, roomId={}", creatorId, roomId);
        return chatRoomService.dissolveChatRoom(creatorId, roomId);
    }

    /**
     * 测试发送消息（HTTP接口，用于测试）
     * @param message 消息内容
     * @return 是否成功
     */
    @PostMapping("/sendMessage")
    public Boolean sendMessage(@RequestBody ChatMessage message) {
        logUtil.info("调用发送消息接口，roomId={}, senderId={}", message.getRoomId(), message.getSenderId());
        return chatRoomService.sendMessage(message);
    }

    /**
     * 测试自动退群（HTTP接口，用于测试）
     * @param userId 用户ID
     * @param roomId 房间ID
     */
    @PostMapping("/autoLeave")
    public void autoLeaveChatRoom(
            @RequestParam String userId,
            @RequestParam String roomId) {
        logUtil.info("调用自动退群接口，userId={}, roomId={}", userId, roomId);
        chatRoomService.autoLeaveChatRoom(userId, roomId);
    }
}