import { defineConfig } from "vitepress";

export default defineConfig({
  title: "DreamRealms",
  description: "Minecraft Spigot/Paper 插件文档",
  lang: "zh-CN",
  base: "/DreamRealms/",
  head: [["link", { rel: "icon", href: "/DreamRealms/favicon.svg" }]],

  themeConfig: {
    logo: "/favicon.svg",
    nav: [
      { text: "首页", link: "/" },
      { text: "指南", link: "/guide/" },
      { text: "模块", link: "/modules/" },
      { text: "API", link: "/api/" },
      { text: "更新日志", link: "/changelog" },
      { text: "GitHub", link: "https://github.com/LectWolf/DreamRealms" },
    ],

    sidebar: {
      "/guide/": [
        {
          text: "入门",
          items: [
            { text: "安装", link: "/guide/" },
            { text: "配置", link: "/guide/config" },
            { text: "命令", link: "/guide/commands" },
          ],
        },
      ],
      "/modules/": [
        {
          text: "模块",
          items: [
            { text: "概述", link: "/modules/" },
            { text: "TimeSync 时间同步", link: "/modules/timesync" },
            { text: "Welcome 欢迎系统", link: "/modules/welcome" },
            { text: "DogTag 狗牌系统", link: "/modules/dogtag" },
            { text: "ShiftF 快捷键", link: "/modules/shiftf" },
            { text: "OwnerBind 物主绑定", link: "/modules/ownerbind" },
            { text: "Debug 调试工具", link: "/modules/debug" },
            { text: "ItemManager 物品管理器", link: "/modules/itemmanager" },
            { text: "CosmeticMenu 时装菜单", link: "/modules/cosmeticmenu" },
            { text: "GiftPoints 点券赠送", link: "/modules/giftpoints" },
            { text: "Wank 导管模块", link: "/modules/wank" },
          ],
        },
      ],
      "/api/": [
        {
          text: "API",
          items: [
            { text: "概述", link: "/api/" },
            { text: "OwnerBind", link: "/api/ownerbind" },
            { text: "Welcome", link: "/api/welcome" },
          ],
        },
      ],
    },

    socialLinks: [
      { icon: "github", link: "https://github.com/LectWolf/DreamRealms" },
    ],

    footer: {
      message: "Released under the GPL-3.0 License.",
      copyright: "Copyright © 2025 LectWolf",
    },

    // 中文本地化
    outline: {
      label: "页面导航",
    },
    docFooter: {
      prev: "上一页",
      next: "下一页",
    },
    lastUpdated: {
      text: "最后更新于",
    },
    darkModeSwitchLabel: "主题",
    sidebarMenuLabel: "菜单",
    returnToTopLabel: "回到顶部",
    langMenuLabel: "语言",
  },
});
