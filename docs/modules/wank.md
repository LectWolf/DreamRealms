# Wank 导管模块

夜间睡觉时的娱乐功能模块。

## 功能

- 玩家睡觉时随机触发导管提示
- 导管动画和音效
- 每日次数限制 (随机)
- 超过次数会爆炸死亡并全服广播
- 手持玻璃瓶可收集"牛奶"物品
- 剪刀可以"剪掉"其他玩家
- 羊驼口水射击效果
- 裤子可提供保护

## 命令

| 命令          | 说明             | 权限                     |
| ------------- | ---------------- | ------------------------ |
| `/wank`       | 执行导管         | 无                       |
| `/wank reset` | 重置所有玩家次数 | `dreamrealms.wank.admin` |

## 权限

| 权限                     | 说明     |
| ------------------------ | -------- |
| `dreamrealms.wank.admin` | 管理权限 |

## 配置文件

- `modules/wank/settings.yml` - 模块配置

### 配置示例

```yaml
# 调试模式
debug: false

# 睡觉提示
prompt:
  chance: 0.7        # 触发概率
  delay-min: 1       # 最小延迟 (秒)
  delay-max: 3       # 最大延迟 (秒)

# 每日最大次数
max-times:
  min: 3
  max: 5

# 导管持续时间 (秒)
wank-duration:
  min: 3
  max: 30

# 冷却时间 (秒)
cooldown: 30

# 牛奶物品配置
milk-item:
  material: POTION
  color: "#FFFFFF"
  name: "&b%player%的精液"
  lore:
    - "&7持续时间: %duration%秒"

# 剪刀功能
scissors:
  enabled: true
  disable-duration-ticks: 24000  # 被剪后禁用时间
  durability-cost: 1             # 耐久消耗
  cooldown: 3                    # 冷却时间 (秒)

# 裤子保护
scissors:
  pants-protection:
    enabled: true
    pants-durability-cost: 5

# 每日重置时间 (游戏时间)
reset:
  time: "06:00"
  world: "world"
```
