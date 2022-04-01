**<p align="center">[My Feedly OPML](https://github.com/{}/my-feed-OPML)</p>**
====

## 前言

**分享我订阅的一些 Blog 和 Newsletter，每天自动同步我 Feedly 上的订阅源，✅ 代表能正常订阅，❌ 代表暂无法订阅（对于无法订阅的 feed，支持 Telegram Bot、Email、Server酱等推送工具提醒更新），**[opml 下载地址](https://github.com/{}/my-feed-OPML/releases/download/latest/feed.opml)

**最新更新时间（北京时间）：{}**

## 如何使用

- fork 本仓库

- 在 `Settings --> Secrets --> Actions` 下新增如下的几个 secrets：
   - **FEEDLY_TOKEN**：**必须**，你的 Feedly token，访问 [https://feedly.com/v3/auth/dev](https://feedly.com/v3/auth/dev)，创建一个开发者 token
   - **G_TOKEN**：**必须**，你的 [Github Token](https://github.com/settings/tokens/new)，scope 不知道选啥，就全部勾上，然后拷贝生成的 token
   - **TG_CHAT_ID**：可选，你的 Telegram user id，关注机器人 [@getuseridbot](https://t.me/getuseridbot) 即可获取
   - **TG_TOKEN**：可选，你的用于接收通知的 telegram bot 的 token，如何创建 Telegram Bot，见搜索引擎
   - **EMAIL**：可选，你的用于接收通知的邮箱
   - **EMAIL_PASS**：可选，你的邮箱授权密码
   - **EMAIL_HOST**：可选，邮箱协议
   - **SC_KEY**：可选，Server酱的 secret key

- 修改 `feedly_opml_import.yml`，修改如下的配置项为你自己的 Github userName 和 Email：
   ```yml
   env:
    GITHUB_NAME: superleeyom
    GITHUB_EMAIL: 635709492@qq.com
   ```
  
- 然后 `Actions --> Workflows --> FeedlyOpmlImportApplication --> Run workflow`，手动触发任务


