package com.example.study.im_websocket_client01.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LogUtil {
    @Value("${im.client.userId}")
    private String userId;

    private final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public void info(String format, Object... args) {
        logger.info("[用户ID:{}] " + format, addUserId(args));
    }

    public void error(String format, Object... args) {
        logger.error("[用户ID:{}] " + format, addUserId(args));
    }

    public void warn(String format, Object... args) {
        logger.warn("[用户ID:{}] " + format, addUserId(args));
    }

    private Object[] addUserId(Object[] args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = userId;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return newArgs;
    }
}