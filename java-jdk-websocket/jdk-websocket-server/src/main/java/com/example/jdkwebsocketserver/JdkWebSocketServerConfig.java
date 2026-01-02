package com.example.jdkwebsocketserver;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author jeffrey
 * @version 1.0
 * @date 2023/2/27
 * @time 12:01
 * @week 星期一
 * @description webSocket 服务端 配置类
 **/
@Component
public class JdkWebSocketServerConfig {

    /**
     * ServerEndpointExporter 作用
     *
     * 这个Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
