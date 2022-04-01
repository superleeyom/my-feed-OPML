package com.leeyom.opml.app.sender;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 电报机器人
 *
 * @author leeyom wang
 * @date 2021/1/30 5:57 下午
 */
@Slf4j
@Data
public class TelegramBot implements Sender {

    /**
     * 聊天id
     */
    private long chatId;

    /**
     * bot token
     */
    private String token;

    public TelegramBot(Long chatId, String token) {
        this.chatId = chatId;
        this.token = token;
    }

    /**
     * 机器人推送消息
     *
     * @param msg 消息
     */
    @Override
    public void sendMessage(String msg) {
        try {
            String url = "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&text=" + msg;
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
            String response = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
            log.info("Telegram bot response: {}", response);
        } catch (Exception e) {
            log.error("Telegram Bot send error", e);
        }
    }

    @Override
    public void sendMessage(String title, String content) {

    }

}
