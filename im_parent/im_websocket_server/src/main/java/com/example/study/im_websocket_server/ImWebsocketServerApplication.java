package com.example.study.im_websocket_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImWebsocketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImWebsocketServerApplication.class, args);
        System.out.println("========== IM WebSocket服务启动成功 ==========");
    }

}
