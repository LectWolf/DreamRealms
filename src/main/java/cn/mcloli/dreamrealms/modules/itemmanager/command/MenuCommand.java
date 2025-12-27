package cn.mcloli.dreamrealms.modules.itemmanager.command;

import cn.mcloli.dreamrealms.command.AbstractModuleCommand;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.CategoryGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 打开菜单命令
 */
public class MenuCommand extends AbstractModuleCommand {

    private final ItemManagerModule itemModule;

    public MenuCommand(ItemManagerModule module) {
        super(module, "menu", "gui");
        this.itemModule = module;
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "打开物品管理菜单";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkModuleEnabled(sender)) return true;

        Player player = getPlayer(sender);
        if (player == null) return true;

        new CategoryGui(player, itemModule.getCategoryMenuConfig()).open();
        return true;
    }

    @Override
    @Nullable
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return null;
    }
}
