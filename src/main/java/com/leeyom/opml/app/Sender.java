package com.leeyom.opml.app;

/**
 * 发送行为
 *
 * @author stormbuf
 * @since 2022/03/31
 */
public interface Sender {

    /**
     * @param content
     */
    void sendMessage(String content);

    /**
     * @param title
     * @param content
     */
    void sendMessage(String title, String content);
}
