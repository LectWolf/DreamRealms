package cn.mcloli.dreamrealms.modules.itemmanager.command;

import cn.mcloli.dreamrealms.command.ISubCommandHandler;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.CategoryGui;
import cn.mcloli.dreamrealms.utils.ItemNameUtil;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 物品管理器命令
 */
public class ItemManagerCommand implements ISubCommandHandler {

    private final ItemManagerModule module;

    public ItemManagerCommand(ItemManagerModule module) {
        this.module = module;
    }

    @Override
    @NotNull
    public String getName() {
        return "itemmanager";
    }

    @Override
    @NotNull
    public String[] getAliases() {
        return new String[]{"im"};
    }

    @Override
    @NotNull
    public String getDescription() {
        return "物品管理器";
    }

    @Override
    @Nullable
    public String getPermission() {
        return "dreamrealms.itemmanager";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!module.isModuleEnabled()) {
            return true;
        }

        if (args.length == 0) {
            ItemManagerMessages.cmd__usage.t(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        return switch (sub) {
            case "add" -> handleAdd(sender, subArgs);
            case "give" -> handleGive(sender, subArgs);
            case "get" -> handleGet(sender, subArgs);
            case "delete", "del" -> handleDelete(sender, subArgs);
            case "menu", "gui" -> handleMenu(sender);
            default -> {
                ItemManagerMessages.cmd__usage.t(sender);
                yield true;
            }
        };
    }

    /**
     * 添加物品
     */
    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

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

        module.getDatabase().saveItem(storedItem);
        ItemManagerMessages.cmd__add_success.t(sender,
                Pair.of("{identifier}", storedItem.getDisplayIdentifier()));
        return true;
    }

    /**
     * 给予物品
     */
    private boolean handleGive(CommandSender sender, String[] args) {
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

        StoredItem storedItem = module.getDatabase().getItem(args[1]);
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

        // 获取翻译后的物品名
        String itemName = ItemNameUtil.getItemName(item);

        // 发送消息给执行者
        ItemManagerMessages.cmd__give_success.t(sender,
                Pair.of("{player}", target.getName()),
                Pair.of("{amount}", String.valueOf(amount)),
                Pair.of("{item}", itemName));

        // 如果不是静默模式且目标不是执行者，通知目标玩家
        if (!module.getModuleConfig().isSilentGive() && !target.equals(sender)) {
            ItemManagerMessages.cmd__give_received.t(target,
                    Pair.of("{amount}", String.valueOf(amount)),
                    Pair.of("{item}", itemName));
        }
        return true;
    }

    /**
     * 获取物品
     */
    private boolean handleGet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length < 1) {
            ItemManagerMessages.cmd__get_usage.t(sender);
            return true;
        }

        StoredItem storedItem = module.getDatabase().getItem(args[0]);
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

        // 获取翻译后的物品名
        String itemName = ItemNameUtil.getItemName(item);

        ItemManagerMessages.cmd__get_success.t(sender,
                Pair.of("{amount}", String.valueOf(amount)),
                Pair.of("{item}", itemName));
        return true;
    }

    /**
     * 删除物品
     */
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 1) {
            ItemManagerMessages.cmd__delete_usage.t(sender);
            return true;
        }

        StoredItem storedItem = module.getDatabase().getItem(args[0]);
        if (storedItem == null) {
            ItemManagerMessages.cmd__delete_fail_not_found.t(sender,
                    Pair.of("{identifier}", args[0]));
            return true;
        }

        module.getDatabase().deleteItem(storedItem.getGuid());
        ItemManagerMessages.cmd__delete_success.t(sender,
                Pair.of("{identifier}", storedItem.getDisplayIdentifier()));
        return true;
    }

    /**
     * 打开菜单
     */
    private boolean handleMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        new CategoryGui(player, module.getCategoryMenuConfig()).open();
        return true;
    }

    @Override
    @Nullable
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(Arrays.asList("add", "give", "get", "delete", "menu"), args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give")) {
                return startsWith(getOnlinePlayerNames(), args[1]);
            }
            if (sub.equals("get") || sub.equals("delete") || sub.equals("del")) {
                return getItemIdentifiers(args[1]);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return getItemIdentifiers(args[2]);
        }

        return new ArrayList<>();
    }

    private List<String> getItemIdentifiers(String prefix) {
        List<String> identifiers = new ArrayList<>();
        for (StoredItem item : module.getDatabase().getAllItems()) {
            if (item.getIdentifier() != null) {
                identifiers.add(item.getIdentifier());
            } else {
                identifiers.add(item.getGuid().toString());
            }
        }
        return startsWith(identifiers, prefix);
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

    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }
}
