# 更新日志

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
