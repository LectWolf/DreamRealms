package cn.mcloli.dreamrealms.command;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 模块命令抽象基类
 */
public abstract class AbstractModuleCommand implements IModuleCommand {

    protected final DreamRealms plugin;
    protected final AbstractModule module;
    protected final String name;
    protected final String[] aliases;

    public AbstractModuleCommand(AbstractModule module, String name, String... aliases) {
        this.plugin = DreamRealms.getInstance();
        this.module = module;
        this.name = name;
        this.aliases = aliases;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public String[] getAliases() {
        return aliases;
    }

    @Override
    @Nullable
    public String getPermission() {
        return "dreamrealms." + module.getModuleId() + "." + name;
    }

    /**
     * 发送消息
     */
    protected boolean msg(CommandSender sender, String message) {
        sender.sendMessage(ColorHelper.parseColor(message));
        return true;
    }

    /**
     * 检查权限
     */
    protected boolean hasPermission(CommandSender sender) {
        String perm = getPermission();
        return perm == null || sender.hasPermission(perm);
    }

    /**
     * 检查是否为玩家
     */
    protected boolean checkPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            msg(sender, "&c该命令只能由玩家执行");
            return false;
        }
        return true;
    }

    /**
     * 检查模块是否启用
     */
    protected boolean checkModuleEnabled(CommandSender sender) {
        if (!module.isModuleEnabled()) {
            msg(sender, "&c该模块未启用");
            return false;
        }
        return true;
    }

    /**
     * 获取玩家发送者
     */
    @Nullable
    protected Player getPlayer(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }

    /**
     * 过滤以指定前缀开头的字符串
     */
    protected List<String> startsWith(Collection<String> list, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * 获取在线玩家名列表
     */
    protected List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }
}
