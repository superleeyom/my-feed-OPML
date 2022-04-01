package com.leeyom.opml.app;

import be.ceau.opml.OpmlParseException;
import be.ceau.opml.OpmlParser;
import be.ceau.opml.entity.Body;
import be.ceau.opml.entity.Opml;
import be.ceau.opml.entity.Outline;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.leeyom.opml.app.sender.SenderFactory;
import lombok.extern.slf4j.Slf4j;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class OpmlUtils {

    public static final String IMPORT_OPML_API = "https://cloud.feedly.com/v3/opml";
    private static final int MAX_NUM = 50;
    private static final String BR = "\n";

    public static void main(String[] args) throws IOException, OpmlParseException {
        // 1、请求feedly api
        HttpResponse response = requestFeedlyApi();
        // 2、导出opml
        File file = importOpml(response.body());
        // 3、更新README.md
        updateReadme(file);
    }

    private static HttpResponse requestFeedlyApi() {
        String feedlyToken = System.getenv("FEEDLY_TOKEN");
        String errMsg;
        if (StrUtil.isBlank(feedlyToken)) {
            errMsg = "feedly token is null!!!";
            SenderFactory.send(errMsg);
            throw new RuntimeException(errMsg);
        }
        HttpResponse response = HttpRequest.get(IMPORT_OPML_API).auth("Bearer " + feedlyToken).execute();
        if (response.getStatus() == HttpStatus.HTTP_UNAUTHORIZED) {
            errMsg = "feedly token expired!!!";
            SenderFactory.send(errMsg);
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

        // header
        String githubName = System.getenv("GITHUB_NAME");
        StrBuilder readmd = new StrBuilder();
        readmd.append("**<p align=\"center\">[My Feedly OPML](https://github.com/superleeyom/my-feed-OPML)</p>**").append(BR);
        readmd.append("====").append(BR).append(BR);
        readmd.append(new BoldText("分享我订阅的一些 Blog 和 Newsletter，每天自动同步我 Feedly 上的订阅源，✅ 代表能正常订阅，❌ 代表暂无法订阅（对于无法订阅的 feed，会通过 Telegram Bot 提醒我更新），"))
                .append(new Link("opml 下载地址", "https://github.com/" + githubName + "/my-feed-OPML/releases/download/latest/feed.opml"))
                .append(BR).append(BR);
        // github actions 上用的是零时区，需要转成北京时间，+8个小时
        readmd.append(new BoldText("最新更新时间（北京时间）：" + utcToBeijing(new Date())))
                .append(BR).append(BR);

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
                    StrBuilder invalidFeed = new StrBuilder();
                    invalidFeed.append(tag).append(title).append("：").append(xmlUrl).append(BR);
                    SenderFactory.send(invalidFeed.toString());
                }
                linkList.add(or.append(new Link(tag + title, htmlUrl)).append("：").append(new Link("feed", xmlUrl)));
            }

            // 超过50个收起
            if (linkList.size() > MAX_NUM) {
                displayMore(header, linkList);
            } else {
                header.append(new UnorderedList<>(linkList)).append(BR).append(BR);
            }
            readmd.append(header);
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

    private static boolean isOnline(String xmlUrl) {
        try {
            Thread.sleep(3000);
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
            HttpResponse response = HttpRequest.get(xmlUrl).timeout(50000).execute();
            log.info("response msg: {}", xmlUrl + "：" + response.getStatus());
            return HttpStatus.HTTP_OK == response.getStatus()
                    || HttpStatus.HTTP_MOVED_PERM == response.getStatus()
                    || HttpStatus.HTTP_MOVED_TEMP == response.getStatus()
                    || HttpStatus.HTTP_SEE_OTHER == response.getStatus()
                    || HttpStatus.HTTP_NOT_MODIFIED == response.getStatus()
                    || 308 == response.getStatus();
        } catch (Exception e) {
            log.error(ExceptionUtil.stacktraceToString(e, 1000));
            return false;
        }
    }

    /**
     * 零时区转北京时间
     *
     * @param utcDate utc
     * @return 北京时间
     */
    public static String utcToBeijing(Date utcDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(utcDate);
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 8);
        Date time = calendar.getTime();
        return DateUtil.format(time, DatePattern.NORM_DATETIME_PATTERN);
    }


}
