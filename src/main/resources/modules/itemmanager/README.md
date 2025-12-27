# ItemManager 物品管理器

存储、分类、编辑和分发物品的管理模块。

## 功能

- 物品存储 (数据库存储，支持 GUID 和自定义标识名)
- 分类管理 (创建分类、设置图标、排序)
- 物品编辑 (序列化后可编辑名称、Lore、附魔等)
- 命令分发 (give/get 命令)
- GUI 管理界面

## 命令

| 命令                                               | 说明         | 权限                      |
| -------------------------------------------------- | ------------ | ------------------------- |
| `/dr itemmanager add [标识名]`                     | 添加手持物品 | `dreamrealms.itemmanager` |
| `/dr itemmanager give <玩家> <标识名/GUID> [数量]` | 给予物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager get <标识名/GUID> [数量]`         | 获取物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager delete <标识名/GUID>`             | 删除物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager menu`                             | 打开管理界面 | `dreamrealms.itemmanager` |

## 权限

| 权限                      | 说明         |
| ------------------------- | ------------ |
| `dreamrealms.itemmanager` | 物品管理权限 |

## 配置文件

- `settings.yml` - 模块配置
- `menu/category.yml` - 分类菜单配置
- `menu/item_list.yml` - 物品列表菜单配置
- `menu/item_edit.yml` - 物品编辑菜单配置

## 存储模式

- **非序列化** (默认): 保存物品原始格式，适用于 CraftEngine/ItemsAdder 等插件物品
- **序列化**: 转为 Bukkit 序列化格式，可编辑但可能丢失特殊数据
