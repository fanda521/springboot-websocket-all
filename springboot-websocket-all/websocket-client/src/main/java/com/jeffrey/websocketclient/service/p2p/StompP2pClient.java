package com.jeffrey.websocketclient.service.p2p;

import com.jeffrey.websocketclient.entity.P2pMessage;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * STOMP客户端工具类（通用）
 */
public class StompP2pClient {
    private final String clientId; // 客户端用户标识（如user1、user2）
    private StompSession stompSession; // STOMP会话

    public StompP2pClient(String clientId) {
        this.clientId = clientId;
    }

    /**
     * 连接WebSocket服务端
     */
    public void connect(String url) throws Exception {
        // 1. 配置WebSocket传输方式
        Transport transport = new WebSocketTransport(new StandardWebSocketClient());
        SockJsClient sockJsClient = new SockJsClient(Collections.singletonList(transport));

        // 2. 创建STOMP客户端
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        // 设置消息转换器（JSON）
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // 3. 连接服务端并创建会话
        ListenableFuture<StompSession> listenableFuture = stompClient.connect(
                url,
                new StompSessionHandlerAdapter() {
                    // 会话连接成功回调
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        stompSession = session;
                        System.out.println(clientId + " 连接服务端成功！SessionId：" + session.getSessionId());
                        // 订阅专属的点对点消息队列：/user/{clientId}/queue/message
                        subscribeP2pMessage();
                    }
                }
        );

        // 等待连接完成
        stompSession = listenableFuture.get(5, TimeUnit.SECONDS);
    }

    /**
     * 订阅点对点消息队列（接收其他用户发送的消息）
     */
    private void subscribeP2pMessage() {
        String subscribeDestination = "/user/" + clientId + "/queue/message";
        stompSession.subscribe(subscribeDestination, new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // 接收点对点消息（payload为P2pMessage对象）
                P2pMessage message = (P2pMessage) payload;
                System.out.println(clientId + " 接收点对点消息：" + message.getFromUser() + "：" + message.getContent());
            }

            // 指定消息类型（必须，否则无法反序列化）
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return P2pMessage.class;
            }
        });
        System.out.println(clientId + " 已订阅点对点消息队列：" + subscribeDestination);
    }

    /**
     * 发送点对点消息
     */
    public void sendP2pMessage(String toUser, String content) {
        if (stompSession == null || !stompSession.isConnected()) {
            throw new RuntimeException(clientId + " 未连接服务端！");
        }
        // 构造消息体
        P2pMessage message = new P2pMessage(toUser, clientId, content);
        // 发送到服务端的 /app/p2p/send 地址
        stompSession.send("/app/p2p/send", message);
        System.out.println(clientId + " 发送点对点消息到 " + toUser + "：" + content);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (stompSession != null) {
            stompSession.disconnect();
            System.out.println(clientId + " 断开与服务端的连接！");
        }
    }
}