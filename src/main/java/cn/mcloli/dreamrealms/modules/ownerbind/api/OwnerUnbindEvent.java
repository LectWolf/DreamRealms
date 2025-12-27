package cn.mcloli.dreamrealms.modules.ownerbind.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 物品解绑事件
 * <p>
 * 当物品绑定被解除时触发
 * </p>
 */
public class OwnerUnbindEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemStack item;
    private final String previousOwner;
    private final UnbindSource source;
    private boolean cancelled = false;

    /**
     * @param item 被解绑的物品
     * @param previousOwner 之前绑定的玩家名 (可能为 null 如果只是移除可绑定标记)
     * @param source 解绑来源
     */
    public OwnerUnbindEvent(@NotNull ItemStack item, @Nullable String previousOwner, @NotNull UnbindSource source) {
        this.item = item;
        this.previousOwner = previousOwner;
        this.source = source;
    }

    /**
     * 获取被解绑的物品
     */
    @NotNull
    public ItemStack getItem() {
        return item;
    }

    /**
     * 获取之前绑定的玩家名
     * @return 玩家名，如果只是移除可绑定标记则返回 null
     */
    @Nullable
    public String getPreviousOwner() {
        return previousOwner;
    }

    /**
     * 获取解绑来源
     */
    @NotNull
    public UnbindSource getSource() {
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
     * 解绑来源
     */
    public enum UnbindSource {
        /** 命令解绑 */
        COMMAND,
        /** API 调用 */
        API,
        /** 其他/未知来源 */
        OTHER
    }
}
