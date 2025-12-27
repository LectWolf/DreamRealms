# OwnerBind 物主绑定模块

物品绑定后禁止转手，自动绑定给首次操作的玩家。

## 功能

- 通过 Lore 或 NBT 识别可绑定物品
- 丢弃、切换、拾取等操作时自动绑定
- 非物主操作时自动丢出或邮件归还
- 可选阻止 Q 键丢出
- 挂钩市场插件禁止上架
- 提供 [API](/api/ownerbind) 供其他插件调用

## 绑定识别

### Lore 识别

物品 Lore 包含配置的特征文本时视为可绑定：

```yaml
bind-detection:
  lore:
    enabled: true
    patterns:
      - "&7⛓ 不可转手"
      - "&c✘ 未绑定"
```

### NBT 识别

物品包含 `dreamrealms:ownerbind_bindable` NBT 标记时视为可绑定。

## 命令

| 命令                 | 说明                   | 权限                          |
| -------------------- | ---------------------- | ----------------------------- |
| `/dr ob mark`        | 标记手持物品为可绑定   | `dreamrealms.ownerbind.admin` |
| `/dr ob bind [玩家]` | 绑定手持物品给指定玩家 | `dreamrealms.ownerbind.admin` |
| `/dr ob unbind`      | 解除手持物品绑定       | `dreamrealms.ownerbind.admin` |
| `/dr ob info`        | 查看手持物品绑定信息   | `dreamrealms.ownerbind`       |
| `/dr ob reload`      | 重载配置               | `dreamrealms.ownerbind.admin` |

## 权限

| 权限                           | 说明                               |
| ------------------------------ | ---------------------------------- |
| `dreamrealms.ownerbind`        | 基础权限 (使用命令)                |
| `dreamrealms.ownerbind.admin`  | 管理权限 (bind/unbind/mark/reload) |
| `dreamrealms.ownerbind.bypass` | 绕过绑定检查 (可操作他人物品)      |
| `dreamrealms.ownerbind.nobind` | 免自动绑定 (物品不会自动绑定)      |

## 软依赖

- **SweetMail** - 邮件归还功能
- **GlobalMarketPlus** - 禁止上架绑定物品
- **zAuctionHouse** - 禁止上架绑定物品
- **QuickShop-Hikari** - 禁止创建绑定物品商店

## 配置文件

- `modules/ownerbind/settings.yml` - 模块配置

### 配置示例

```yaml
# 绑定识别方式
bind-detection:
  lore:
    enabled: true
    patterns:
      - "&7⛓ 不可转手"
  nbt:
    enabled: true

# 绑定后显示的 Lore
lore:
  bound: "&7⛓ 已绑定: &f%player%"

# 阻止 Q 键丢出
anti-drop: false

# 阻止从容器拿取他人绑定物品
anti-container-pickup: true

# 邮件归还配置
hooks:
  sweetmail:
    enabled: false
    sender-name: "系统"
    title: "物品归还"
```
