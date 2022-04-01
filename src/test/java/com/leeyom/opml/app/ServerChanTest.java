package com.leeyom.opml.app;

import com.leeyom.opml.app.sender.ServerChan;
import org.junit.jupiter.api.Test;

class ServerChanTest {

    @Test
    void sendMessage() {
        ServerChan serverChan = new ServerChan("");
        serverChan.sendMessage("serverChan title", "serverChan content");
    }
}