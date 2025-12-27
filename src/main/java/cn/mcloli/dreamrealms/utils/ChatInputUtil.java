package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.DreamRealms;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 聊天输入工具类
 * 支持超时、打开菜单取消、玩家退出取消
 */
public class ChatInputUtil {

    private static final Map<UUID, InputSession> sessions = new ConcurrentHashMap<>();

    /**
     * 请求玩家输入
     * @param player 玩家
     * @param prompt 提示消息
     * @param callback 输入回调 (null 表示取消)
     */
    public static void requestInput(@NotNull Player player, @NotNull String prompt, @NotNull Consumer<String> callback) {
        requestInput(player, prompt, 60, callback);
    }

    /**
     * 请求玩家输入
     * @param player 玩家
     * @param prompt 提示消息
     * @param timeoutSeconds 超时秒数
     * @param callback 输入回调 (null 表示取消)
     */
    public static void requestInput(@NotNull Player player, @NotNull String prompt, int timeoutSeconds, @NotNull Consumer<String> callback) {
        // 取消之前的会话
        cancelInput(player);

        // 发送提示
        player.sendMessage(ColorHelper.parseColor(prompt));
        player.sendMessage(ColorHelper.parseColor("&7输入 &ccancel &7取消"));

        // 创建新会话
        InputSession session = new InputSession(player, callback, timeoutSeconds);
        sessions.put(player.getUniqueId(), session);
        session.start();
    }

    /**
     * 取消玩家输入
     */
    public static void cancelInput(@NotNull Player player) {
        InputSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.cancel(false);
        }
    }

    /**
     * 检查玩家是否正在输入
     */
    public static boolean isWaitingInput(@NotNull Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    /**
     * 输入会话
     */
    private static class InputSession implements Listener {
        private final Player player;
        private final Consumer<String> callback;
        private final int timeoutSeconds;
        private Runnable timeoutCanceller;
        private boolean completed = false;

        public InputSession(Player player, Consumer<String> callback, int timeoutSeconds) {
            this.player = player;
            this.callback = callback;
            this.timeoutSeconds = timeoutSeconds;
        }

        public void start() {
            // 注册监听器
            DreamRealms.getInstance().getServer().getPluginManager()
                    .registerEvents(this, DreamRealms.getInstance());

            // 设置超时
            if (timeoutSeconds > 0) {
                timeoutCanceller = Util.runLaterCancellable(() -> {
                    if (!completed) {
                        player.sendMessage(ColorHelper.parseColor("&c输入超时"));
                        complete(null);
                    }
                }, timeoutSeconds * 20L);
            }
        }

        public void cancel(boolean notify) {
            if (completed) return;
            if (notify) {
                player.sendMessage(ColorHelper.parseColor("&c输入已取消"));
            }
            complete(null);
        }

        private void complete(@Nullable String input) {
            if (completed) return;
            completed = true;

            // 取消超时任务
            if (timeoutCanceller != null) {
                timeoutCanceller.run();
            }

            // 注销监听器
            HandlerList.unregisterAll(this);

            // 移除会话
            sessions.remove(player.getUniqueId());

            // 回调
            Util.runSync(() -> callback.accept(input));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event) {
            if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
            if (completed) return;

            event.setCancelled(true);
            String message = event.getMessage().trim();

            if (message.equalsIgnoreCase("cancel")) {
                cancel(true);
            } else {
                complete(message);
            }
        }

        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent event) {
            if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
            if (completed) return;

            // 打开菜单时取消输入
            cancel(true);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
            if (completed) return;

            complete(null);
        }
    }
}
