package cn.mcloli.dreamrealms.modules.itemmanager.command;

import cn.mcloli.dreamrealms.command.AbstractModuleCommand;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

/**
 * 添加物品命令
 */
public class AddCommand extends AbstractModuleCommand {

    private final ItemManagerModule itemModule;

    public AddCommand(ItemManagerModule module) {
        super(module, "add");
        this.itemModule = module;
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "添加手持物品到管理器";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkModuleEnabled(sender)) return true;

        Player player = getPlayer(sender);
        if (player == null) return true;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (Util.isAir(item)) {
            ItemManagerMessages.cmd__add_fail_empty.t(sender);
            return true;
        }

        StoredItem storedItem = StoredItem.create(item);

        // 如果提供了标识名
        if (args.length > 0) {
            storedItem.setIdentifier(args[0]);
        }

        itemModule.getDatabase().saveItem(storedItem);
        ItemManagerMessages.cmd__add_success.t(sender,
                Pair.of("{identifier}", storedItem.getDisplayIdentifier()));
        return true;
    }

    @Override
    @Nullable
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return null;
    }
}
