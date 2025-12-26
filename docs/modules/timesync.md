# TimeSync 时间同步

将服务器时间与现实时间同步。

## 功能

- 服务器时间跟随现实时区
- 支持白名单/黑名单模式选择世界
- 可禁止睡觉跳过黑夜（玩家仍可躺下，但不会跳过时间）

## 命令

| 命令                                       | 说明         | 权限                   |
| ------------------------------------------ | ------------ | ---------------------- |
| `/dr timesync status`                      | 查看状态     | `dreamrealms.timesync` |
| `/dr timesync toggle`                      | 重启同步任务 | `dreamrealms.timesync` |
| `/dr timesync timezone <时区>`             | 设置时区     | `dreamrealms.timesync` |
| `/dr timesync mode <whitelist\|blacklist>` | 切换模式     | `dreamrealms.timesync` |
| `/dr timesync world <add\|remove> <世界>`  | 管理世界     | `dreamrealms.timesync` |
| `/dr timesync sleep <on\|off>`             | 禁止睡觉开关 | `dreamrealms.timesync` |

## 配置

`modules/timesync/settings.yml`

```yaml
debug: false
timezone: Asia/Shanghai
sync-interval: 1200
whitelist-mode: true
worlds:
  - world
disable-sleep: true
```

### 配置说明

| 配置项           | 说明             | 默认值          |
| ---------------- | ---------------- | --------------- |
| `debug`          | 调试模式         | `false`         |
| `timezone`       | 时区             | `Asia/Shanghai` |
| `sync-interval`  | 同步间隔 (ticks) | `1200`          |
| `whitelist-mode` | 白名单模式       | `true`          |
| `worlds`         | 世界列表         | `[world]`       |
| `disable-sleep`  | 禁止跳过黑夜     | `true`          |
