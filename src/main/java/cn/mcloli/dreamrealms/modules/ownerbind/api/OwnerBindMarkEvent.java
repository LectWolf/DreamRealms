package cn.mcloli.dreamrealms.modules.ownerbind.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 物品标记为可绑定事件
 * <p>
 * 当物品被标记为可绑定状态时触发
 * </p>
 */
public class OwnerBindMarkEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemStack item;
    private final MarkSource source;
    private boolean cancelled = false;

    /**
     * @param item 被标记的物品
     * @param source 标记来源
     */
    public OwnerBindMarkEvent(@NotNull ItemStack item, @NotNull MarkSource source) {
        this.item = item;
        this.source = source;
    }

    /**
     * 获取被标记的物品
     */
    @NotNull
    public ItemStack getItem() {
        return item;
    }

    /**
     * 获取标记来源
     */
    @NotNull
    public MarkSource getSource() {
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
     * 标记来源
     */
    public enum MarkSource {
        /** 命令标记 */
        COMMAND,
        /** API 调用 */
        API,
        /** 其他/未知来源 */
        OTHER
    }
}
