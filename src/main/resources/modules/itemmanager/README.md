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

| 命令                                               | 说明         | 权限                      |
| -------------------------------------------------- | ------------ | ------------------------- |
| `/dr itemmanager add [标识名]`                     | 添加手持物品 | `dreamrealms.itemmanager` |
| `/dr itemmanager give <玩家> <标识名/GUID> [数量]` | 给予物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager get <标识名/GUID> [数量]`         | 获取物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager delete <标识名/GUID>`             | 删除物品     | `dreamrealms.itemmanager` |
| `/dr itemmanager menu`                             | 打开管理界面 | `dreamrealms.itemmanager` |
| `/itemmanager` 或 `/im`                            | 打开管理界面 | `dreamrealms.itemmanager` |

## 权限

| 权限                      | 说明                   |
| ------------------------- | ---------------------- |
| `dreamrealms.itemmanager` | 物品管理权限（全功能） |

## GUI 功能

- 分类管理 (创建/删除/排序/设置图标)
- 物品列表 (添加/删除/排序/获取)
- 物品编辑 (名称/Lore/附魔/属性/Flag/耐久)
- 附魔选择 (分类浏览)
- 属性编辑 (数值/操作类型/槽位)

## 配置文件

- `settings.yml` - 模块配置
- `menu/*.yml` - 菜单配置

## 存储模式

- 非序列化 (默认): 保存物品原始格式
- 序列化: 转为可编辑格式
