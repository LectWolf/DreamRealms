# OwnerBind 物主绑定模块

物品绑定后禁止转手，自动绑定给首次操作的玩家。

## 功能

- 通过 Lore 或 NBT 识别可绑定物品
- 支持传统颜色代码和 MiniMessage 格式
- 支持自定义 NBT 键值对匹配
- 丢弃、切换、拾取等操作时自动绑定
- 非物主操作时自动丢出或邮件归还
- 可选阻止 Q 键丢出绑定物品
- 可选只添加 NBT 不修改 Lore
- 死亡时绑定物品可配置保留
- 挂钩市场插件禁止上架

## 绑定识别

### Lore 识别

物品 Lore 包含配置的特征文本时视为可绑定。

支持传统颜色代码 (`&7`) 和 MiniMessage 格式 (`<gray>`)：

```yaml
bind-detection:
  lore:
    enabled: true
    patterns:
      - "&7⛓ 不可转手"
      - "<gray>成功率:"
```

### NBT 识别

默认检查 `dreamrealms:ownerbind_bindable` NBT 标记。

可配置自定义 NBT 键，支持键值对匹配：

```yaml
bind-detection:
  nbt:
    enabled: true
    custom-keys:
      - "divinity:item_module" # 只检查键是否存在
      - "divinity:item_module=gems" # 字符串匹配
      - "divinity:item_level=10" # 整数匹配
      - "myplugin:bindable=true" # 布尔值匹配
```

## 命令

| 命令                 | 说明                   | 权限                          |
| -------------------- | ---------------------- | ----------------------------- |
| `/dr ob mark`        | 标记手持物品为可绑定   | `dreamrealms.ownerbind.admin` |
| `/dr ob bind [玩家]` | 绑定手持物品给指定玩家 | `dreamrealms.ownerbind.admin` |
| `/dr ob unbind`      | 解除手持物品绑定       | `dreamrealms.ownerbind.admin` |
| `/dr ob info`        | 查看手持物品绑定信息   | 无                            |
| `/dr ob reload`      | 重载配置               | `dreamrealms.ownerbind.admin` |

## 权限

| 权限                           | 说明                                |
| ------------------------------ | ----------------------------------- |
| `dreamrealms.ownerbind.admin`  | 管理权限 (bind/unbind/mark/reload)  |
| `dreamrealms.ownerbind.bypass` | 绕过绑定检查 (可操作他人物品)       |
| `dreamrealms.ownerbind.nobind` | 免自动绑定 (物品不会自动绑定给自己) |

## 死亡掉落配置

```yaml
death-drop:
  default: DEFAULT # 默认处理方式
  worlds:
    world: KEEP # 指定世界使用 KEEP
```

| 方式      | 说明                         |
| --------- | ---------------------------- |
| `DEFAULT` | 不处理，按世界原有规则       |
| `KEEP`    | 绑定物品死亡不掉落，重生归还 |

## 软依赖

- SweetMail - 邮件归还功能
- GlobalMarketPlus - 禁止上架绑定物品
- zAuctionHouse - 禁止上架绑定物品
- QuickShop-Hikari - 禁止创建绑定物品商店

## 配置文件

- `settings.yml` - 模块配置
