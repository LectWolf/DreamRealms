package cn.mcloli.dreamrealms.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * GUI 实例接口
 */
public interface IGui extends InventoryHolder {

    /**
     * 获取打开界面的玩家
     */
    Player getPlayer();

    /**
     * 创建物品栏
     */
    @NotNull
    Inventory newInventory();

    /**
     * 点击处理
     */
    void onClick(InventoryAction action, ClickType click,
                 InventoryType.SlotType slotType, int slot,
                 ItemStack currentItem, ItemStack cursor,
                 InventoryView view, InventoryClickEvent event);

    /**
     * 拖拽处理 (默认取消)
     */
    default void onDrag(InventoryView view, InventoryDragEvent event) {
        event.setCancelled(true);
    }

    /**
     * 关闭处理
     */
    default void onClose(InventoryView view) {
    }

    /**
     * 是否允许 newInventory 返回 null
     */
    default boolean allowNullInventory() {
        return false;
    }

    /**
     * 打开界面
     */
    default void open() {
        GuiManager.inst().openGui(this);
    }
}
