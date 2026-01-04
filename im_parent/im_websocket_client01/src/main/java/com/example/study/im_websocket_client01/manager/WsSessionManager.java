package com.example.study.im_websocket_client01.manager;


import com.example.study.im_websocket_client01.config.PropertiesConfig;
import com.example.study.im_websocket_client01.handler.ClientWebSocketHandler;
import com.example.study.im_websocket_client01.service.FailoverService;
import com.example.study.im_websocket_client01.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 修正：
 * 1. 确保@Component注解（Spring管理）
 * 2. 注入依赖使用@Resource
 * 3. 构造ClientWebSocketHandler时this类型匹配
 */
@Component // 关键：确保WsSessionManager是Spring组件，this能被正确引用
public class WsSessionManager {
    // 服务端实例 -> WS客户端连接
    private final Map<String, WebSocketClient> serverWsClients = new ConcurrentHashMap<>();
    // 服务端实例列表
    private List<String> serverList = new ArrayList<>();
    // 当前默认服务端实例
    private String defaultServer;

    // ========== 修正依赖注入方式（使用@Resource） ==========
    @Resource // 注入PropertiesConfig（Spring管理）
    private PropertiesConfig propertiesConfig;
    @Resource // 注入FailoverService（Spring管理）
    private FailoverService failoverService;
    @Resource // 注入LogUtil（Spring管理）
    private LogUtil logUtil;
    @Resource // 注入ObjectMapper（Spring管理，需配置Bean）
    private ObjectMapper objectMapper;

    // ========== 初始化方法（不变） ==========
    @PostConstruct
    public void init() {
        parseServerList();
        PropertiesConfig.ClientConfig.ConnectMode connectMode = propertiesConfig.getClient().getConnectMode();
        if (connectMode == PropertiesConfig.ClientConfig.ConnectMode.RANDOM) {
            defaultServer = randomSelectServer();
            logUtil.info("【随机连接模式】初始选中服务端实例：{}", defaultServer);
        } else {
            defaultServer = serverList.isEmpty() ? null : serverList.get(0);
            logUtil.info("【指定连接模式】初始选中服务端实例：{}", defaultServer);
        }
        if (defaultServer != null) {
            connectToServer(defaultServer);
        }
    }

    // ========== 修复connectToServer方法（核心） ==========
    public boolean connectToServer(String server) {
        try {
            String wsProtocol = server.startsWith("https") ? "wss" : "ws";
            String wsServer = wsProtocol + server.substring(server.indexOf("://"));
            String wsUri = wsServer + propertiesConfig.getWebsocket().getPath() +
                    "?userId=" + propertiesConfig.getClient().getUserId();

            logUtil.info("尝试连接服务端实例：{}，WS地址：{}", server, wsUri);
            URI uri = new URI(wsUri);

            // 关闭旧连接
            if (serverWsClients.containsKey(server) && serverWsClients.get(server).isOpen()) {
                logUtil.info("服务端实例{}已连接，无需重复连接", server);
                return true;
            }

            // ========== 修复构造ClientWebSocketHandler的代码 ==========
            // this 是 WsSessionManager 实例（当前类），类型完全匹配构造器
            ClientWebSocketHandler client = new ClientWebSocketHandler(uri, this, logUtil, objectMapper);
            client.connect();

            // 等待连接
            Thread.sleep(2000);
            if (client.isOpen()) {
                serverWsClients.put(server, client);
                logUtil.info("成功连接服务端实例：{}", server);
                return true;
            } else {
                logUtil.error("连接服务端实例{}超时（{}ms）", server, propertiesConfig.getServer().getTimeout());
                return false;
            }
        } catch (URISyntaxException | InterruptedException e) {
            logUtil.error("连接服务端实例{}异常：{}", server, e.getMessage());
            return false;
        }
    }

    // ========== 其他方法（不变） ==========
    private void parseServerList() {
        String serverListStr = propertiesConfig.getServer().getList();
        if (serverListStr == null || serverListStr.isEmpty()) {
            return;
        }
        String[] servers = serverListStr.split(",");
        for (String server : servers) {
            String trimServer = server.trim();
            if (!trimServer.isEmpty() && !serverList.contains(trimServer)) {
                serverList.add(trimServer);
            }
        }
        logUtil.info("解析服务端实例列表完成：{}", serverList);
    }

    public String randomSelectServer() {
        if (serverList.isEmpty()) {
            logUtil.error("服务端实例列表为空，无法随机选择");
            return null;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(serverList.size());
        return serverList.get(randomIndex);
    }

    public boolean connectToRandomServer() {
        String randomServer = randomSelectServer();
        if (randomServer == null) {
            return false;
        }
        this.defaultServer = randomServer;
        return connectToServer(randomServer);
    }

    public boolean sendMessageToServer(String server, String message) {
        WebSocketClient client = serverWsClients.get(server);
        if (client == null || !client.isOpen()) {
            logUtil.error("服务端实例{}未连接，尝试重连", server);
            if (connectToServer(server)) {
                client = serverWsClients.get(server);
            } else {
                return false;
            }
        }
        client.send(message);
        logUtil.info("发送消息到服务端实例{}：{}", server, message);
        return true;
    }

    // Getter & Setter
    public String getDefaultServer() {
        return defaultServer;
    }

    public List<String> getServerList() {
        return serverList;
    }

    public Map<String, WebSocketClient> getServerWsClients() {
        return serverWsClients;
    }

    public void switchDefaultServer(String server) {
        this.defaultServer = server;
        if (!serverWsClients.containsKey(server)) {
            connectToServer(server);
        }
    }

    public boolean sendMessageToDefaultServer(String message) {
        return sendMessageToServer(defaultServer, message);
    }
}