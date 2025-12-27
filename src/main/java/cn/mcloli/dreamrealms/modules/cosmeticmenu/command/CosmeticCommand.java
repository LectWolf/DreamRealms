package cn.mcloli.dreamrealms.modules.cosmeticmenu.command;

import cn.mcloli.dreamrealms.modules.cosmeticmenu.CosmeticMenuModule;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.menu.CosmeticListGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.Collections;
import java.util.List;

/**
 * 时装菜单独立命令 /cosmetic 或 /cos
 * 直接打开时装菜单，无需子命令
 */
public class CosmeticCommand implements CommandExecutor, TabCompleter {

    private final CosmeticMenuModule module;

    public CosmeticCommand(CosmeticMenuModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorHelper.parseColor("&c该命令只能由玩家执行"));
            return true;
        }

        if (!player.hasPermission("dreamrealms.cosmetic")) {
            sender.sendMessage(ColorHelper.parseColor("&c你没有权限执行此命令"));
            return true;
        }

        if (!module.isModuleEnabled()) {
            sender.sendMessage(ColorHelper.parseColor("&c该模块未启用"));
            return true;
        }

        // 直接打开时装菜单
        new CosmeticListGui(player, module.getCosmeticListMenuConfig()).open();
        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
