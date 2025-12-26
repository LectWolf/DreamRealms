package cn.mcloli.dreamrealms.gui;

import cn.mcloli.dreamrealms.DreamRealms;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 支持交互格子的 GUI 基类
 * @param <C> 配置类型
 */
@SuppressWarnings("unchecked")
public abstract class AbstractInteractiveGui<C extends AbstractMenuConfig<?>> implements IGui {

    protected final Player player;
    protected final C config;
    protected Inventory inventory;

    // 玩家放入的物品
    protected final Map<Integer, ItemStack> placedItems = new HashMap<>();

    public AbstractInteractiveGui(Player player, C config) {
        this.player = player;
        this.config = config;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public C getConfig() {
        return config;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = ((AbstractMenuConfig<IGui>) config).createInventory(this, player);
        ((AbstractMenuConfig<IGui>) config).applyIcons(this, inventory, player);
        return inventory;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click,
                        InventoryType.SlotType slotType, int slot,
                        ItemStack currentItem, ItemStack cursor,
                        InventoryView view, InventoryClickEvent event) {

        // 玩家背包区域 - 允许自由操作
        if (slotType == InventoryType.SlotType.CONTAINER && slot >= inventory.getSize()) {
            // 点击的是玩家背包，允许
            return;
        }
        if (slotType == InventoryType.SlotType.QUICKBAR) {
            // 点击的是快捷栏，允许
            return;
        }

        // 处理可交互格子
        if (slot >= 0 && slot < inventory.getSize() && config.isInteractiveSlot(slot)) {
            // 放入物品
            if (isPlaceAction(action)) {
                if (canPlaceItem(slot, cursor)) {
                    // 允许放入
                    DreamRealms.getInstance().getScheduler().runTaskLater(() -> {
                        ItemStack placed = view.getItem(slot);
                        placedItems.put(slot, placed);
                        onItemPlaced(slot, placed);
                    }, 1);
                    return;
                }
            }

            // 取出物品
            if (isPickupAction(action) && placedItems.containsKey(slot)) {
                ItemStack taken = placedItems.remove(slot);
                onItemTaken(slot, taken);
                return;
            }
        }

        // 默认取消事件 (GUI 区域的非交互格子)
        event.setCancelled(true);

        // 处理点击
        if (slot >= 0 && slot < inventory.getSize()) {
            Character key = config.getSlotKey(slot);
            if (key != null) {
                int index = config.getKeyIndex(key, slot);
                handleClick(click, key, index, currentItem, event);
            }
        }
    }

    /**
     * 检查是否可以放入物品
     */
    protected boolean canPlaceItem(int slot, ItemStack item) {
        return true;
    }

    /**
     * 物品放入回调
     */
    protected void onItemPlaced(int slot, ItemStack item) {
    }

    /**
     * 物品取出回调
     */
    protected void onItemTaken(int slot, ItemStack item) {
    }

    /**
     * 子类实现点击处理
     */
    protected abstract void handleClick(ClickType click, char key, int index,
                                         ItemStack currentItem, InventoryClickEvent event);

    /**
     * 获取已放入的物品
     */
    public Map<Integer, ItemStack> getPlacedItems() {
        return new HashMap<>(placedItems);
    }

    @Override
    public void onClose(InventoryView view) {
        // 返还物品给玩家
        for (ItemStack item : placedItems.values()) {
            if (item != null && !item.getType().isAir()) {
                // 尝试添加到背包，满了则掉落
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                for (ItemStack drop : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }
        placedItems.clear();
    }

    private boolean isPlaceAction(InventoryAction action) {
        return action == InventoryAction.PLACE_ALL
                || action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME;
    }

    private boolean isPickupAction(InventoryAction action) {
        return action == InventoryAction.PICKUP_ALL
                || action == InventoryAction.PICKUP_HALF
                || action == InventoryAction.PICKUP_ONE;
    }
}
