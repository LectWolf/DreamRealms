package cn.mcloli.dreamrealms.modules.welcome.command;

import cn.mcloli.dreamrealms.command.ISubCommandHandler;
import cn.mcloli.dreamrealms.modules.welcome.WelcomeModule;
import cn.mcloli.dreamrealms.modules.welcome.config.WelcomeConfig;
import cn.mcloli.dreamrealms.modules.welcome.lang.WelcomeMessages;
import cn.mcloli.dreamrealms.modules.welcome.listener.WelcomeListener;
import cn.mcloli.dreamrealms.utils.Util;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.*;

public class WelcomeCommand implements ISubCommandHandler {

    private final WelcomeModule module;

    private static final List<String> SUB_COMMANDS = Lists.newArrayList("status", "reload", "delay", "quit");
    private static final List<String> ON_OFF = Lists.newArrayList("on", "off");

    public WelcomeCommand(WelcomeModule module) {
        this.module = module;
    }

    @Override
    public @NotNull String getName() {
        return "welcome";
    }

    @Override
    public @NotNull String[] getAliases() {
        return new String[]{};
    }

    @Override
    public @NotNull String getDescription() {
        return "欢迎模块";
    }

    @Override
    public @Nullable String getPermission() {
        return "dreamrealms.welcome";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        String sub = args[0].toLowerCase();
        WelcomeConfig config = module.getModuleConfig();

        switch (sub) {
            case "click" -> {
                // 内部命令 - 处理点击欢迎
                if (!(sender instanceof Player player)) {
                    return true;
                }
                if (args.length < 2) return true;

                Optional<UUID> uuidOpt = Util.parseUUID(args[1]);
                if (uuidOpt.isEmpty()) return true;

                // 获取监听器处理点击
                WelcomeListener listener = module.getListener();
                if (listener != null) {
                    listener.handleWelcomeClick(player, uuidOpt.get());
                }
                return true;
            }
            case "status" -> {
                if (!module.isModuleEnabled()) {
                    return WelcomeMessages.module_disabled.t(sender);
                }
                WelcomeMessages.status__header.t(sender);
                WelcomeMessages.status__reward_enabled.t(sender,
                        Pair.of("{enabled}", config.isRewardEnabled() ? "开启" : "关闭"));
                WelcomeMessages.status__reward_delay.t(sender,
                        Pair.of("{delay}", String.valueOf(config.getRewardDelayMinutes())));
                WelcomeMessages.status__reward_on_quit.t(sender,
                        Pair.of("{value}", config.isRewardOnNewPlayerQuit() ? "是" : "否"));
                WelcomeMessages.status__welcomer_balance.t(sender,
                        Pair.of("{value}", String.valueOf(config.getWelcomerBalance())));
                WelcomeMessages.status__newplayer_balance.t(sender,
                        Pair.of("{value}", String.valueOf(config.getNewplayerBalance())));
                WelcomeMessages.status__active_sessions.t(sender,
                        Pair.of("{count}", String.valueOf(module.getActiveSessions().size())));
                return true;
            }
            case "reload" -> {
                module.reloadConfig(module.plugin.getConfig());
                return WelcomeMessages.reload__success.t(sender);
            }
            case "delay" -> {
                if (args.length < 2) {
                    return WelcomeMessages.delay__usage.t(sender);
                }
                Optional<Integer> minutes = Util.parseInt(args[1]);
                if (minutes.isEmpty() || minutes.get() < 0) {
                    return WelcomeMessages.delay__invalid.t(sender);
                }
                config.setRewardDelayMinutes(minutes.get());
                return WelcomeMessages.delay__success.t(sender,
                        Pair.of("{delay}", String.valueOf(minutes.get())));
            }
            case "quit" -> {
                if (args.length < 2) {
                    return WelcomeMessages.quit__usage.t(sender);
                }
                String value = args[1].toLowerCase();
                if ("on".equals(value) || "true".equals(value)) {
                    config.setRewardOnNewPlayerQuit(true);
                    return WelcomeMessages.quit__on.t(sender);
                } else if ("off".equals(value) || "false".equals(value)) {
                    config.setRewardOnNewPlayerQuit(false);
                    return WelcomeMessages.quit__off.t(sender);
                }
                return WelcomeMessages.quit__invalid.t(sender);
            }
            default -> {
                return showHelp(sender);
            }
        }
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(SUB_COMMANDS, args[0]);
        }

        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            return switch (sub) {
                case "quit" -> startsWith(ON_OFF, args[1]);
                case "delay" -> Lists.newArrayList("5", "10", "20");
                default -> Collections.emptyList();
            };
        }

        return Collections.emptyList();
    }

    private boolean showHelp(CommandSender sender) {
        WelcomeMessages.help__header.t(sender);
        WelcomeMessages.help__status.t(sender);
        WelcomeMessages.help__reload.t(sender);
        WelcomeMessages.help__delay.t(sender);
        WelcomeMessages.help__quit.t(sender);
        return true;
    }

    private List<String> startsWith(Collection<String> list, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }
}
