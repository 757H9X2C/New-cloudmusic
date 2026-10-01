# New CloudMusic

将 [CloudMusic-Mod](https://github.com/FengLiuFeseliud/CloudMusic-Mod) 
从 1.21.4 移植到 1.21.11 的非官方版本。

原作者：FengLiuFeseliud（MIT 协议）。
移植者：757_BROHXC（GitHub: 757H9X2C）。
新手第一次移植，AI（Doubao、DeepSeek）辅助完成。

---

## ⚠️ 注意

- 这是 **beta 版**，会有不少 bug
- 不保证移植后全部功能正常
- 发现问题请反馈并上传日志

---

## 前置

- Minecraft **1.21.11**
- Fabric Loader **0.18.0+**
- [MaLiLib (sakura-ryoko fork)](https://github.com/sakura-ryoko/malilib) **0.27.20+**（推荐）
- [ModMenu](https://modrinth.com/mod/modmenu) **17.0.0+**
- [Fabric API](https://modrinth.com/mod/fabric-api) **0.141.6+1.21.11**

---

## 下载

- [GitHub Releases](https://github.com/757H9X2C/New-CloudMusic/releases)
- [Modrinth](https://modrinth.com/mod/new-cloudmusic)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/new-cloudmusic)

---

## 功能

- 登录网易云账号（验证码 / 邮箱 / 手机 / 二维码）
- 搜索歌曲、歌单、歌手
- 游戏内 HUD 显示播放状态、歌词、进度条
- 快捷键控制播放
- 分享到聊天框

---

## 登录

支持四种登录方式：

- 验证码
- 手机号密码
- 邮箱
- 二维码（推荐，成功率最高）

使用 `/cloudmusic login {方式}` 查看详细用法。

输入邮箱如果有报错，用双引号包裹邮箱即可，例如：
`/cloudmusic login email "user@163.com" "password"`

二维码会在游戏画面左上角绘制，无需切出游戏。

---

## 配置 / 热键

使用 [MaLiLib](https://github.com/sakura-ryoko/malilib)（masa 全家桶前置）。

高自定义、多热键。

在游戏中按 `Ctrl + C + M` 打开配置界面，或在 ModMenu 里找到 New CloudMusic 点齿轮。

---

## 指令

游戏内输入 `/cloudmusic` 查看全部指令。

常用指令：

```
/cloudmusic login qr
/cloudmusic login captcha {手机号} {验证码}
/cloudmusic login email {邮箱} {密码}
/cloudmusic login phone {手机号} {密码}
/cloudmusic my like
/cloudmusic my playlist
/cloudmusic search music {关键词}
/cloudmusic prev
/cloudmusic next
/cloudmusic stop
```


---

## 移植版改动

- 修复了原作者歌词卡顿、延迟的 bug（AI 辅助修复）

---

## 已知问题

- captcha 登录可能触发网易云风控
- 歌单翻页按钮暂不可用（临时方案：用 `/cloudmusic page next` 和 `/cloudmusic page prev`）

---

## 致谢

- 原作者：FengLiuFeseliud（MIT 协议）
- 移植：757_BROHXC（GitHub: 757H9X2C）
- AI 辅助：Doubao、DeepSeek
- API 来源：[NeteaseCloudMusicApi](https://github.com/Binaryify/NeteaseCloudMusicApi)

---

## 协议

MIT