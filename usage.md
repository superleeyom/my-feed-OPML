## 推送工具

*  tg
*  server 酱
*  e-mail

通过以下环境变量控制

```bash
     /**
     *  推送工具的开关
     *  开启：添加相应单词，并以 & 相隔
     *  tg、email、serverChan
     *  e.g. tg&email&serverChan
     */
     SENDER

    填写开启工具的环境变量，没开启不需要填

    /**
     *  tg
     */
     TG_TOKEN
     TG_CHAT_ID

    /**
     *  email
     */
     EMAIL
     EMAIL_PASS
     EMAIL_HOST

    /**
     *  serverChan
     */
     SC_KEY
```