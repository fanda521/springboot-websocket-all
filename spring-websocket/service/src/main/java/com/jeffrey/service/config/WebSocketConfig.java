package com.jeffrey.service.config;

import com.jeffrey.service.handler.MyMessageHandler;
import com.jeffrey.service.interceptor.WebSocketInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    /** 
     * 注册handle 
     * @see org.springframework.web.socket.config.annotation.WebSocketConfigurer#registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry) 
     */  
    @Override  
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
          registry.addHandler(myHandler(), "/testHandler.do").addInterceptors(new WebSocketInterceptor()).setAllowedOrigins("*");
          registry.addHandler(myHandler(), "/socketJs/testHandler.do").addInterceptors(new WebSocketInterceptor()).setAllowedOrigins("*").withSockJS();
  
    }  
      
    @Bean
    public WebSocketHandler myHandler(){
        return new MyMessageHandler();
    }  
}
