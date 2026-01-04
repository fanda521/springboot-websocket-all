package com.example.study.im_websocket_client01.service;


import com.example.study.im_websocket_client01.config.PropertiesConfig;
import com.example.study.im_websocket_client01.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class FailoverService {

    private final PropertiesConfig propertiesConfig;
    private final LogUtil logUtil;
    private final AtomicInteger currentServerIndex = new AtomicInteger(0);
    private final AtomicInteger retryCount = new AtomicInteger(0);

    /**
     * 获取下一个可用的服务端地址
     * @return 服务端地址
     */
    public String getNextServer() {
        String serverList = propertiesConfig.getServer().getList();
        if (serverList.isEmpty()) {
            logUtil.error("服务端列表为空");
            return null;
        }
        String[] split = serverList.split(",");
        int index = currentServerIndex.getAndIncrement() % split.length;
        String server = split[index];
        logUtil.info("切换到下一个服务端，地址={}, 当前重试次数={}", server, retryCount.get());
        return server;
    }

    /**
     * 重置重试次数
     */
    public void resetRetryCount() {
        retryCount.set(0);
        logUtil.info("重置重试次数");
    }

    /**
     * 增加重试次数
     * @return 是否超过最大重试次数
     */
    public boolean incrementRetryCount() {
        int count = retryCount.incrementAndGet();
        logUtil.warn("重试次数增加，当前次数={}, 最大次数={}", count, propertiesConfig.getFailover().getMaxRetry());
        return count > propertiesConfig.getFailover().getMaxRetry();
    }

    /**
     * 检查是否启用故障转移
     * @return 是否启用
     */
    public boolean isFailoverEnabled() {
        return propertiesConfig.getFailover().isEnable();
    }
}