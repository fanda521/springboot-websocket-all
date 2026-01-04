package com.example.study.im_websocket_server.service;

import com.example.study.im_websocket_server.entity.UserSession;

public interface SessionService {
    /**
     * 保存用户会话
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void saveUserSession(String userId, String sessionId);

    /**
     * 获取用户会话
     * @param userId 用户ID
     * @return 会话信息
     */
    UserSession getUserSession(String userId);

    /**
     * 添加用户到房间
     * @param userId 用户ID
     * @param roomId 房间ID
     */
    void addUserToRoom(String userId, String roomId);

    /**
     * 从房间移除用户
     * @param userId 用户ID
     * @param roomId 房间ID
     */
    void removeUserFromRoom(String userId, String roomId);

    /**
     * 移除用户会话
     * @param userId 用户ID
     */
    void removeUserSession(String userId);
}