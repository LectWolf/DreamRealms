package cn.mcloli.dreamrealms.command;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.Messages;
import cn.mcloli.dreamrealms.func.AbstractModule;
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {

    private static CommandMain instance;

    private final Map<String, ISubCommandHandler> handlers = new LinkedHashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();

    public CommandMain(DreamRealms plugin) {
        super(plugin);
        instance = this;
        registerCommand("dreamrealms", this);
    }

    public static CommandMain inst() {
        return instance;
    }

    /**
     * 注册子命令处理器
     */
    public void registerHandler(ISubCommandHandler handler) {
        String name = handler.getName().toLowerCase();
        handlers.put(name, handler);
        for (String alias : handler.getAliases()) {
            aliasMap.put(alias.toLowerCase(), name);
        }
    }

    /**
     * 注销子命令处理器
     */
    public void unregisterHandler(String name) {
        String lower = name.toLowerCase();
        ISubCommandHandler handler = handlers.remove(lower);
        if (handler != null) {
            for (String alias : handler.getAliases()) {
                aliasMap.remove(alias.toLowerCase());
            }
        }
    }

    /**
     * 获取子命令处理器
     */
    @Nullable
    public ISubCommandHandler getHandler(String name) {
        String lower = name.toLowerCase();
        String realName = aliasMap.getOrDefault(lower, lower);
        return handlers.get(realName);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        String sub = args[0].toLowerCase();

        // reload 命令
        if ("reload".equals(sub) && sender.isOp()) {
            plugin.reloadConfig();
            return Messages.reload_success.t(sender);
        }

        // 查找子命令处理器
        ISubCommandHandler handler = getHandler(sub);
        if (handler != null) {
            String perm = handler.getPermission();
            if (perm != null && !sender.hasPermission(perm)) {
                return Messages.no_permission.t(sender);
            }
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return handler.execute(sender, subArgs);
        }

        return showHelp(sender);
    }

    private boolean showHelp(CommandSender sender) {
        Messages.help__header.t(sender);
        if (sender.isOp()) {
            Messages.help__reload.t(sender);
        }
        // 显示已注册的子命令
        for (ISubCommandHandler handler : handlers.values()) {
            String perm = handler.getPermission();
            if (perm != null && !sender.hasPermission(perm)) continue;
            String desc = handler.getDescription();
            if (desc.isEmpty()) {
                t(sender, "&b/dr " + handler.getName());
            } else {
                t(sender, "&b/dr " + handler.getName() + " &7- &f" + desc);
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            if (sender.isOp()) {
                result.add("reload");
            }
            // 添加已注册的子命令
            for (ISubCommandHandler handler : handlers.values()) {
                String perm = handler.getPermission();
                if (perm != null && !sender.hasPermission(perm)) continue;
                result.add(handler.getName());
                result.addAll(Arrays.asList(handler.getAliases()));
            }
            return startsWith(result, args[0]);
        }

        // 委托给子命令处理器
        String sub = args[0].toLowerCase();
        ISubCommandHandler handler = getHandler(sub);
        if (handler != null) {
            String perm = handler.getPermission();
            if (perm == null || sender.hasPermission(perm)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                List<String> result = handler.tabComplete(sender, subArgs);
                if (result != null) return result;
            }
        }

        return Collections.emptyList();
    }

    public List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }

    public List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
