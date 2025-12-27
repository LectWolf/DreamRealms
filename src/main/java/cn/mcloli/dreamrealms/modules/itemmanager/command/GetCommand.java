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

import java.util.ArrayList;
import java.util.List;

/**
 * 获取物品命令
 */
public class GetCommand extends AbstractModuleCommand {

    private final ItemManagerModule itemModule;

    public GetCommand(ItemManagerModule module) {
        super(module, "get");
        this.itemModule = module;
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "获取物品";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkModuleEnabled(sender)) return true;

        Player player = getPlayer(sender);
        if (player == null) return true;

        if (args.length < 1) {
            ItemManagerMessages.cmd__get_usage.t(sender);
            return true;
        }

        StoredItem storedItem = itemModule.getDatabase().getItem(args[0]);
        if (storedItem == null) {
            ItemManagerMessages.cmd__give_fail_not_found.t(sender,
                    Pair.of("{identifier}", args[0]));
            return true;
        }

        int amount = 1;
        if (args.length > 1) {
            amount = Util.parseInt(args[1]).orElse(1);
        }

        ItemStack item = storedItem.getItemStack().clone();
        item.setAmount(amount);
        Util.giveItem(player, item);

        ItemManagerMessages.cmd__get_success.t(sender,
                Pair.of("{amount}", String.valueOf(amount)),
                Pair.of("{item}", storedItem.getDisplayIdentifier()));
        return true;
    }

    @Override
    @Nullable
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return getItemIdentifiers(args[0]);
        }
        return null;
    }

    private List<String> getItemIdentifiers(String prefix) {
        List<String> identifiers = new ArrayList<>();
        for (StoredItem item : itemModule.getDatabase().getAllItems()) {
            if (item.getIdentifier() != null) {
                identifiers.add(item.getIdentifier());
            } else {
                identifiers.add(item.getGuid().toString());
            }
        }
        return startsWith(identifiers, prefix);
    }
}
