package cn.mcloli.dreamrealms.modules.ownerbind.api;

import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindResult;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * OwnerBind 模块对外 API
 * <p>
 * 供其他插件调用的绑定功能接口
 * </p>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 检查物品是否已绑定
 * if (OwnerBindAPI.hasBoundOwner(item)) {
 *     String owner = OwnerBindAPI.getBoundOwner(item);
 * }
 * 
 * // 绑定物品给玩家
 * OwnerBindResult result = OwnerBindAPI.bindToPlayer(item, player.getName());
 * if (result == OwnerBindResult.SUCCESS) {
 *     // 绑定成功
 * }
 * }</pre>
 */
public final class OwnerBindAPI {

    private OwnerBindAPI() {
        // 禁止实例化
    }

    /**
     * 检查模块是否已启用
     * @return 模块是否可用
     */
    public static boolean isAvailable() {
        OwnerBindModule module = OwnerBindModule.inst();
        return module != null && module.isModuleEnabled();
    }

    // ================ 状态检查 ================

    /**
     * 检查物品是否已绑定玩家
     * @param item 物品
     * @return 是否已绑定
     */
    public static boolean hasBoundOwner(ItemStack item) {
        OwnerBindModule module = OwnerBindModule.inst();
        if (module == null) return false;
        return module.hasBoundOwner(item);
    }

    /**
     * 获取物品绑定的玩家名
     * @param item 物品
     * @return 绑定的玩家名，未绑定返回 null
     */
    public static String getBoundOwner(ItemStack item) {
        OwnerBindModule module = OwnerBindModule.inst();
        if (module == null) return null;
        return module.getBoundOwner(item);
    }

    /**
     * 检查物品是否可绑定 (未绑定但有可绑定标记)
     * @param item 物品
     * @return 是否可绑定
     */
    public static boolean isBindable(ItemStack item) {
        OwnerBindModule module = OwnerBindModule.inst();
        if (module == null) return false;
        return module.isBindable(item);
    }

    /**
     * 检查物品是否有任何绑定信息 (已绑定或可绑定)
     * @param item 物品
     * @return 是否有绑定信息
     */
    public static boolean hasAnyBindInfo(ItemStack item) {
        OwnerBindModule module = OwnerBindModule.inst();
        if (module == null) return false;
        return module.hasAnyBindInfo(item);
    }

    /**
     * 检查玩家是否是物品的主人
     * @param player 玩家
     * @param item 物品
     * @return 是否为物主 (未绑定物品返回 true)
     */
    public static boolean isOwner(Player player, ItemStack item) {
        OwnerBindModule module = OwnerBindModule.inst();
        if (module == null) return true;
        return module.isOwner(player, item);
    }

    // ================ 物品操作 ================

    /**
     * 标记物品为可绑定
     * <p>
     * 会触发 {@link OwnerBindMarkEvent} 事件
     * </p>
     * @param item 物品
     * @return 操作结果
     */
    public static OwnerBindResult markBindable(ItemStack item) {
        OwnerBindModule module = OwnerBindModule.inst();
        if (module == null) return OwnerBindResult.INVALID_ITEM;
        return module.markBindable(item, OwnerBindMarkEvent.MarkSource.API);
    }

    /**
     * 绑定物品给指定玩家
     * <p>
     * 会触发 {@link OwnerBindEvent} 事件
     * </p>
     * @param item 物品
     * @param playerName 玩家名
     * @return 操作结果
     */
    public static OwnerBindResult bindToPlayer(ItemStack item, String playerName) {
        OwnerBindModule module = OwnerBindModule.inst();
        if (module == null) return OwnerBindResult.INVALID_ITEM;
        return module.bindToPlayer(item, playerName, OwnerBindEvent.BindSource.API);
    }

    /**
     * 解除物品绑定
     * <p>
     * 会触发 {@link OwnerUnbindEvent} 事件
     * </p>
     * @param item 物品
     * @return 操作结果
     */
    public static OwnerBindResult unbind(ItemStack item) {
        OwnerBindModule module = OwnerBindModule.inst();
        if (module == null) return OwnerBindResult.INVALID_ITEM;
        return module.unbind(item, OwnerUnbindEvent.UnbindSource.API);
    }
}
