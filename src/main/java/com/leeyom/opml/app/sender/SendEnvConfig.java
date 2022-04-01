package com.leeyom.opml.app.sender;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 环境变量
 *
 * @author leeyom.wang
 * @date 2022/4/1 2:52 PM
 */
@Slf4j
public class SendEnvConfig {

    /**
     * telegram
     */
    private static final String TG_TOKEN;
    private static final String TG_CHAT_ID;

    /**
     * email
     */
    private static final String EMAIL;
    private static final String EMAIL_PASS;
    private static final String EMAIL_HOST;

    /**
     * server酱
     */
    private static final String SC_KEY;

    /**
     * 消息推送器
     */
    public static List<Sender> SENDERS = new ArrayList<>();

    static {

        Map<String, String> envs = System.getenv();
        TG_TOKEN = envs.get("TG_TOKEN");
        TG_CHAT_ID = envs.get("TG_CHAT_ID");
        EMAIL = envs.get("EMAIL");
        EMAIL_PASS = envs.get("EMAIL_PASS");
        EMAIL_HOST = envs.get("EMAIL_HOST");
        SC_KEY = envs.get("SC_KEY");

        if (StrUtil.isNotBlank(TG_CHAT_ID) && StrUtil.isNotBlank(TG_TOKEN)) {
            SENDERS.add(new TelegramBot(Convert.toLong(TG_CHAT_ID), TG_TOKEN));
        } else {
            log.warn("telegram sender init fail, please check TG_CHAT_ID and TG_TOKEN.");
        }

        if (StrUtil.isNotBlank(EMAIL_HOST) && StrUtil.isNotBlank(EMAIL_PASS) && StrUtil.isNotBlank(EMAIL)) {
            SENDERS.add(new Mail(EMAIL_HOST, EMAIL_PASS, EMAIL));
        } else {
            log.warn("email sender init fail, please check EMAIL_HOST and EMAIL_PASS and EMAIL.");
        }

        if (StrUtil.isNotBlank(SC_KEY)) {
            SENDERS.add(new ServerChan(SC_KEY));
        } else {
            log.warn("serverChan sender init fail, please check SC_KEY.");
        }
    }

}
