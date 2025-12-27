# Welcome API

欢迎模块的对外 API。

## 导入

```java
import cn.mcloli.dreamrealms.modules.welcome.api.WelcomeAPI;
import cn.mcloli.dreamrealms.modules.welcome.data.WelcomeSession;
```

## 方法

### 模块状态

```java
// 检查模块是否可用
boolean available = WelcomeAPI.isAvailable();
```

### 新玩家检查

```java
// 检查玩家是否为新玩家（首次进服）
boolean isNew = WelcomeAPI.isNewPlayer(player);
boolean isNew = WelcomeAPI.isNewPlayer(uuid);
```

### 会话管理

```java
// 检查是否有活跃的欢迎会话
boolean hasSession = WelcomeAPI.hasActiveSession(player);

// 获取欢迎会话
WelcomeSession session = WelcomeAPI.getSession(player);
if (session != null) {
    UUID newPlayerUuid = session.getNewPlayerUuid();
    String newPlayerName = session.getNewPlayerName();
    long createTime = session.getCreateTime();
}
```

### 欢迎者信息

```java
// 获取已欢迎新玩家的人数
int count = WelcomeAPI.getWelcomerCount(newPlayer);

// 获取欢迎者 UUID 集合
Set<UUID> welcomers = WelcomeAPI.getWelcomers(newPlayer);

// 检查是否已欢迎过
boolean welcomed = WelcomeAPI.hasWelcomed(welcomer, newPlayer);
```

### 历史数据

```java
// 获取玩家历史被欢迎次数（从数据库）
int historyCount = WelcomeAPI.getHistoryWelcomeCount(player);
```

## API 方法列表

| 方法                                  | 说明               |
| ------------------------------------- | ------------------ |
| `isAvailable()`                       | 检查模块是否可用   |
| `isNewPlayer(Player/UUID)`            | 检查是否为新玩家   |
| `hasActiveSession(Player/UUID)`       | 检查是否有活跃会话 |
| `getSession(Player/UUID)`             | 获取欢迎会话       |
| `getWelcomerCount(Player/UUID)`       | 获取欢迎者数量     |
| `getWelcomers(Player/UUID)`           | 获取欢迎者集合     |
| `hasWelcomed(Player, Player)`         | 检查是否已欢迎     |
| `getHistoryWelcomeCount(Player/UUID)` | 获取历史被欢迎次数 |

## WelcomeSession 数据类

```java
WelcomeSession session = WelcomeAPI.getSession(newPlayer);
if (session != null) {
    // 新玩家信息
    UUID uuid = session.getNewPlayerUuid();
    String name = session.getNewPlayerName();

    // 会话信息
    long createTime = session.getCreateTime();
    boolean online = session.isNewPlayerOnline();

    // 欢迎者信息
    int count = session.getWelcomerCount();
    Set<UUID> welcomers = session.getWelcomers();
    boolean welcomed = session.hasWelcomed(playerUuid);
}
```

## 使用示例

```java
public class MyPlugin extends JavaPlugin {

    // 检查玩家是否为新玩家并给予特殊待遇
    public void handlePlayer(Player player) {
        if (!WelcomeAPI.isAvailable()) return;

        if (WelcomeAPI.isNewPlayer(player)) {
            player.sendMessage("欢迎新玩家！");

            // 检查是否有活跃会话
            if (WelcomeAPI.hasActiveSession(player)) {
                int welcomers = WelcomeAPI.getWelcomerCount(player);
                player.sendMessage("已有 " + welcomers + " 人欢迎你！");
            }
        } else {
            // 老玩家，检查历史被欢迎次数
            int history = WelcomeAPI.getHistoryWelcomeCount(player);
            if (history > 0) {
                player.sendMessage("你曾被 " + history + " 人欢迎过！");
            }
        }
    }

    // 检查玩家是否可以欢迎新玩家
    public boolean canWelcome(Player welcomer, Player newPlayer) {
        if (!WelcomeAPI.hasActiveSession(newPlayer)) {
            return false; // 没有活跃会话
        }
        if (WelcomeAPI.hasWelcomed(welcomer, newPlayer)) {
            return false; // 已经欢迎过
        }
        return true;
    }
}
```
