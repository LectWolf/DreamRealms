# Welcome 欢迎模块

新玩家进服欢迎系统，支持可点击的欢迎文本和奖励机制。

## 功能

- 新玩家进服时向其他玩家发送可点击的欢迎文本
- 鼠标悬浮显示提示信息
- 点击后随机发送一条欢迎消息（以玩家聊天形式）
- 可配置延迟奖励（默认 5 分钟后发放）
- 可配置新玩家退出后是否发放奖励
- 欢迎者和新玩家都可获得奖励
- 新玩家奖励 = 基础数值 × 欢迎人数
- 数据库记录新玩家信息（支持多服务器）
- 提供 [API](/api/welcome) 供其他插件调用

## 命令

| 命令                         | 说明               | 权限                  |
| ---------------------------- | ------------------ | --------------------- |
| `/dr welcome status`         | 查看模块状态       | `dreamrealms.welcome` |
| `/dr welcome reload`         | 重载配置           | `dreamrealms.welcome` |
| `/dr welcome delay <分钟>`   | 设置奖励延迟时间   | `dreamrealms.welcome` |
| `/dr welcome quit <on\|off>` | 设置退出是否发奖励 | `dreamrealms.welcome` |

## 权限

| 权限                  | 说明         |
| --------------------- | ------------ |
| `dreamrealms.welcome` | 管理欢迎模块 |

## 配置文件

- `modules/welcome/settings.yml` - 模块配置
