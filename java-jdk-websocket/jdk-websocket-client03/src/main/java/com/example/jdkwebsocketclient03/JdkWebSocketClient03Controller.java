package com.example.jdkwebsocketclient03;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @Aythor allen
 * @date 2022/11/20 0:28
 * @description
 */
@RestController
public class JdkWebSocketClient03Controller {
    @Autowired
    JdkWebSocketClient03 webSocketClient;

    @Value("${server.port}")
    private String port;

    @GetMapping("/sayHello")
    public String sayHello() {
        webSocketClient.send(port + ": hello");
        return port + ": hello";
    }

    @GetMapping("/sayAny")
    public String sayAny(@RequestParam(name = "param") String param) {
        webSocketClient.send(param + "  port=" + port);
        return param + "  port=" + port;
    }
}
