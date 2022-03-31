package com.leeyom.opml.app;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮件通知工具
 *
 * @author stormbuf
 * @since 2022/03/31
 */
@Slf4j
@Data
public class Mail implements Sender {

    private MailAccount mailAccount = new MailAccount();
    private String to;

    public Mail(String host, String pass, String email) {
        this.mailAccount.setHost(host);
        this.mailAccount.setPass(pass);
        this.mailAccount.setFrom(email);
        this.to = email;
    }

    @Override
    public void sendMessage(String content) {
        sendMessage("my-feed-OPML msg", content);
    }

    @Override
    public void sendMessage(String title, String content) {
        try {
            MailUtil.send(mailAccount, to, title, content, true);
        } catch (Exception e) {
            log.error("Email send error", e);
        }
    }
}
