package com.example.study.im_websocket_client01.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置类：严格对齐YML层级，connect-mode与userId同级
 */
@Component
@ConfigurationProperties(prefix = "im")
@Data
public class PropertiesConfig {
    // im.client 层级（包含userId和新增的connect-mode）
    private ClientConfig client = new ClientConfig();
    // im.websocket 层级（原有）
    private WebsocketConfig websocket = new WebsocketConfig();
    // im.server 层级（原有）
    private ServerConfig server = new ServerConfig();
    // im.failover 层级（原有）
    private FailoverConfig failover = new FailoverConfig();

    /**
     * im.client 子配置（userId和connect-mode同级）
     */
    @Data
    public static class ClientConfig {
        // 原有字段
        private String userId;
        // 新增字段：连接模式（与userId同级）
        private ConnectMode connectMode = ConnectMode.RANDOM;

        // 连接模式枚举（内部枚举，不影响层级）
        public enum ConnectMode {
            RANDOM,    // 随机连接实例
            SPECIFIED  // 指定默认实例
        }
    }

    // 以下为原有子配置，完全不变
    @Data
    public static class WebsocketConfig {
        private String path;
        private long heartbeatInterval;
    }

    @Data
    public static class ServerConfig {
        private String list;
        private long retryInterval;
        private int timeout;
    }

    @Data
    public static class FailoverConfig {
        private boolean enable;
        private int maxRetry;
    }
}