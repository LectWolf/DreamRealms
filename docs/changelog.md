# 更新日志

## v1.0.8

### 新增

- Wank 剪刀功能 (裤子保护、掉落物配置)
- Wank 羊驼口水射击伤害
- Util.runAtEntityTimer() 实体定时器

### 修改

- OwnerBind 市场插件监听器重构

### 删除

- GiftPoints 点券赠送模块

### 修复

- 修复动态命令注册在插件重载后失效的问题

## v1.0.7

### 新增

- CosmeticMenu 时装菜单模块
  - 展示玩家拥有的 HMCCosmetics 时装
  - 7 个分类 (头部/胸部/腿部/脚部/背包/副手/气球)
  - 已装备时装附魔闪烁标识
  - 分类按钮显示拥有数量
  - 当前分类使用独立图标标识
  - 独立命令 `/cos`

### 修复

- 修复 CommandRegister.unregister 误删其他插件同名命令的问题

## v1.0.6

### 新增

- ItemManager 物品管理器模块
  - 物品存储 (GUID + 自定义标识名)
  - 分类管理 (创建/删除/排序/图标设置)
  - 物品列表 GUI (添加/删除/排序/获取)
  - 物品编辑 (名称/Lore/附魔/属性/Flag/耐久/材质)
  - 食物组件编辑 (Paper 服务端完整支持)
  - 聊天输入工具类 ChatInputUtil
  - 动态命令注册 (/itemmanager, /im)
- Debug 调试模块
  - 获取物品/实体/方块的 JSON 序列化信息
  - 可配置射线追踪距离和格式化输出
- OwnerBind 死亡掉落处理配置
  - 支持按世界设置处理方式 (DEFAULT/KEEP/DROP/DESTROY)
- EntityNameUtil 实体名称工具类
- PaperUtil 工具类 (运行时检测 Paper API)

### 修改

- OwnerBind 支持 MiniMessage 格式、自定义 NBT 键列表、NBT 键值对匹配
- DogTag 狗牌模块配置完善 (空手击杀名称、未知击杀者名称)
- 性能优化 (预编译正则、快速路径检查、异步下载)
- 统一翻页按钮位置到第六行位置 2 和 8
- 删除操作改为 Ctrl+Q

### 修复

- 修复分类有物品时可删除的问题
- 修复物品属性菜单复杂组件图标不显示
- 修复分类选择菜单返回按钮不显示
- 修复动态命令重载问题
- 修复多个 ItemManager 菜单问题

## v1.0.5

### 新增

- OwnerBind API 和事件系统
  - `OwnerBindAPI` 对外 API 类
  - `OwnerBindEvent` / `OwnerBindMarkEvent` / `OwnerUnbindEvent` 事件
  - 支持其他插件监听和取消绑定操作
- Welcome API
  - `WelcomeAPI` 对外 API 类
  - 查询新玩家状态、会话信息、欢迎者数据
- OwnerBind 挂钩 QuickShop-Hikari
  - 禁止使用绑定物品创建箱子商店
- API 文档目录 (`docs/api/`)
- 导航栏 API 菜单

### 修复

- 移除 Vault 硬依赖，插件不再因缺少经济接口而卸载

## v1.0.4

### 新增

- OwnerBind 物主绑定模块
  - 通过 Lore 或 NBT 识别可绑定物品
  - 自动绑定功能 (丢弃/切换/拾取时)
  - 非物主操作时自动丢出或邮件归还
  - 阻止 Q 键丢出绑定物品 (可配置)
  - 阻止从容器拿取他人绑定物品 (可配置)
  - 挂钩 SweetMail 邮件归还
  - 挂钩 GlobalMarketPlus/zAuctionHouse 禁止上架
  - 命令: mark/bind/unbind/info/reload
  - 权限: bypass/nobind/admin

## v1.0.3

### 新增

- ShiftF 快捷键模块
  - Shift+F 快捷键执行配置命令
- DogTag 狗牌模块
  - 玩家死亡时掉落狗牌
  - 支持仅 PVP 死亡掉落
  - 多狗牌配置，按权限和优先级匹配
  - 支持 CraftEngine/ItemsAdder 物品
  - 支持 PAPI 变量
- ItemNameUtil 物品名称工具类
  - 支持 CraftEngine/ItemsAdder/原版物品名称获取
- CraftEngine 翻译文件自动加载
- 模块添加到 config.yml 时自动添加描述注释

### 修改

- config.yml 结构优化 (item-language 子配置)
- 所有模块支持 Folia

### 修复

- 修复狗牌掉落物飞太远的问题
- 修复 keepInventory 下狗牌不掉落的问题

## v1.0.2

### 新增

- Welcome 欢迎模块
  - 新玩家进服可点击欢迎文本
  - 延迟奖励机制（默认 5 分钟）
  - 欢迎者和新玩家双向奖励
  - 数据库记录支持多服务器
  - 可配置重复欢迎
- AbstractModuleDatabase 基类自动初始化数据表
- GitHub Release 使用 tag message 作为版本说明

### 修改

- 构建输出移除 `-all` 后缀

### 修复

- 修复数据库连接未释放导致的超时问题

## v1.0.0

### 新增

- TimeSync 时间同步模块
  - 服务器时间与现实时间同步
  - 白名单/黑名单世界模式
  - 禁止睡觉功能
- 模块化架构
  - 模块开关自动写入配置
  - 模块语言枚举独立
- VitePress 文档站点
- GitHub Actions 自动构建
