package com.example.study.im_websocket_server.controller;

import com.example.study.im_websocket_server.util.LogUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ws")
@RequiredArgsConstructor
public class WebSocketController {

    private final LogUtil logUtil;

    /**
     * 获取当前服务实例信息
     * @return 实例ID
     */
    @GetMapping("/instance")
    public String getInstanceInfo() {
        String instanceId = logUtil.getInstanceId();
        logUtil.info("获取实例信息，instanceId={}", instanceId);
        return "当前服务实例ID：" + instanceId;
    }
}