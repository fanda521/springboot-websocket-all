package com.example.webfluxwebsocketclient01;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Client {

  public static void main(String[] args) throws URISyntaxException {

    WebSocketClient client = new ReactorNettyWebSocketClient();
    URI url = new URI("ws://10.1.5.13:7420/path");
    client.execute(url,
        session -> session.send(Mono.just(session.textMessage("hello world")))
            .thenMany(session.receive().map(WebSocketMessage::getPayloadAsText).log())
            .then())
        .block(Duration.ofSeconds(2));
  }


/*    public static void main(final String[] args) {
      final WebSocketClient client = new ReactorNettyWebSocketClient();
      client.execute(URI.create("ws://localhost:7552/websocket/JdkWebSocketClient01"),
              session -> session.send(Flux.just(session.textMessage("你好")))
                      .thenMany(session.receive().take(1)
                      .map(WebSocketMessage::getPayloadAsText))
                      .doOnNext(System.out::println) .then())
              .block(Duration.ofMillis(5000));
    }*/
}
