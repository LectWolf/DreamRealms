# ShiftF 快捷键模块

玩家按下 Shift+F (潜行+交换手持物品) 时执行配置的命令。

## 功能

- 监听 Shift+F 快捷键组合
- 执行配置的命令（以玩家身份）
- 支持其他插件拦截命令预处理事件

## 配置文件

- `modules/shiftf/settings.yml` - 模块配置

### 配置示例

```yaml
# 调试模式
debug: false

# Shift+F 执行的命令 (不带斜杠)
command: "trmenu open 游戏菜单"
```
