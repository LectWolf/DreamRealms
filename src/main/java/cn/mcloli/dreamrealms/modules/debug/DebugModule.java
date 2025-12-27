package cn.mcloli.dreamrealms.modules.debug;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.command.CommandMain;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.debug.command.DebugCommand;
import cn.mcloli.dreamrealms.modules.debug.config.DebugConfig;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;

@AutoRegister
public class DebugModule extends AbstractModule {

    private DebugConfig config;
    private DebugCommand command;

    public DebugModule(DreamRealms plugin) {
        super(plugin, "debug");
    }

    public static DebugModule inst() {
        return instanceOf(DebugModule.class);
    }

    @Override
    protected String getModuleDescription() {
        return "调试模块 - 获取物品/实体/方块的序列化信息";
    }

    public DebugConfig getModuleConfig() {
        return config;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            return;
        }

        if (config == null) {
            config = new DebugConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        if (command == null) {
            command = new DebugCommand(this);
            CommandMain.inst().registerHandler(command);
        }

        info("模块已加载");
    }
}
