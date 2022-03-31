package com.leeyom.opml.app;

import org.junit.jupiter.api.Test;

class MailTest {

    @Test
    void sendMessage() {
        Mail mail = new Mail("smtp.163.com", "", "");
        mail.sendMessage("email test", "this is a test email");
    }
}