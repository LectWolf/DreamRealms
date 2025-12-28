package cn.mcloli.dreamrealms.modules.ownerbind.command;

import cn.mcloli.dreamrealms.command.ISubCommandHandler;
import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindResult;
import cn.mcloli.dreamrealms.modules.ownerbind.api.OwnerBindEvent;
import cn.mcloli.dreamrealms.modules.ownerbind.api.OwnerBindMarkEvent;
import cn.mcloli.dreamrealms.modules.ownerbind.api.OwnerUnbindEvent;
import cn.mcloli.dreamrealms.modules.ownerbind.lang.OwnerBindMessages;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OwnerBindCommand implements ISubCommandHandler {

    private final OwnerBindModule module;

    private static final List<String> SUB_COMMANDS = Lists.newArrayList("bind", "unbind", "mark", "info", "reload");

    public OwnerBindCommand(OwnerBindModule module) {
        this.module = module;
    }

    @Override
    public @NotNull String getName() {
        return "ownerbind";
    }

    @Override
    public @NotNull String[] getAliases() {
        return new String[]{"ob"};
    }

    @Override
    public @NotNull String getDescription() {
        return "物主绑定模块";
    }

    @Override
    public @Nullable String getPermission() {
        return "dreamrealms.ownerbind";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!module.isModuleEnabled()) {
            return OwnerBindMessages.module_disabled.t(sender);
        }

        if (args.length == 0) {
            return showHelp(sender);
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "mark" -> {
                if (!(sender instanceof Player player)) {
                    return OwnerBindMessages.player_only.t(sender);
                }
                if (!sender.hasPermission("dreamrealms.ownerbind.admin")) {
                    return OwnerBindMessages.no_permission.t(sender);
                }
                return handleMark(player);
            }
            case "bind" -> {
                if (!(sender instanceof Player player)) {
                    return OwnerBindMessages.player_only.t(sender);
                }
                if (!sender.hasPermission("dreamrealms.ownerbind.admin")) {
                    return OwnerBindMessages.no_permission.t(sender);
                }
                return handleBind(player, args);
            }
            case "unbind" -> {
                if (!(sender instanceof Player player)) {
                    return OwnerBindMessages.player_only.t(sender);
                }
                if (!sender.hasPermission("dreamrealms.ownerbind.admin")) {
                    return OwnerBindMessages.no_permission.t(sender);
                }
                return handleUnbind(player);
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    return OwnerBindMessages.player_only.t(sender);
                }
                return handleInfo(player);
            }
            case "reload" -> {
                if (!sender.hasPermission("dreamrealms.ownerbind.admin")) {
                    return OwnerBindMessages.no_permission.t(sender);
                }
                module.reloadConfig(module.plugin.getConfig());
                return OwnerBindMessages.reload_success.t(sender);
            }
            default -> {
                return showHelp(sender);
            }
        }
    }

    private boolean handleMark(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (module.isEmptyItem(item)) {
            return OwnerBindMessages.hand_empty.t(player);
        }

        if (module.hasBoundOwner(item)) {
            String owner = module.getBoundOwner(item);
            return OwnerBindMessages.already_bound.t(player, Pair.of("%owner%", owner != null ? owner : "未知"));
        }

        if (module.isBindable(item)) {
            return OwnerBindMessages.already_marked.t(player);
        }

        OwnerBindResult result = module.markBindable(item, OwnerBindMarkEvent.MarkSource.COMMAND);
        return switch (result) {
            case SUCCESS -> OwnerBindMessages.mark_success.t(player);
            default -> OwnerBindMessages.mark_failed.t(player);
        };
    }

    private boolean handleBind(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (module.isEmptyItem(item)) {
            return OwnerBindMessages.hand_empty.t(player);
        }

        // 确定绑定目标
        String targetPlayer;
        if (args.length >= 2) {
            targetPlayer = args[1];
        } else {
            targetPlayer = player.getName();
        }

        OwnerBindResult result = module.bindToPlayer(item, targetPlayer, OwnerBindEvent.BindSource.COMMAND);
        return switch (result) {
            case SUCCESS -> OwnerBindMessages.bind_success.t(player, Pair.of("%player%", targetPlayer));
            case ALREADY_BOUND -> {
                String owner = module.getBoundOwner(item);
                yield OwnerBindMessages.already_bound.t(player, Pair.of("%owner%", owner != null ? owner : "未知"));
            }
            default -> OwnerBindMessages.bind_failed.t(player);
        };
    }

    private boolean handleUnbind(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (module.isEmptyItem(item)) {
            return OwnerBindMessages.hand_empty.t(player);
        }

        OwnerBindResult result = module.unbind(item, OwnerUnbindEvent.UnbindSource.COMMAND);
        return switch (result) {
            case SUCCESS -> OwnerBindMessages.unbind_success.t(player);
            case NOT_BOUND -> OwnerBindMessages.not_bound.t(player);
            default -> OwnerBindMessages.unbind_failed.t(player);
        };
    }

    private boolean handleInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (module.isEmptyItem(item)) {
            return OwnerBindMessages.hand_empty.t(player);
        }

        String owner = module.getBoundOwner(item);
        boolean isBindable = module.isBindable(item);
        boolean hasBindableNbt = module.hasBindableNbt(item);
        boolean hasBindableLore = module.hasBindableLore(item);

        OwnerBindMessages.info__header.t(player);
        
        if (owner != null) {
            OwnerBindMessages.info__bound_owner.t(player, Pair.of("%owner%", owner));
        } else if (isBindable) {
            OwnerBindMessages.info__bindable.t(player);
            if (hasBindableNbt) {
                OwnerBindMessages.info__bindable_nbt.t(player);
            }
            if (hasBindableLore) {
                OwnerBindMessages.info__bindable_lore.t(player);
            }
        } else {
            OwnerBindMessages.info__not_bindable.t(player);
        }

        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(SUB_COMMANDS, args[0]);
        }

        String sub = args[0].toLowerCase();
        if (args.length == 2 && "bind".equals(sub)) {
            // 返回在线玩家名
            return startsWith(getOnlinePlayerNames(), args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private boolean showHelp(CommandSender sender) {
        OwnerBindMessages.help__header.t(sender);
        if (sender.hasPermission("dreamrealms.ownerbind.admin")) {
            OwnerBindMessages.help__mark.t(sender);
            OwnerBindMessages.help__bind.t(sender);
            OwnerBindMessages.help__unbind.t(sender);
        }
        OwnerBindMessages.help__info.t(sender);
        if (sender.hasPermission("dreamrealms.ownerbind.admin")) {
            OwnerBindMessages.help__reload.t(sender);
        }
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
