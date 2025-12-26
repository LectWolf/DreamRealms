package cn.mcloli.dreamrealms.command;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.*;

/**
 * 模块命令管理器
 * 每个模块可以创建自己的命令管理器来管理子命令
 */
public class ModuleCommandManager implements CommandExecutor, TabCompleter {

    protected final DreamRealms plugin;
    protected final AbstractModule module;
    protected final Map<String, IModuleCommand> commands = new LinkedHashMap<>();
    protected final Map<String, String> aliasMap = new HashMap<>();

    public ModuleCommandManager(AbstractModule module) {
        this.plugin = DreamRealms.getInstance();
        this.module = module;
    }

    /**
     * 注册子命令
     */
    public ModuleCommandManager register(IModuleCommand command) {
        String name = command.getName().toLowerCase();
        commands.put(name, command);

        for (String alias : command.getAliases()) {
            aliasMap.put(alias.toLowerCase(), name);
        }

        return this;
    }

    /**
     * 获取命令
     */
    @Nullable
    public IModuleCommand getCommand(String name) {
        String lower = name.toLowerCase();
        String realName = aliasMap.getOrDefault(lower, lower);
        return commands.get(realName);
    }

    /**
     * 获取所有命令
     */
    public Collection<IModuleCommand> getCommands() {
        return commands.values();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // 无参数显示帮助
        if (args.length == 0) {
            return showHelp(sender);
        }

        String subName = args[0];
        IModuleCommand subCmd = getCommand(subName);

        if (subCmd == null) {
            return showHelp(sender);
        }

        // 检查权限
        String perm = subCmd.getPermission();
        if (perm != null && !sender.hasPermission(perm)) {
            sender.sendMessage(ColorHelper.parseColor("&c你没有权限执行此命令"));
            return true;
        }

        // 检查是否仅玩家
        if (subCmd.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(ColorHelper.parseColor("&c该命令只能由玩家执行"));
            return true;
        }

        // 执行命令
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCmd.execute(sender, subArgs);
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            // 补全子命令名
            List<String> result = new ArrayList<>();
            String prefix = args[0].toLowerCase();
            for (IModuleCommand subCmd : commands.values()) {
                String perm = subCmd.getPermission();
                if (perm != null && !sender.hasPermission(perm)) continue;

                if (subCmd.getName().toLowerCase().startsWith(prefix)) {
                    result.add(subCmd.getName());
                }
                for (String a : subCmd.getAliases()) {
                    if (a.toLowerCase().startsWith(prefix)) {
                        result.add(a);
                    }
                }
            }
            return result;
        }

        // 委托给子命令
        IModuleCommand subCmd = getCommand(args[0]);
        if (subCmd != null) {
            String perm = subCmd.getPermission();
            if (perm == null || sender.hasPermission(perm)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                List<String> result = subCmd.tabComplete(sender, subArgs);
                if (result != null) return result;
            }
        }

        return Collections.emptyList();
    }

    /**
     * 显示帮助
     */
    protected boolean showHelp(CommandSender sender) {
        sender.sendMessage(ColorHelper.parseColor("&6&l" + module.getModuleId().toUpperCase() + " &7- &f帮助"));
        for (IModuleCommand cmd : commands.values()) {
            String perm = cmd.getPermission();
            if (perm != null && !sender.hasPermission(perm)) continue;

            String desc = cmd.getDescription();
            if (desc.isEmpty()) {
                sender.sendMessage(ColorHelper.parseColor("&e/" + module.getModuleId() + " " + cmd.getName()));
            } else {
                sender.sendMessage(ColorHelper.parseColor("&e/" + module.getModuleId() + " " + cmd.getName() + " &7- &f" + desc));
            }
        }
        return true;
    }
}
