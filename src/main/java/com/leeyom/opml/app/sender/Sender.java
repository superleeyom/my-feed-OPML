package com.leeyom.opml.app.sender;

/**
 * 发送行为
 *
 * @author stormbuf
 * @since 2022/03/31
 */
public interface Sender {

    /**
     * 消息推送
     *
     * @param content 消息内容
     */
    void sendMessage(String content);

    /**
     * 消息推送
     *
     * @param title   标题
     * @param content 消息内容
     */
    void sendMessage(String title, String content);
}
