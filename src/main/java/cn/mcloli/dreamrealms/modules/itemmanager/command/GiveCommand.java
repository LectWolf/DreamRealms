package cn.mcloli.dreamrealms.modules.itemmanager.command;

import cn.mcloli.dreamrealms.command.AbstractModuleCommand;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * 给予物品命令
 */
public class GiveCommand extends AbstractModuleCommand {

    private final ItemManagerModule itemModule;

    public GiveCommand(ItemManagerModule module) {
        super(module, "give");
        this.itemModule = module;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "给予玩家物品";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkModuleEnabled(sender)) return true;

        if (args.length < 2) {
            ItemManagerMessages.cmd__give_usage.t(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            ItemManagerMessages.cmd__give_fail_player.t(sender,
                    Pair.of("{player}", args[0]));
            return true;
        }

        StoredItem storedItem = itemModule.getDatabase().getItem(args[1]);
        if (storedItem == null) {
            ItemManagerMessages.cmd__give_fail_not_found.t(sender,
                    Pair.of("{identifier}", args[1]));
            return true;
        }

        int amount = 1;
        if (args.length > 2) {
            amount = Util.parseInt(args[2]).orElse(1);
        }

        ItemStack item = storedItem.getItemStack().clone();
        item.setAmount(amount);
        Util.giveItem(target, item);

        // 发送消息给执行者
        ItemManagerMessages.cmd__give_success.t(sender,
                Pair.of("{player}", target.getName()),
                Pair.of("{amount}", String.valueOf(amount)),
                Pair.of("{item}", storedItem.getDisplayIdentifier()));

        // 如果不是静默模式且目标不是执行者，通知目标玩家
        if (!itemModule.getModuleConfig().isSilentGive() && !target.equals(sender)) {
            ItemManagerMessages.cmd__give_received.t(target,
                    Pair.of("{amount}", String.valueOf(amount)),
                    Pair.of("{item}", storedItem.getDisplayIdentifier()));
        }
        return true;
    }

    @Override
    @Nullable
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(getOnlinePlayerNames(), args[0]);
        }
        if (args.length == 2) {
            return getItemIdentifiers(args[1]);
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
