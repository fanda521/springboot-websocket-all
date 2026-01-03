package com.jeffrey.websocketclient03.config.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.Collections;

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