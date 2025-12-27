package cn.mcloli.dreamrealms.modules.ownerbind.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 物品绑定事件
 * <p>
 * 当物品被绑定给玩家时触发
 * </p>
 */
public class OwnerBindEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemStack item;
    private final String playerName;
    private final BindSource source;
    private boolean cancelled = false;

    /**
     * @param item 被绑定的物品
     * @param playerName 绑定目标玩家名
     * @param source 绑定来源
     */
    public OwnerBindEvent(@NotNull ItemStack item, @NotNull String playerName, @NotNull BindSource source) {
        this.item = item;
        this.playerName = playerName;
        this.source = source;
    }

    /**
     * 获取被绑定的物品
     */
    @NotNull
    public ItemStack getItem() {
        return item;
    }

    /**
     * 获取绑定目标玩家名
     */
    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    /**
     * 获取绑定来源
     */
    @NotNull
    public BindSource getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * 绑定来源
     */
    public enum BindSource {
        /** 玩家拾取物品自动绑定 */
        PICKUP,
        /** 玩家手持物品自动绑定 */
        HOLD,
        /** 玩家交互物品自动绑定 */
        INTERACT,
        /** 背包点击自动绑定 */
        INVENTORY,
        /** 命令绑定 */
        COMMAND,
        /** API 调用 */
        API,
        /** 其他/未知来源 */
        OTHER
    }
}
