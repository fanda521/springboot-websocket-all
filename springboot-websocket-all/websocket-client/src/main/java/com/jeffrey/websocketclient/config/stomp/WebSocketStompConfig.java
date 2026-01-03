package com.jeffrey.websocketclient.config.stomp;

import com.jeffrey.websocketclient.entity.PubSubMessage;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: liaoshiyao
 * @Date: 2019/1/11 17:38
 * @Description: 配置websocket后台客户端
 */
@Slf4j
@Configuration
public class WebSocketStompConfig {

    // 配置 WebSocketStompClient Bean
    @Bean(name = "webSocketStompClient")
    public WebSocketStompClient createWebSocketStompClient() {
        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        SockJsClient sockJsClient = new SockJsClient(Collections.singletonList(webSocketTransport));

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("stomp-client-");
        scheduler.initialize();

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setTaskScheduler(scheduler);
        stompClient.setDefaultHeartbeat(new long[]{10000, 10000});

        return stompClient;
    }


}