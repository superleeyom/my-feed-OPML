package com.leeyom.opml.app;

import org.junit.jupiter.api.Test;

class ServerChanTest {

    @Test
    void sendMessage() {
        ServerChan serverChan = new ServerChan("");
        serverChan.sendMessage("serverChan title", "serverChan content");
    }
}