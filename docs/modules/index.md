# 模块概述

DreamRealms 采用模块化设计，每个功能独立成模块。

## 可用模块

| 模块                            | 说明                     | 状态 |
| ------------------------------- | ------------------------ | ---- |
| [TimeSync](/modules/timesync)   | 服务器时间与现实时间同步 | ✅   |
| [Welcome](/modules/welcome)     | 新玩家欢迎系统           | ✅   |
| [DogTag](/modules/dogtag)       | 死亡狗牌掉落系统         | ✅   |
| [ShiftF](/modules/shiftf)       | Shift+F 快捷键执行命令   | ✅   |
| [OwnerBind](/modules/ownerbind) | 物主绑定系统             | ✅   |
| [Debug](/modules/debug)         | 调试工具模块             | ✅   |

## 启用模块

在 `config.yml` 中设置：

```yaml
modules:
  timesync: true
```
