package cn.mcloli.dreamrealms.hook;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * CraftEngine 插件集成
 */
public class CraftEngineHook {

    private static boolean available = false;

    /**
     * 初始化 Hook
     */
    public static void init() {
        available = Bukkit.getPluginManager().isPluginEnabled("CraftEngine");
    }

    /**
     * 检查 CraftEngine 是否可用
     */
    public static boolean isAvailable() {
        return available;
    }

    /**
     * 获取 CraftEngine 物品
     * @param id 物品ID，格式: craftengine:namespace:item_id 或 namespace:item_id
     * @return 物品，如果不可用或找不到则返回 null
     */
    @Nullable
    public static ItemStack getItem(String id) {
        return getItem(id, 1);
    }

    /**
     * 获取 CraftEngine 物品
     * @param id 物品ID，格式: craftengine:namespace:item_id 或 namespace:item_id
     * @param amount 数量
     * @return 物品，如果不可用或找不到则返回 null
     */
    @Nullable
    public static ItemStack getItem(String id, int amount) {
        if (!available || id == null) return null;

        try {
            String itemKey = id;
            // 移除 craftengine: 前缀
            if (id.startsWith("craftengine:")) {
                itemKey = id.substring("craftengine:".length());
            }

            // 获取物品
            CustomItem<ItemStack> customItem = CraftEngineItems.byId(Key.of(itemKey));
            if (customItem == null) return null;

            // 构建物品
            return customItem.buildItemStack(amount);

        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * 获取 CraftEngine 物品 (带玩家上下文)
     * @param id 物品ID，格式: craftengine:namespace:item_id 或 namespace:item_id
     * @param player 玩家 (用于某些需要玩家上下文的物品)
     * @return 物品，如果不可用或找不到则返回 null
     */
    @Nullable
    public static ItemStack getItem(String id, @Nullable Player player) {
        // 新版 API 不再需要 player 参数，直接调用数量版本
        return getItem(id, 1);
    }
}
