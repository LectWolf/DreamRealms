# DreamRealms 次梦领域

为次梦领域 Minecraft 服务器开发的专属插件，基于 [PluginBase](https://github.com/MrXiaoM/PluginBase) 框架。

## 支持版本

- **Minecraft**: 1.21+
- **Java**: 21+
- **服务端核心**: Spigot / Paper / Folia

## 功能模块

| 模块         | 说明                     | 状态 |
| ------------ | ------------------------ | ---- |
| TimeSync     | 服务器时间与现实时间同步 | ✅   |
| Welcome      | 新玩家欢迎系统           | ✅   |
| DogTag       | 死亡狗牌掉落系统         | ✅   |
| ShiftF       | Shift+F 快捷键执行命令   | ✅   |
| OwnerBind    | 物主绑定系统             | ✅   |
| Debug        | 调试工具模块             | ✅   |
| ItemManager  | 物品管理器               | ✅   |
| CosmeticMenu | HMCCosmetics 时装菜单    | ✅   |
| GiftPoints   | 点券赠送系统             | ✅   |
| Wank         | 导管娱乐模块             | ✅   |

## 文档

详细文档请查看 [在线文档](https://lectwolf.github.io/DreamRealms/)

## 软依赖

- Vault - 经济系统
- PlaceholderAPI - 变量支持
- PlayerPoints - 点券系统
- CraftEngine - 自定义物品
- ItemsAdder - 自定义物品
- SweetMail - 邮件系统
- SweetCheckout - 支付系统
- GlobalMarketPlus - 全球市场
- zAuctionHouse - 拍卖行
- QuickShop-Hikari - 箱子商店
- HMCCosmetics - 时装系统

## 安装

1. 下载 [最新 Release](https://github.com/LectWolf/DreamRealms/releases)
2. 放入 `plugins/` 目录
3. 重启服务器
4. 编辑 `plugins/DreamRealms/config.yml` 启用需要的模块

## 命令

| 命令           | 说明         | 权限                       |
| -------------- | ------------ | -------------------------- |
| `/dr reload`   | 重载配置     | `dreamrealms.admin`        |
| `/dr timesync` | 时间同步模块 | `dreamrealms.timesync`     |
| `/dr welcome`  | 欢迎模块     | `dreamrealms.welcome`      |
| `/dr ob`       | 物主绑定模块 | `dreamrealms.ownerbind`    |
| `/dr debug`    | 调试工具     | `dreamrealms.debug`        |
| `/im`          | 物品管理器   | `dreamrealms.itemmanager`  |
| `/cos`         | 时装菜单     | `dreamrealms.cosmeticmenu` |
| `/gift`        | 点券赠送     | `dreamrealms.giftpoints`   |
| `/wank`        | 导管模块     | 无                         |

## 构建

```bash
./gradlew build
```

产物位于 `out/`

## 许可证

本项目采用 [GPL-3.0](LICENSE) 许可证。
