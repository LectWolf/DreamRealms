package cn.mcloli.dreamrealms.modules.welcome.api;

import cn.mcloli.dreamrealms.modules.welcome.WelcomeModule;
import cn.mcloli.dreamrealms.modules.welcome.data.WelcomeSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Welcome 模块对外 API
 * <p>
 * 供其他插件调用的欢迎功能接口
 * </p>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 检查玩家是否为新玩家
 * if (WelcomeAPI.isNewPlayer(player)) {
 *     // 新玩家逻辑
 * }
 * 
 * // 检查是否有活跃的欢迎会话
 * if (WelcomeAPI.hasActiveSession(player)) {
 *     int count = WelcomeAPI.getWelcomerCount(player);
 * }
 * }</pre>
 */
public final class WelcomeAPI {

    private WelcomeAPI() {
        // 禁止实例化
    }

    /**
     * 检查模块是否已启用
     * @return 模块是否可用
     */
    public static boolean isAvailable() {
        WelcomeModule module = WelcomeModule.inst();
        return module != null && module.isModuleEnabled();
    }

    // ================ 新玩家检查 ================

    /**
     * 检查玩家是否为新玩家（首次进服）
     * <p>
     * 通过数据库查询，未记录过的玩家视为新玩家
     * </p>
     * @param player 玩家
     * @return 是否为新玩家
     */
    public static boolean isNewPlayer(Player player) {
        return isNewPlayer(player.getUniqueId());
    }

    /**
     * 检查玩家是否为新玩家（首次进服）
     * @param uuid 玩家 UUID
     * @return 是否为新玩家
     */
    public static boolean isNewPlayer(UUID uuid) {
        WelcomeModule module = WelcomeModule.inst();
        if (module == null || module.getDatabase() == null) return false;
        return module.getDatabase().isNewPlayer(uuid);
    }

    // ================ 会话管理 ================

    /**
     * 检查玩家是否有活跃的欢迎会话
     * @param player 新玩家
     * @return 是否有活跃会话
     */
    public static boolean hasActiveSession(Player player) {
        return hasActiveSession(player.getUniqueId());
    }

    /**
     * 检查玩家是否有活跃的欢迎会话
     * @param uuid 新玩家 UUID
     * @return 是否有活跃会话
     */
    public static boolean hasActiveSession(UUID uuid) {
        WelcomeModule module = WelcomeModule.inst();
        if (module == null) return false;
        return module.getSession(uuid) != null;
    }

    /**
     * 获取欢迎会话
     * @param player 新玩家
     * @return 会话，不存在返回 null
     */
    @Nullable
    public static WelcomeSession getSession(Player player) {
        return getSession(player.getUniqueId());
    }

    /**
     * 获取欢迎会话
     * @param uuid 新玩家 UUID
     * @return 会话，不存在返回 null
     */
    @Nullable
    public static WelcomeSession getSession(UUID uuid) {
        WelcomeModule module = WelcomeModule.inst();
        if (module == null) return null;
        return module.getSession(uuid);
    }

    // ================ 欢迎者信息 ================

    /**
     * 获取已欢迎新玩家的人数
     * @param newPlayer 新玩家
     * @return 欢迎者数量，无会话返回 0
     */
    public static int getWelcomerCount(Player newPlayer) {
        return getWelcomerCount(newPlayer.getUniqueId());
    }

    /**
     * 获取已欢迎新玩家的人数
     * @param newPlayerUuid 新玩家 UUID
     * @return 欢迎者数量，无会话返回 0
     */
    public static int getWelcomerCount(UUID newPlayerUuid) {
        WelcomeSession session = getSession(newPlayerUuid);
        return session != null ? session.getWelcomerCount() : 0;
    }

    /**
     * 获取已欢迎新玩家的玩家 UUID 集合
     * @param newPlayer 新玩家
     * @return 欢迎者 UUID 集合，无会话返回 null
     */
    @Nullable
    public static Set<UUID> getWelcomers(Player newPlayer) {
        return getWelcomers(newPlayer.getUniqueId());
    }

    /**
     * 获取已欢迎新玩家的玩家 UUID 集合
     * @param newPlayerUuid 新玩家 UUID
     * @return 欢迎者 UUID 集合，无会话返回 null
     */
    @Nullable
    public static Set<UUID> getWelcomers(UUID newPlayerUuid) {
        WelcomeSession session = getSession(newPlayerUuid);
        return session != null ? session.getWelcomers() : null;
    }

    /**
     * 检查玩家是否已欢迎过指定新玩家
     * @param welcomer 欢迎者
     * @param newPlayer 新玩家
     * @return 是否已欢迎
     */
    public static boolean hasWelcomed(Player welcomer, Player newPlayer) {
        return hasWelcomed(welcomer.getUniqueId(), newPlayer.getUniqueId());
    }

    /**
     * 检查玩家是否已欢迎过指定新玩家
     * @param welcomerUuid 欢迎者 UUID
     * @param newPlayerUuid 新玩家 UUID
     * @return 是否已欢迎
     */
    public static boolean hasWelcomed(UUID welcomerUuid, UUID newPlayerUuid) {
        WelcomeSession session = getSession(newPlayerUuid);
        return session != null && session.hasWelcomed(welcomerUuid);
    }

    // ================ 历史数据 ================

    /**
     * 获取玩家历史被欢迎次数（从数据库）
     * @param player 玩家
     * @return 被欢迎次数
     */
    public static int getHistoryWelcomeCount(Player player) {
        return getHistoryWelcomeCount(player.getUniqueId());
    }

    /**
     * 获取玩家历史被欢迎次数（从数据库）
     * @param uuid 玩家 UUID
     * @return 被欢迎次数
     */
    public static int getHistoryWelcomeCount(UUID uuid) {
        WelcomeModule module = WelcomeModule.inst();
        if (module == null || module.getDatabase() == null) return 0;
        return module.getDatabase().getWelcomeCount(uuid);
    }
}
