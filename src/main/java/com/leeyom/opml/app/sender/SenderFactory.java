package com.leeyom.opml.app.sender;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 配置
 *
 * @author stormbuf
 * @since 2022/03/31
 */
@Slf4j
@Data
public abstract class SenderFactory {

    public static void send(String msg) {
        if (StrUtil.isBlank(msg)) {
            log.error("send msg fail, msg is empty.");
            return;
        }
        SendEnvConfig.SENDERS.forEach(sender -> sender.sendMessage(msg));
    }

    private SenderFactory() {

    }
}
