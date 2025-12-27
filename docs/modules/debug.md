# Debug 调试模块

用于获取物品、实体、方块的序列化信息，方便开发调试。

## 功能

- 获取手持物品的 JSON 序列化信息
- 获取指向生物的序列化信息
- 获取指向方块的序列化信息

## 命令

| 命令               | 说明                   | 权限                |
| ------------------ | ---------------------- | ------------------- |
| `/dr debug item`   | 获取手持物品序列化信息 | `dreamrealms.debug` |
| `/dr debug entity` | 获取指向生物序列化信息 | `dreamrealms.debug` |
| `/dr debug block`  | 获取指向方块序列化信息 | `dreamrealms.debug` |

## 权限

| 权限                | 说明         |
| ------------------- | ------------ |
| `dreamrealms.debug` | 使用调试命令 |

## 配置文件

- `modules/debug/settings.yml` - 模块配置

### settings.yml

```yaml
# 是否开启调试日志
debug: false

# 射线追踪距离 (格)
ray-trace-distance: 10
```
