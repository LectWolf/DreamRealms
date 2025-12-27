# OwnerBind API

物主绑定模块的对外 API。

## 导入

```java
import cn.mcloli.dreamrealms.modules.ownerbind.api.OwnerBindAPI;
import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindResult;
```

## 方法

### 状态检查

```java
// 检查模块是否可用
boolean available = OwnerBindAPI.isAvailable();

// 检查物品是否已绑定
boolean bound = OwnerBindAPI.hasBoundOwner(item);

// 获取绑定的玩家名 (未绑定返回 null)
String owner = OwnerBindAPI.getBoundOwner(item);

// 检查物品是否可绑定 (有标记但未绑定)
boolean bindable = OwnerBindAPI.isBindable(item);

// 检查是否有任何绑定信息 (已绑定或可绑定)
boolean hasInfo = OwnerBindAPI.hasAnyBindInfo(item);

// 检查玩家是否为物主 (未绑定物品返回 true)
boolean isOwner = OwnerBindAPI.isOwner(player, item);
```

### 物品操作

```java
// 标记物品为可绑定
OwnerBindResult result = OwnerBindAPI.markBindable(item);

// 绑定物品给指定玩家
OwnerBindResult result = OwnerBindAPI.bindToPlayer(item, "PlayerName");

// 解除物品绑定
OwnerBindResult result = OwnerBindAPI.unbind(item);
```

### 返回值

`OwnerBindResult` 枚举：

| 值              | 说明                 |
| --------------- | -------------------- |
| `SUCCESS`       | 操作成功             |
| `EMPTY_ITEM`    | 物品为空             |
| `INVALID_ITEM`  | 无效物品或操作被取消 |
| `ALREADY_BOUND` | 物品已绑定           |
| `NOT_BOUND`     | 物品未绑定           |

## 事件

所有操作都会触发对应事件，可被监听和取消。

### OwnerBindEvent

物品绑定给玩家时触发。

```java
@EventHandler
public void onBind(OwnerBindEvent event) {
    ItemStack item = event.getItem();
    String playerName = event.getPlayerName();
    OwnerBindEvent.BindSource source = event.getSource();

    // 取消绑定
    event.setCancelled(true);
}
```

**BindSource 来源：**

| 值          | 说明     |
| ----------- | -------- |
| `PICKUP`    | 拾取物品 |
| `HOLD`      | 手持物品 |
| `INTERACT`  | 交互物品 |
| `INVENTORY` | 背包点击 |
| `COMMAND`   | 命令绑定 |
| `API`       | API 调用 |
| `OTHER`     | 其他来源 |

### OwnerBindMarkEvent

物品标记为可绑定时触发。

```java
@EventHandler
public void onMark(OwnerBindMarkEvent event) {
    ItemStack item = event.getItem();
    OwnerBindMarkEvent.MarkSource source = event.getSource();

    event.setCancelled(true);
}
```

**MarkSource 来源：** `COMMAND` / `API` / `OTHER`

### OwnerUnbindEvent

物品解除绑定时触发。

```java
@EventHandler
public void onUnbind(OwnerUnbindEvent event) {
    ItemStack item = event.getItem();
    String previousOwner = event.getPreviousOwner(); // 可能为 null
    OwnerUnbindEvent.UnbindSource source = event.getSource();

    event.setCancelled(true);
}
```

**UnbindSource 来源：** `COMMAND` / `API` / `OTHER`

## 使用示例

```java
public class MyPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    // 给玩家物品添加绑定
    public void bindPlayerItem(Player player, ItemStack item) {
        if (!OwnerBindAPI.isAvailable()) {
            player.sendMessage("OwnerBind 模块未启用");
            return;
        }

        OwnerBindResult result = OwnerBindAPI.bindToPlayer(item, player.getName());
        if (result == OwnerBindResult.SUCCESS) {
            player.sendMessage("物品已绑定");
        } else if (result == OwnerBindResult.ALREADY_BOUND) {
            player.sendMessage("物品已被绑定给: " + OwnerBindAPI.getBoundOwner(item));
        }
    }

    // 监听绑定事件
    @EventHandler
    public void onBind(OwnerBindEvent event) {
        // 禁止 API 调用绑定某些物品
        if (event.getSource() == OwnerBindEvent.BindSource.API) {
            if (isSpecialItem(event.getItem())) {
                event.setCancelled(true);
            }
        }
    }
}
```
