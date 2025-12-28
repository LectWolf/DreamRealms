# DogTag 狗牌模块

玩家死亡时掉落狗牌物品，记录死亡信息。

## 功能

- 玩家死亡时掉落狗牌物品
- 可配置仅 PVP 死亡掉落（默认开启）
- 支持多种狗牌配置，按权限和优先级匹配
- 可自定义狗牌材质、名称、Lore、CustomModelData
- 支持绕过权限（拥有权限的玩家不掉落狗牌）
- 支持 PlaceholderAPI 变量
- 可自定义日期格式
- 支持 CraftEngine 和 ItemsAdder 物品

## 权限

| 权限                        | 说明                       |
| --------------------------- | -------------------------- |
| `dreamrealms.dogtag.bypass` | 死亡不掉落狗牌（绕过权限） |
| 自定义权限                  | 在配置中为每种狗牌设置权限 |

### 狗牌权限配置示例

```yaml
tags:
  vip:
    permission: "group.vip" # 拥有此权限的玩家掉落此狗牌
    priority: 10 # 优先级越高越先匹配
    material: "GOLD_INGOT"
  default:
    permission: "" # 空表示无权限要求（默认狗牌）
    priority: 0
    material: "IRON_INGOT"
```

## 配置文件

- `settings.yml` - 模块配置
