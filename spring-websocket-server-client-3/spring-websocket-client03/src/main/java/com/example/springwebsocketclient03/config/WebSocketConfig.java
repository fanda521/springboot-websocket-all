package com.example.springwebsocketclient03.config;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @Auther: liaoshiyao
 * @Date: 2019/1/11 17:38
 * @Description: 配置websocket后台客户端
 */
@Slf4j
@Component
public class WebSocketConfig {

    @Bean
    public WebSocketClient webServiceClient() throws URISyntaxException {

        MyWebSocketClient mWebSocketClient = new MyWebSocketClient(new URI("ws://localhost:7620/spring-websocket?userId=spring-client03"));
        mWebSocketClient.connect();
        return mWebSocketClient;
    }

 
}