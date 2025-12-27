# CosmeticMenu 时装菜单

展示玩家拥有的 HMCCosmetics 时装，支持分类浏览和快速穿戴。

## 功能

- 7 个时装分类 (头部/胸部/腿部/脚部/背包/副手/气球)
- 已装备时装附魔闪烁标识
- 分页浏览
- 一键卸下当前分类时装
- 分类按钮显示拥有数量

## 依赖

- [HMCCosmetics](https://www.spigotmc.org/resources/hmccosmetics.100107/) (必需)

## 命令

| 命令   | 说明         | 权限                   |
| ------ | ------------ | ---------------------- |
| `/cos` | 打开时装菜单 | `dreamrealms.cosmetic` |

## 权限

| 权限                   | 说明         |
| ---------------------- | ------------ |
| `dreamrealms.cosmetic` | 使用时装菜单 |

## 配置文件

- `settings.yml` - 模块配置
- `menu/cosmetic_list.yml` - 菜单配置

## 菜单布局

```
IIIIIIIII  (4行时装展示)
IIIIIIIII
IIIIIIIII
IIIIIIIII
XXXXPXXXX  (X分隔栏, P卸下时装)
<ABCDEFG>  (<>翻页, A-G分类按钮)
```

### 分类按钮

| 按钮 | 分类 |
| ---- | ---- |
| A    | 头部 |
| B    | 胸部 |
| C    | 腿部 |
| D    | 脚部 |
| E    | 背包 |
| F    | 副手 |
| G    | 气球 |
