package cn.mcloli.dreamrealms.modules.wank.command;

import cn.mcloli.dreamrealms.modules.wank.WankModule;
import cn.mcloli.dreamrealms.modules.wank.lang.WankMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class WankCommand implements CommandExecutor, TabCompleter {

    private final WankModule module;

    public WankCommand(WankModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        // 管理员重置命令
        if (args.length == 1 && "reset".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("dreamrealms.wank.admin")) {
                module.resetAllWankTimes();
                WankMessages.admin_reset.t(sender);
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            return WankMessages.player_only.t(sender);
        }

        if (!module.isModuleEnabled()) {
            return WankMessages.module_disabled.t(sender);
        }

        // 执行导管
        module.startWank(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("dreamrealms.wank.admin")) {
            if ("reset".startsWith(args[0].toLowerCase())) {
                return Collections.singletonList("reset");
            }
        }
        return Collections.emptyList();
    }
}
