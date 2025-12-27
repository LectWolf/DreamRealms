package cn.mcloli.dreamrealms.hook;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * ItemsAdder 插件集成
 */
public class ItemsAdderHook {

    private static boolean available = false;

    /**
     * 初始化 Hook
     */
    public static void init() {
        available = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
    }

    /**
     * 检查 ItemsAdder 是否可用
     */
    public static boolean isAvailable() {
        return available;
    }

    /**
     * 获取 ItemsAdder 物品
     * @param id 物品ID，格式: itemsadder:namespace:item_id 或 namespace:item_id
     * @return 物品，如果不可用或找不到则返回 null
     */
    @Nullable
    public static ItemStack getItem(String id) {
        return getItem(id, 1);
    }

    /**
     * 获取 ItemsAdder 物品
     * @param id 物品ID，格式: itemsadder:namespace:item_id 或 namespace:item_id
     * @param amount 数量
     * @return 物品，如果不可用或找不到则返回 null
     */
    @Nullable
    public static ItemStack getItem(String id, int amount) {
        if (!available || id == null) return null;

        try {
            String itemKey = id;
            // 移除 itemsadder: 前缀
            if (id.startsWith("itemsadder:")) {
                itemKey = id.substring("itemsadder:".length());
            }

            // 获取物品
            CustomStack customStack = CustomStack.getInstance(itemKey);
            if (customStack == null) return null;

            // 构建物品
            ItemStack item = customStack.getItemStack();
            if (item != null) {
                item.setAmount(amount);
            }
            return item;

        } catch (Throwable t) {
            return null;
        }
    }
}
