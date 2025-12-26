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
- 数据库记录新玩家信息

## 命令

| 命令                         | 说明               | 权限                  |
| ---------------------------- | ------------------ | --------------------- |
| `/dr welcome status`         | 查看模块状态       | `dreamrealms.welcome` |
| `/dr welcome reload`         | 重载配置           | `dreamrealms.welcome` |
| `/dr welcome delay <分钟>`   | 设置奖励延迟时间   | `dreamrealms.welcome` |
| `/dr welcome quit <on\|off>` | 设置退出是否发奖励 | `dreamrealms.welcome` |
| `/dr welcome value <数值>`   | 设置基础数值       | `dreamrealms.welcome` |

## 权限

| 权限                  | 说明         |
| --------------------- | ------------ |
| `dreamrealms.welcome` | 管理欢迎模块 |

## 配置文件

- `settings.yml` - 模块配置

## 占位符说明

### 欢迎文本占位符

- `{player}` - 新玩家名称
- `{welcomer_balance}` - 欢迎者奖励数值
- `{delay}` - 奖励延迟分钟数

### 命令占位符

- `{player}` - 执行命令的玩家名称
- `{uuid}` - 执行命令的玩家 UUID
- `{new_player}` - 新玩家名称（仅欢迎者命令可用）
- `{welcomer_count}` - 欢迎人数
- `{welcomer_balance}` - 欢迎者奖励数值
- `{newplayer_balance}` - 新玩家总奖励（基础数值 × 欢迎人数）

### 命令前缀

- `[console]` - 以控制台执行
- `[player]` - 以玩家执行
- 无前缀 - 默认以控制台执行
