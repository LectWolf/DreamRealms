# TimeSync 时间同步模块

将游戏世界时间同步到现实时区时间。

## 功能

- 同步游戏时间到指定时区
- 支持白名单/黑名单模式选择世界
- 可禁止玩家在同步世界睡觉

## 命令

| 命令                                       | 说明         | 权限                   |
| ------------------------------------------ | ------------ | ---------------------- |
| `/dr timesync status`                      | 查看当前状态 | `dreamrealms.timesync` |
| `/dr timesync toggle`                      | 重启同步任务 | `dreamrealms.timesync` |
| `/dr timesync timezone <时区>`             | 设置时区     | `dreamrealms.timesync` |
| `/dr timesync mode <whitelist\|blacklist>` | 切换模式     | `dreamrealms.timesync` |
| `/dr timesync world <add\|remove> <世界>`  | 管理世界列表 | `dreamrealms.timesync` |
| `/dr timesync sleep <on\|off>`             | 禁止睡觉开关 | `dreamrealms.timesync` |

## 权限

| 权限                   | 说明                     |
| ---------------------- | ------------------------ |
| `dreamrealms.timesync` | 使用所有时间同步管理命令 |

## 配置文件

- `settings.yml` - 模块配置
- `lang.yml` - 语言文件
