package cn.mcloli.dreamrealms.modules.timesync.command;

import cn.mcloli.dreamrealms.modules.timesync.lang.TimeSyncMessages;
import cn.mcloli.dreamrealms.command.ISubCommandHandler;
import cn.mcloli.dreamrealms.modules.timesync.TimeSyncModule;
import cn.mcloli.dreamrealms.modules.timesync.config.TimeSyncConfig;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.*;

public class TimeSyncCommand implements ISubCommandHandler {

    private final TimeSyncModule module;

    private static final List<String> SUB_COMMANDS = Lists.newArrayList("status", "toggle", "timezone", "mode", "world", "sleep");
    private static final List<String> MODE_OPTIONS = Lists.newArrayList("whitelist", "blacklist");
    private static final List<String> WORLD_ACTIONS = Lists.newArrayList("add", "remove");
    private static final List<String> ON_OFF = Lists.newArrayList("on", "off");
    private static final List<String> TIMEZONE_EXAMPLES = Lists.newArrayList("Asia/Shanghai", "America/New_York", "Europe/London", "UTC");

    public TimeSyncCommand(TimeSyncModule module) {
        this.module = module;
    }

    @Override
    public @NotNull String getName() {
        return "timesync";
    }

    @Override
    public @NotNull String[] getAliases() {
        return new String[]{"ts"};
    }

    @Override
    public @NotNull String getDescription() {
        return "时间同步模块";
    }

    @Override
    public @Nullable String getPermission() {
        return "dreamrealms.timesync";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        String sub = args[0].toLowerCase();
        TimeSyncConfig config = module.getModuleConfig();

        switch (sub) {
            case "status" -> {
                if (!module.isModuleEnabled()) {
                    return TimeSyncMessages.module_disabled.t(sender);
                }
                TimeSyncMessages.status__header.t(sender);
                TimeSyncMessages.status__timezone.t(sender, Pair.of("{timezone}", config.getTimeZone().getID()));
                TimeSyncMessages.status__mode.t(sender, Pair.of("{mode}", config.isWhitelistMode() ? "白名单" : "黑名单"));
                TimeSyncMessages.status__worlds.t(sender, Pair.of("{worlds}", String.join(", ", config.getWorlds())));
                TimeSyncMessages.status__sleep.t(sender, Pair.of("{sleep}", config.isDisableSleep() ? "是" : "否"));
                return true;
            }
            case "toggle" -> {
                if (!module.isModuleEnabled()) {
                    return TimeSyncMessages.toggle__disabled.t(sender);
                }
                module.startSyncTask();
                return TimeSyncMessages.toggle__success.t(sender);
            }
            case "timezone", "tz" -> {
                if (args.length < 2) {
                    return TimeSyncMessages.timezone__usage.t(sender);
                }
                String tzId = args[1];
                TimeZone tz = TimeZone.getTimeZone(tzId);
                if (tz.getID().equals("GMT") && !tzId.equalsIgnoreCase("GMT")) {
                    return TimeSyncMessages.timezone__invalid.t(sender, Pair.of("{timezone}", tzId));
                }
                config.setTimeZone(tzId);
                module.startSyncTask();
                return TimeSyncMessages.timezone__success.t(sender, Pair.of("{timezone}", tz.getID()));
            }
            case "mode" -> {
                if (args.length < 2) {
                    return TimeSyncMessages.mode__usage.t(sender);
                }
                String mode = args[1].toLowerCase();
                if ("whitelist".equals(mode) || "white".equals(mode)) {
                    config.setWhitelistMode(true);
                    return TimeSyncMessages.mode__whitelist.t(sender);
                } else if ("blacklist".equals(mode) || "black".equals(mode)) {
                    config.setWhitelistMode(false);
                    return TimeSyncMessages.mode__blacklist.t(sender);
                }
                return TimeSyncMessages.mode__invalid.t(sender);
            }
            case "world" -> {
                if (args.length < 3) {
                    return TimeSyncMessages.world__usage.t(sender);
                }
                String action = args[1].toLowerCase();
                String worldName = args[2];
                if ("add".equals(action)) {
                    if (Bukkit.getWorld(worldName) == null) {
                        TimeSyncMessages.world__not_exist.t(sender, Pair.of("{world}", worldName));
                    }
                    if (config.addWorld(worldName)) {
                        return TimeSyncMessages.world__add_success.t(sender, Pair.of("{world}", worldName));
                    }
                    return TimeSyncMessages.world__add_exists.t(sender, Pair.of("{world}", worldName));
                } else if ("remove".equals(action)) {
                    if (config.removeWorld(worldName)) {
                        return TimeSyncMessages.world__remove_success.t(sender, Pair.of("{world}", worldName));
                    }
                    return TimeSyncMessages.world__remove_not_found.t(sender, Pair.of("{world}", worldName));
                }
                return TimeSyncMessages.world__invalid_action.t(sender);
            }
            case "sleep" -> {
                if (args.length < 2) {
                    return TimeSyncMessages.sleep__usage.t(sender);
                }
                String value = args[1].toLowerCase();
                if ("on".equals(value) || "true".equals(value)) {
                    config.setDisableSleep(true);
                    return TimeSyncMessages.sleep__on.t(sender);
                } else if ("off".equals(value) || "false".equals(value)) {
                    config.setDisableSleep(false);
                    return TimeSyncMessages.sleep__off.t(sender);
                }
                return TimeSyncMessages.sleep__invalid.t(sender);
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
                case "mode" -> startsWith(MODE_OPTIONS, args[1]);
                case "world" -> startsWith(WORLD_ACTIONS, args[1]);
                case "sleep" -> startsWith(ON_OFF, args[1]);
                case "timezone", "tz" -> startsWith(TIMEZONE_EXAMPLES, args[1]);
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3 && "world".equals(sub)) {
            List<String> worldNames = new ArrayList<>();
            for (World w : Bukkit.getWorlds()) {
                worldNames.add(w.getName());
            }
            return startsWith(worldNames, args[2]);
        }

        return Collections.emptyList();
    }

    private boolean showHelp(CommandSender sender) {
        TimeSyncMessages.help__header.t(sender);
        TimeSyncMessages.help__status.t(sender);
        TimeSyncMessages.help__toggle.t(sender);
        TimeSyncMessages.help__timezone.t(sender);
        TimeSyncMessages.help__mode.t(sender);
        TimeSyncMessages.help__world.t(sender);
        TimeSyncMessages.help__sleep.t(sender);
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
