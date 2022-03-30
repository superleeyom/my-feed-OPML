package com.leeyom.opml.app;

import be.ceau.opml.OpmlParseException;
import be.ceau.opml.OpmlParser;
import be.ceau.opml.entity.Body;
import be.ceau.opml.entity.Opml;
import be.ceau.opml.entity.Outline;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class OpmlUtils {

    public static final String IMPORT_OPML_API = "https://cloud.feedly.com/v3/opml";
    private static final int MAX_NUM = 50;
    private static final String BR = "\n";
    private static String tgChatId;
    private static String tgToken;
    private static boolean isAlarm;

    public static void main(String[] args) throws IOException, OpmlParseException {
        // 1、请求feedly api
        HttpResponse response = requestFeedlyApi(args);
        // 2、导出opml
        File file = importOpml(response.body());
        // 3、更新README.md
        updateReadme(file);
    }

    private static HttpResponse requestFeedlyApi(String[] args) {
        String feedlyToken = args[0];
        tgChatId = args[1];
        tgToken = args[2];
        isAlarm = StrUtil.isNotBlank(tgChatId) && StrUtil.isNotBlank(tgToken);
        String errMsg;
        if (StrUtil.isBlank(feedlyToken)) {
            errMsg = "feedly token is null!!!";
            sendMsgToTelegram(isAlarm, errMsg);
            throw new RuntimeException(errMsg);
        }
        HttpResponse response = HttpRequest.get(IMPORT_OPML_API).auth("Bearer " + feedlyToken).execute();
        if (response.getStatus() == HttpStatus.HTTP_UNAUTHORIZED) {
            errMsg = "feedly token expired!!!";
            sendMsgToTelegram(isAlarm, errMsg);
            throw new RuntimeException(errMsg);
        }
        return response;
    }

    private static void updateReadme(File opmlFile) throws FileNotFoundException, OpmlParseException {
        log.info("===========================开始更新 README.md===========================");
        Opml opml = new OpmlParser().parse(new FileInputStream(opmlFile));
        Body body = opml.getBody();
        List<Outline> originOutlines = body.getOutlines();
        if (CollUtil.isEmpty(originOutlines)) {
            log.error("originOutlines is empty!!!");
            return;
        }
        // 过滤掉 Must Read
        List<Outline> outlines = originOutlines.stream().filter(o -> !"Must Read".equals(o.getText())).collect(Collectors.toList());
        if (CollUtil.isEmpty(outlines)) {
            log.error("outlines is empty!!!");
            return;
        }
        //
        StrBuilder readmd = new StrBuilder();
        readmd.append(new BoldText("分享我订阅的一些 Blog 和 Newsletter，每天自动同步我 Feedly 上的订阅源，✅ 代表能正常订阅，❌ 代表暂无法订阅（对于无法订阅的 feed，会通过 Telegram bot 提醒我更新），"))
                .append(new Link("opml 地址", "https://github.com/superleeyom/my-feed-OPML/blob/master/feed.opml"))
                .append(BR).append(BR);
        // 已失效的订阅
        StrBuilder invalidFeed = new StrBuilder();
        for (Outline outline : outlines) {
            StrBuilder header = new StrBuilder().append(new Heading(outline.getText(), 2)).append(BR);
            List<Object> linkList = CollUtil.newArrayList();
            List<Outline> subElements = outline.getSubElements();
            if (CollUtil.isEmpty(subElements)) {
                readmd.append(header);
                continue;
            }
            for (Outline subElement : subElements) {
                StrBuilder or = new StrBuilder();
                String title = subElement.getAttribute("title");
                String xmlUrl = subElement.getAttribute("xmlUrl");
                String htmlUrl = subElement.getAttribute("htmlUrl");
                if (StrUtil.isBlank(title)) {
                    continue;
                }
                String tag;
                if (isOnline(xmlUrl)) {
                    tag = "✅ ";
                } else {
                    tag = "❌ ";
                    invalidFeed.append(tag).append(title).append("：").append(xmlUrl).append(BR);
                }
                linkList.add(or.append(new Link(tag + title, htmlUrl)).append("：").append(new Link("feed", xmlUrl)));
            }

            // 超过20个收起
            if (linkList.size() > MAX_NUM) {
                displayMore(header, linkList);
            } else {
                header.append(new UnorderedList<>(linkList)).append(BR).append(BR);
            }
            readmd.append(header);
        }

        // 对于已失效的订阅，telegram 进行告警
        if (StrUtil.isNotBlank(invalidFeed)) {
            sendMsgToTelegram(isAlarm, invalidFeed.toString());
        }

        File readmeMd = new File("README.md");
        FileWriter readmeMdWriter = new FileWriter(readmeMd);
        readmeMdWriter.write(readmd.toString());
        log.info("===========================README.md更新完毕===========================");
    }

    private static void displayMore(StrBuilder header, List<Object> linkList) {
        header.append(new UnorderedList<>(ListUtil.sub(linkList, 0, MAX_NUM))).append(BR);
        header.append("<details><summary>显示更多</summary>").append(BR).append(BR);
        header.append(new UnorderedList<>(ListUtil.sub(linkList, MAX_NUM, linkList.size()))).append(BR);
        header.append("</details>").append(BR);
    }

    private static File importOpml(String opmlText) {
        log.info("===========================开始创建opml===========================");
        File opmlFile = new File("feed.opml");
        FileWriter opmlWriter = new FileWriter(opmlFile);
        opmlWriter.write(opmlText);
        log.info("===========================opml创建成功===========================");
        return opmlFile;
    }

    private static void sendMsgToTelegram(boolean isAlarm, String msg) {
        if (isAlarm) {
            TelegramBot bot = new TelegramBot(Convert.toLong(tgChatId), tgToken);
            bot.sendMessage(msg);
        }
    }

    private static boolean isOnline(String xmlUrl) {
        try {
            Thread.sleep(3000);
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
            HttpResponse response = HttpRequest.get(xmlUrl).timeout(50000).execute();
            return HttpStatus.HTTP_OK == response.getStatus();
        } catch (Exception e) {
            log.error(ExceptionUtil.stacktraceToString(e, 1000));
            return false;
        }
    }

}
