package com.example.webfluxwebsocketserver;

import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EchoHandler implements WebSocketHandler, CorsConfigurationSource {
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<WebSocketMessage> messageFlux = session.receive().map(message -> {
            String payload = message.getPayloadAsText();
            return "Received: " + payload;
        }).map(session::textMessage).log();
        return session.send(messageFlux);
    }

    @Override
    public CorsConfiguration getCorsConfiguration(ServerWebExchange exchange) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        return configuration;
    }
}