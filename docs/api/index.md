# API 文档

DreamRealms 提供 API 供其他插件调用模块功能。

## 可用 API

| 模块                        | API 类         | 说明           |
| --------------------------- | -------------- | -------------- |
| [OwnerBind](/api/ownerbind) | `OwnerBindAPI` | 物品绑定功能   |
| [Welcome](/api/welcome)     | `WelcomeAPI`   | 新玩家欢迎功能 |

## 使用方式

1. 将 DreamRealms 添加为依赖（`plugin.yml` 中的 `depend` 或 `softdepend`）
2. 导入对应的 API 类
3. 调用静态方法

```java
// 检查模块是否可用
if (OwnerBindAPI.isAvailable()) {
    // 调用 API
}
```

## 事件监听

各模块的操作会触发对应事件，可在你的插件中监听：

```java
@EventHandler
public void onBind(OwnerBindEvent event) {
    // 处理绑定事件
}
```
