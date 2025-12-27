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

## 可用变量

| 变量             | 说明                            |
| ---------------- | ------------------------------- |
| `{victim}`       | 死亡玩家名称                    |
| `{killer}`       | 击杀者名称（非 PVP 显示"未知"） |
| `{killer_level}` | 击杀者等级                      |
| `{weapon}`       | 击杀武器名称                    |
| `{death_time}`   | 死亡时间（按配置格式化）        |
| `{world}`        | 死亡世界名称                    |
| `{x}`            | 死亡坐标 X                      |
| `{y}`            | 死亡坐标 Y                      |
| `{z}`            | 死亡坐标 Z                      |
| `%xxx%`          | PlaceholderAPI 变量             |

## 权限

| 权限                         | 说明           |
| ---------------------------- | -------------- |
| `dreamrealms.dogtag.bypass`  | 死亡不掉落狗牌 |
| `dreamrealms.dogtag.default` | 使用默认狗牌   |
| `dreamrealms.dogtag.vip`     | 使用 VIP 狗牌  |

## 配置文件

- `modules/dogtag/settings.yml` - 模块配置
