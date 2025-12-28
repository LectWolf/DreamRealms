# Wank 导管模块

夜间睡觉时的娱乐功能。

## 功能

- 玩家睡觉时有概率收到提示
- 执行 `/wank` 命令进行导管
- 导管时会播放动画和声音效果
- 射精时发射羊驼口水（可配置伤害）
- 手持玻璃瓶可以收集"精液"药水
- 每日导管次数有限制，超过会爆炸
- 剪刀可剪掉其他玩家的牛牛（会掉落物品）

## 命令

| 命令          | 说明             | 权限                     |
| ------------- | ---------------- | ------------------------ |
| `/wank`       | 进行导管         | 无                       |
| `/wank reset` | 重置所有玩家次数 | `dreamrealms.wank.admin` |

## 权限

| 权限                     | 说明                   |
| ------------------------ | ---------------------- |
| `dreamrealms.wank.admin` | 管理权限（重置次数等） |

## 配置文件

- `settings.yml` - 模块配置

### 精液药水配置

```yaml
milk-item:
  material: POTION # 物品材质
  custom-model-data: 10001 # 自定义模型数据
  name: "&b%player%的精液" # 名称
  lore: # 描述
    - "&7新鲜出炉"
  nbt:
    enabled: true # 是否添加 NBT 标记
    key: "dreamrealms_milk" # NBT 键名
```

### 剪刀功能配置

```yaml
scissors:
  enabled: true # 是否启用
  disable-duration-ticks: 24000 # 被剪后禁用时间
  drop:
    enabled: true # 是否掉落物品
    material: PORKCHOP # 掉落物材质
    name: "&c%player%的牛牛" # 掉落物名称
    owner-pickup-delay: 5 # 原主人捡起延迟(秒)
    nbt:
      enabled: true # 是否添加 NBT 标记
      key: "dreamrealms_cut" # NBT 键名
```

### 射精效果配置

```yaml
shoot-particle:
  speed-min: 0.3 # 口水速度最小值
  speed-max: 0.8 # 口水速度最大值
  damage-enabled: false # 是否造成伤害
  damage: 0 # 伤害值 (0=只有受击效果)
```
