package com.leeyom.opml.app;

import org.junit.jupiter.api.Test;

class ServerChanTest {

    @Test
    void send() {
        ServerChan serverChan = new ServerChan("");
        serverChan.send("serverChan title", "serverChan content");
    }
}