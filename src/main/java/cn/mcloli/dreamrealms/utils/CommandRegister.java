package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.DreamRealms;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 动态命令注册工具
 * 允许在运行时注册命令，无需在 plugin.yml 中声明
 */
public class CommandRegister {

    private static SimpleCommandMap commandMap;
    private static Map<String, Command> knownCommands;
    private static final Set<String> registeredCommands = new HashSet<>();

    /**
     * 初始化命令映射
     */
    @SuppressWarnings("unchecked")
    public static void init() {
        try {
            // 获取 CraftServer 的 commandMap 字段
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());

            // 获取 knownCommands
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
        } catch (Exception e) {
            DreamRealms.getInstance().warn("无法初始化动态命令注册: " + e.getMessage());
        }
    }

    /**
     * 注册命令
     * @param name 命令名称
     * @param aliases 命令别名
     * @param description 命令描述
     * @param executor 命令执行器
     * @param tabCompleter Tab 补全器 (可选)
     * @return 是否注册成功
     */
    public static boolean register(@NotNull String name, 
                                   @NotNull String[] aliases,
                                   @NotNull String description,
                                   @NotNull CommandExecutor executor,
                                   @Nullable TabCompleter tabCompleter) {
        if (commandMap == null) {
            init();
            if (commandMap == null) return false;
        }

        // 创建命令
        DynamicCommand command = new DynamicCommand(name, description, aliases, executor, tabCompleter);

        // 注册到命令映射
        String fallbackPrefix = DreamRealms.getInstance().getName().toLowerCase();
        if (commandMap.register(fallbackPrefix, command)) {
            registeredCommands.add(name.toLowerCase());
            for (String alias : aliases) {
                registeredCommands.add(alias.toLowerCase());
            }
            return true;
        }
        return false;
    }

    /**
     * 注册命令 (使用 CommandExecutor + TabCompleter 组合)
     */
    public static boolean register(@NotNull String name,
                                   @NotNull String[] aliases,
                                   @NotNull String description,
                                   @NotNull Object handler) {
        CommandExecutor executor = handler instanceof CommandExecutor ? (CommandExecutor) handler : null;
        TabCompleter completer = handler instanceof TabCompleter ? (TabCompleter) handler : null;
        
        if (executor == null) {
            DreamRealms.getInstance().warn("命令处理器必须实现 CommandExecutor 接口");
            return false;
        }
        
        return register(name, aliases, description, executor, completer);
    }

    /**
     * 注销命令 (只注销本插件注册的命令)
     */
    public static boolean unregister(@NotNull String name) {
        if (commandMap == null || knownCommands == null) {
            init();
            if (commandMap == null || knownCommands == null) return false;
        }

        String fallbackPrefix = DreamRealms.getInstance().getName().toLowerCase();
        String prefixedName = fallbackPrefix + ":" + name.toLowerCase();
        
        // 优先查找带前缀的命令 (本插件注册的)
        Command command = knownCommands.get(prefixedName);
        
        // 如果没有带前缀的，检查无前缀的是否是本插件注册的
        if (command == null) {
            Command unprefixedCommand = knownCommands.get(name.toLowerCase());
            // 只有当命令是本插件注册的 DynamicCommand 时才注销
            if (unprefixedCommand instanceof DynamicCommand) {
                command = unprefixedCommand;
            }
        }
        
        if (command == null) return false;
        
        // 确保只注销本插件的命令
        if (!(command instanceof DynamicCommand)) {
            return false;
        }

        // 从 commandMap 注销
        command.unregister(commandMap);
        
        // 只移除本插件注册的命令键
        final String cmdName = command.getName().toLowerCase();
        knownCommands.remove(prefixedName);
        
        // 检查无前缀的命令是否指向本插件的命令，如果是则移除
        Command unprefixed = knownCommands.get(cmdName);
        if (unprefixed instanceof DynamicCommand) {
            knownCommands.remove(cmdName);
        }
        
        // 移除别名 (只移除本插件注册的)
        for (String alias : command.getAliases()) {
            String aliasLower = alias.toLowerCase();
            String prefixedAlias = fallbackPrefix + ":" + aliasLower;
            knownCommands.remove(prefixedAlias);
            
            Command aliasCmd = knownCommands.get(aliasLower);
            if (aliasCmd instanceof DynamicCommand) {
                knownCommands.remove(aliasLower);
            }
            registeredCommands.remove(aliasLower);
        }
        
        registeredCommands.remove(name.toLowerCase());
        return true;
    }

    /**
     * 检查命令是否已注册
     */
    public static boolean isRegistered(@NotNull String name) {
        return registeredCommands.contains(name.toLowerCase());
    }

    /**
     * 动态命令类
     */
    private static class DynamicCommand extends Command implements PluginIdentifiableCommand {

        private final CommandExecutor executor;
        private final TabCompleter tabCompleter;

        public DynamicCommand(@NotNull String name, 
                              @NotNull String description,
                              @NotNull String[] aliases,
                              @NotNull CommandExecutor executor,
                              @Nullable TabCompleter tabCompleter) {
            super(name);
            this.setDescription(description);
            this.setAliases(Arrays.asList(aliases));
            this.executor = executor;
            this.tabCompleter = tabCompleter;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            return executor.onCommand(sender, this, commandLabel, args);
        }

        @Override
        @NotNull
        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
            if (tabCompleter != null) {
                List<String> result = tabCompleter.onTabComplete(sender, this, alias, args);
                if (result != null) return result;
            }
            return Collections.emptyList();
        }

        @Override
        @NotNull
        public DreamRealms getPlugin() {
            return DreamRealms.getInstance();
        }
    }
}
