package cn.mcloli.dreamrealms.modules.welcome.listener;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.welcome.WelcomeModule;
import cn.mcloli.dreamrealms.modules.welcome.config.WelcomeConfig;
import cn.mcloli.dreamrealms.modules.welcome.data.WelcomeSession;
import cn.mcloli.dreamrealms.modules.welcome.lang.WelcomeMessages;
import cn.mcloli.dreamrealms.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class WelcomeListener implements Listener {

    private final WelcomeModule module;
    private final DreamRealms plugin;
    private final Random random = new Random();

    public WelcomeListener(WelcomeModule module) {
        this.module = module;
        this.plugin = DreamRealms.getInstance();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!module.isModuleEnabled()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 异步查询数据库判断是否为新玩家（支持多服务器）
        Util.runAsync(() -> {
            boolean isNew = module.getDatabase().isNewPlayer(uuid);
            if (isNew) {
                // 记录新玩家到数据库
                module.getDatabase().recordNewPlayer(uuid, player.getName());

                // 同步处理新玩家逻辑
                Util.runSync(() -> handleNewPlayer(player));
            }
        });
    }

    private void handleNewPlayer(Player newPlayer) {
        if (!newPlayer.isOnline()) return;

        WelcomeConfig config = module.getModuleConfig();

        // 创建欢迎会话
        module.createSession(newPlayer);

        // 向其他玩家发送欢迎文本
        sendWelcomeTextToOthers(newPlayer);

        // 设置奖励延迟任务
        if (config.isRewardEnabled()) {
            scheduleRewardTask(newPlayer.getUniqueId());
        }

        module.debug("新玩家加入: " + newPlayer.getName());
    }

    private void sendWelcomeTextToOthers(Player newPlayer) {
        WelcomeConfig config = module.getModuleConfig();
        List<String> texts = config.getWelcomeTexts();
        List<String> hovers = config.getWelcomeHover();

        if (texts.isEmpty()) return;

        // 构建悬浮文本，替换占位符
        String hoverText = hovers.stream()
                .map(line -> line
                        .replace("{player}", newPlayer.getName())
                        .replace("{welcomer_balance}", String.valueOf(config.getWelcomerBalance()))
                        .replace("{delay}", String.valueOf(config.getRewardDelayMinutes()))
                )
                .collect(Collectors.joining("\n"));

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(newPlayer)) continue;

            // 为每个玩家发送欢迎文本
            for (String text : texts) {
                String parsed = text.replace("{player}", newPlayer.getName());

                Component component = AdventureUtil.miniMessage(parsed);

                // 添加悬浮提示
                if (!hovers.isEmpty()) {
                    component = component.hoverEvent(HoverEvent.showText(
                            AdventureUtil.miniMessage(hoverText)
                    ));
                }

                // 添加点击事件 - 执行欢迎命令
                component = component.clickEvent(ClickEvent.runCommand(
                        "/dr welcome click " + newPlayer.getUniqueId()
                ));

                AdventureUtil.sendMessage(online, component);
            }
        }
    }

    private void scheduleRewardTask(UUID newPlayerUuid) {
        WelcomeConfig config = module.getModuleConfig();
        int delayTicks = config.getRewardDelayMinutes() * 60 * 20; // 分钟转tick

        WelcomeSession session = module.getSession(newPlayerUuid);
        if (session == null) return;

        var task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            distributeRewards(newPlayerUuid);
        }, delayTicks);

        session.setRewardTask(task);
        module.debug("设置奖励任务, 延迟: " + config.getRewardDelayMinutes() + " 分钟");
    }

    private void distributeRewards(UUID newPlayerUuid) {
        WelcomeSession session = module.getSession(newPlayerUuid);
        if (session == null) return;

        WelcomeConfig config = module.getModuleConfig();

        // 检查新玩家是否在线
        if (!session.isNewPlayerOnline() && !config.isRewardOnNewPlayerQuit()) {
            module.debug("新玩家已离线且配置不发奖励，跳过奖励分发");
            module.removeSession(newPlayerUuid);
            return;
        }

        int welcomerCount = session.getWelcomerCount();

        if (welcomerCount <= 0) {
            module.debug("没有人欢迎新玩家，跳过奖励分发");
            module.removeSession(newPlayerUuid);
            return;
        }

        int welcomerBalance = config.getWelcomerBalance();
        int newplayerTotalBalance = config.getNewplayerBalance() * welcomerCount;

        // 更新数据库中的欢迎人数
        Util.runAsync(() -> {
            module.getDatabase().updateWelcomeCount(newPlayerUuid, welcomerCount);
        });

        // 给新玩家发放奖励
        Player newPlayer = Bukkit.getPlayer(newPlayerUuid);
        if (newPlayer != null && newPlayer.isOnline()) {
            executeCommands(config.getNewPlayerRewardCommands(), newPlayer,
                    null, welcomerCount, welcomerBalance, newplayerTotalBalance);
            // 发送奖励消息给新玩家
            WelcomeMessages.newplayer_reward.t(newPlayer,
                    Pair.of("{count}", String.valueOf(welcomerCount)),
                    Pair.of("{balance}", String.valueOf(newplayerTotalBalance)));
        }

        // 给欢迎者发放奖励
        for (UUID welcomerUuid : session.getWelcomers()) {
            Player welcomer = Bukkit.getPlayer(welcomerUuid);
            if (welcomer != null && welcomer.isOnline()) {
                executeCommands(config.getWelcomerCommands(), welcomer,
                        session.getNewPlayerName(), welcomerCount, welcomerBalance, newplayerTotalBalance);
                // 发送奖励消息给欢迎者
                WelcomeMessages.welcomer_reward.t(welcomer,
                        Pair.of("{player}", session.getNewPlayerName()),
                        Pair.of("{balance}", String.valueOf(welcomerBalance)));
            }
        }

        module.debug("奖励分发完成, 欢迎人数: " + welcomerCount + ", 新玩家奖励: " + newplayerTotalBalance);

        // 移除会话
        module.removeSession(newPlayerUuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!module.isModuleEnabled()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 检查是否是新玩家会话
        WelcomeSession session = module.getSession(uuid);
        if (session != null) {
            session.setNewPlayerOnline(false);
            module.debug("新玩家离线: " + player.getName());
        }
    }

    /**
     * 处理玩家点击欢迎
     */
    public void handleWelcomeClick(Player welcomer, UUID newPlayerUuid) {
        WelcomeSession session = module.getSession(newPlayerUuid);
        if (session == null) {
            WelcomeMessages.not_new_player.t(welcomer);
            return;
        }

        WelcomeConfig config = module.getModuleConfig();

        // 检查是否已经欢迎过
        if (session.hasWelcomed(welcomer.getUniqueId())) {
            // 如果允许重复欢迎，只发送欢迎消息，不计入奖励
            if (config.isAllowRepeatWelcome()) {
                sendRandomWelcomeMessage(welcomer, session.getNewPlayerName());
                return;
            }
            WelcomeMessages.already_welcomed.t(welcomer,
                    Pair.of("{player}", session.getNewPlayerName()));
            return;
        }

        // 添加欢迎者
        session.addWelcomer(welcomer.getUniqueId());

        // 随机发送欢迎消息（以玩家聊天形式）
        sendRandomWelcomeMessage(welcomer, session.getNewPlayerName());

        WelcomeMessages.welcome_success.t(welcomer,
                Pair.of("{player}", session.getNewPlayerName()));

        module.debug(welcomer.getName() + " 欢迎了 " + session.getNewPlayerName());
    }

    private void sendRandomWelcomeMessage(Player welcomer, String newPlayerName) {
        WelcomeConfig config = module.getModuleConfig();
        List<String> messages = config.getWelcomeMessages();

        if (messages.isEmpty()) return;

        String message = messages.get(random.nextInt(messages.size()));
        message = message.replace("{player}", newPlayerName)
                .replace("{welcomer}", welcomer.getName());

        // 以玩家聊天形式发送
        welcomer.chat(message);
    }

    private void executeCommands(List<String> commands, Player player,
                                  String newPlayerName, int welcomerCount,
                                  int welcomerBalance, int newplayerBalance) {
        for (String cmd : commands) {
            String parsed = cmd
                    .replace("{player}", player.getName())
                    .replace("{uuid}", player.getUniqueId().toString())
                    .replace("{welcomer_count}", String.valueOf(welcomerCount))
                    .replace("{welcomer_balance}", String.valueOf(welcomerBalance))
                    .replace("{newplayer_balance}", String.valueOf(newplayerBalance));

            if (newPlayerName != null) {
                parsed = parsed.replace("{new_player}", newPlayerName);
            }

            // 判断是否为控制台命令
            if (parsed.startsWith("[console]")) {
                String consoleCmd = parsed.substring(9).trim();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCmd);
            } else if (parsed.startsWith("[player]")) {
                String playerCmd = parsed.substring(8).trim();
                player.performCommand(playerCmd);
            } else {
                // 默认以控制台执行
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
        }
    }
}
