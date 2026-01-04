package com.example.study.im_websocket_server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LogUtil {
    @Value("${server.instance-id}")
    private String instanceId;

    private final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    // 打印带实例ID的日志
    public void info(String format, Object... args) {
        logger.info("[实例ID:{}] " + format, addInstanceId(args));
    }

    public void error(String format, Object... args) {
        logger.error("[实例ID:{}] " + format, addInstanceId(args));
    }

    public void warn(String format, Object... args) {
        logger.warn("[实例ID:{}] " + format, addInstanceId(args));
    }

    private Object[] addInstanceId(Object[] args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = instanceId;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return newArgs;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
}