# ItemManager 物品管理器

存储、分类、编辑和分发物品的管理模块。

## 功能

- 物品存储 (数据库存储，支持 GUID 和自定义标识名)
- 分类管理 (创建分类、设置图标、排序)
- 物品编辑 (名称、Lore、附魔、属性、Flag、耐久)
- 支持 MiniMessage 格式输入
- 命令分发 (give/get 命令)
- GUI 管理界面

## 命令

### 主命令

| 命令                                               | 说明         | 权限                      |
| -------------------------------------------------- | ------------ | ------------------------- |
| `/dr itemmanager add [标识名]`                     | 添加手持物品 | `dreamrealms.itemmanager` |
| `/dr itemmanager give <玩家> <标识名/GUID> [数量]` | 给予物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager get <标识名/GUID> [数量]`         | 获取物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager delete <标识名/GUID>`             | 删除物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager menu`                             | 打开管理界面 | `dreamrealms.itemmanager` |

### 独立命令

| 命令                                   | 说明         |
| -------------------------------------- | ------------ |
| `/itemmanager` 或 `/im`                | 打开管理界面 |
| `/im give <玩家> <标识名/GUID> [数量]` | 给予物品     |
| `/im get <标识名/GUID> [数量]`         | 获取物品     |

## 权限

| 权限                      | 说明         |
| ------------------------- | ------------ |
| `dreamrealms.itemmanager` | 物品管理权限 |

## GUI 功能

### 分类管理

- 创建/删除分类
- 设置分类图标 (支持 CraftEngine/ItemsAdder)
- 分类排序 (Shift+左键/右键)
- 空手点击分类图标修改名称

### 物品列表

- 添加物品 (拖放或点击添加)
- 删除物品 (Ctrl+Q)
- 物品排序 (Shift+左键/右键)
- 左键编辑，右键获取

### 物品编辑

- 修改名称 (支持 MiniMessage)
- 修改 Lore (支持 MiniMessage，可添加空行)
- 修改附魔 (分类选择，等级调整)
- 修改属性 (数值、操作类型、槽位)
- 修改 Flag
- 修改耐久
- 序列化物品 (转为可编辑格式)
- 查看获取命令 (点击复制)

### 附魔选择

- 按分类浏览附魔
- 支持自定义分类配置
- Debug 模式输出所有附魔键名

### 属性编辑

- 添加/删除属性
- 修改数值
- 切换操作类型 (加法/百分比/乘法)
- 切换装备槽位

## 配置文件

- `settings.yml` - 模块配置
- `menu/category.yml` - 分类菜单配置
- `menu/item_list.yml` - 物品列表菜单配置
- `menu/item_edit.yml` - 物品编辑菜单配置
- `menu/lore_edit.yml` - Lore 编辑菜单配置
- `menu/enchant_edit.yml` - 附魔编辑菜单配置
- `menu/enchant_select.yml` - 附魔选择菜单配置
- `menu/attribute_edit.yml` - 属性编辑菜单配置
- `menu/attribute_select.yml` - 属性选择菜单配置
- `menu/attribute_detail.yml` - 属性详情菜单配置

## 存储模式

- **非序列化** (默认): 保存物品原始格式，适用于 CraftEngine/ItemsAdder 等插件物品
- **序列化**: 转为 Bukkit 序列化格式，可编辑但可能丢失特殊数据

## 配置示例

```yaml
# 调试模式
debug: false

# 静默给予 (不显示获取消息)
silent-give: false
```
