package com.leeyom.opml.app;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Server 酱推送工具
 *
 * @author stormbuf
 * @since 2022/03/31
 */
@Slf4j
@Data
public class ServerChan {

    private static final String URL = "https://sctapi.ftqq.com/{}.send";
    private static final String PARAM = "title={}&desp={}";

    private String key;

    public ServerChan(String key) {
        this.key = key;
    }

    public void sendMessage(String title, String content) {
        String url = StrUtil.format(URL, key);
        String params = StrUtil.format(PARAM, title, content);
        try {
            HttpUtil.post(url, params);
        } catch (Exception e) {
            log.error("ServerChan send error", e);
        }
    }

}

