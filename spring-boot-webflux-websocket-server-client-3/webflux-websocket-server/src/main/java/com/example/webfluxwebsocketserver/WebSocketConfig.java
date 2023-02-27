package com.example.webfluxwebsocketserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    public HandlerMapping webSocketMapping() {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        Map<String, WebSocketHandler> handlerMap = new LinkedHashMap<>();
        handlerMap.put("/path", new EchoHandler());
        simpleUrlHandlerMapping.setUrlMap(handlerMap);
        simpleUrlHandlerMapping.setOrder(-1);
        return simpleUrlHandlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    //使用boot内置tomcat时需要注入此bean
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}