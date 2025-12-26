package cn.mcloli.dreamrealms.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 子命令处理器接口
 * 模块可以实现此接口并注册到 CommandMain
 */
public interface ISubCommandHandler {

    /**
     * 获取子命令名称
     */
    @NotNull
    String getName();

    /**
     * 获取子命令别名
     */
    @NotNull
    default String[] getAliases() {
        return new String[0];
    }

    /**
     * 获取命令描述 (用于帮助)
     */
    @NotNull
    default String getDescription() {
        return "";
    }

    /**
     * 获取命令权限
     */
    @Nullable
    default String getPermission() {
        return null;
    }

    /**
     * 执行命令
     * @param sender 发送者
     * @param args 参数 (不包含子命令名)
     * @return 是否执行成功
     */
    boolean execute(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Tab 补全
     * @param sender 发送者
     * @param args 参数 (不包含子命令名)
     * @return 补全列表
     */
    @Nullable
    default List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return null;
    }
}
