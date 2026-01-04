package com.example.study.im_websocket_server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "im")
public class PropertiesConfig {
    private ChatRoomConfig chatRoom;
    private UserSessionConfig userSession;
    private RedisConfig redis;
    private FailoverConfig failover;
    private WebSocketConfig websocket; // 新增：绑定im.websocket节点

    @Data
    public static class WebSocketConfig { // 新增：WebSocket配置类
        private String path;
        private String allowedOrigins;
    }

    @Data
    public static class ChatRoomConfig {
        private String prefix;
        private Integer expireTime;

        public String getPrefix() {
            return prefix + ":";
        }
    }

    @Data
    public static class UserSessionConfig {
        private String prefix;
        private String roomUserPrefix;

        public String getPrefix() {
            return prefix + ":";
        }

        public String getRoomUserPrefix() {
            return roomUserPrefix+ ":";
        }
    }

    @Data
    public static class RedisConfig {
        private String channel;
    }

    @Data
    public static class FailoverConfig {
        private List<String> serverList;
        private Integer retryInterval;
    }
}