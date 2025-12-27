package cn.mcloli.dreamrealms.modules.itemmanager.command;

import cn.mcloli.dreamrealms.command.AbstractModuleCommand;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * 删除物品命令
 */
public class DeleteCommand extends AbstractModuleCommand {

    private final ItemManagerModule itemModule;

    public DeleteCommand(ItemManagerModule module) {
        super(module, "delete", "del");
        this.itemModule = module;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "删除物品";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkModuleEnabled(sender)) return true;

        if (args.length < 1) {
            ItemManagerMessages.cmd__delete_usage.t(sender);
            return true;
        }

        StoredItem storedItem = itemModule.getDatabase().getItem(args[0]);
        if (storedItem == null) {
            ItemManagerMessages.cmd__delete_fail_not_found.t(sender,
                    Pair.of("{identifier}", args[0]));
            return true;
        }

        itemModule.getDatabase().deleteItem(storedItem.getGuid());
        ItemManagerMessages.cmd__delete_success.t(sender,
                Pair.of("{identifier}", storedItem.getDisplayIdentifier()));
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
