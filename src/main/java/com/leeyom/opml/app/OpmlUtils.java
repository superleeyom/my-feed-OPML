package com.leeyom.opml.app;

import be.ceau.opml.OpmlParseException;
import be.ceau.opml.OpmlParser;
import be.ceau.opml.entity.Body;
import be.ceau.opml.entity.Opml;
import be.ceau.opml.entity.Outline;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
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
    private static final int MAX_NUM = 20;
    private static final String BR = "\n";

    public static void main(String[] args) throws IOException, OpmlParseException {
        String token = args[0];
        if (StrUtil.isBlank(token)) {
            log.error("feedly token is null!!!");
            return;
        }
        HttpResponse response = HttpRequest.get(IMPORT_OPML_API).auth("Bearer " + token).execute();
        if (response.getStatus() == HttpStatus.HTTP_UNAUTHORIZED) {
            log.error("feedly token expired!!!");
            return;
        }
        // 导出opml
        File file = importOpml(response.body());
        // 更新README.md
        updateReadme(file);
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
        readmd.append(new BoldText("分享我订阅的一些Blog和Newsletter，每天自动更新我的Feedly上的订阅源，"))
                .append(new Link("opml地址", "https://github.com/superleeyom/my-feed-OPML/blob/master/feed.opml"))
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
                linkList.add(or.append(title).append("：").append(new Link("feed", xmlUrl)).append("，").append(new Link("site", htmlUrl)));
            }

            // 超过20个收起
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

    private static File importOpml(String opmlText){
        log.info(opmlText);
        log.info("===========================开始创建opml===========================");
        File opmlFile = new File("feed.opml");
        FileWriter opmlWriter = new FileWriter(opmlFile);
        opmlWriter.write(opmlText);
        log.info("===========================opml创建成功===========================");
        return opmlFile;
    }

}
