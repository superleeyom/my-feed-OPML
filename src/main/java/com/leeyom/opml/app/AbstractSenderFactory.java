package com.leeyom.opml.app;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置
 *
 * @author stormbuf
 * @since 2022/03/31
 */
@Slf4j
@Data
public abstract class AbstractSenderFactory {
    /**
     * tg、email、serverChan
     * e.g. tg&email&serverChan
     */
    private static String SENDER;

    /**
     * tg
     */
    private static String TG_TOKEN;
    private static String TG_CHAT_ID;

    /**
     * email
     */
    private static String EMAIL;
    private static String EMAIL_PASS;
    private static String EMAIL_HOST;

    /**
     * serverChan
     */
    private static String SC_KEY;

    private static List<Sender> SENDERS = new ArrayList<>();

    /**
     * 初始化
     */
    static {
        Map<String, String> envs = System.getenv();
        log.info("环境变量：{}", envs);
        SENDER = envs.get("INPUT_SENDER");
        TG_TOKEN = envs.get("INPUT_TG_TOKEN");
        TG_CHAT_ID = envs.get("INPUT_TG_CHAT_ID");
        EMAIL = envs.get("INPUT_EMAIL");
        EMAIL_PASS = envs.get("INPUT_EMAIL_PASS");
        EMAIL_HOST = envs.get("INPUT_EMAIL_HOST");
        SC_KEY = envs.get("INPUT_SC_KEY");

        if (StrUtil.isNotBlank(SENDER)) {
            String[] senders = StrUtil.split(SENDER, "&");
            for (String sender : senders) {
                switch (sender) {
                    case "tg":
                        SENDERS.add(new TelegramBot(Convert.toLong(TG_CHAT_ID), TG_TOKEN));
                        break;
                    case "email":
                        SENDERS.add(new Mail(EMAIL_HOST, EMAIL_PASS, EMAIL));
                        break;
                    case "serverChan":
                        SENDERS.add(new ServerChan(SC_KEY));
                        break;
                    default:
                        break;
                }
            }

        }
    }

    public static void send(String msg) {
        if (StrUtil.isBlank(msg)) {
            return;
        }
        SENDERS.forEach(sender -> sender.sendMessage(msg));
    }

    private AbstractSenderFactory() {
    }
}
