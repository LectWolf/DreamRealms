package cn.mcloli.dreamrealms.modules.shiftf;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.shiftf.config.ShiftFConfig;
import cn.mcloli.dreamrealms.modules.shiftf.listener.ShiftFListener;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.HandlerList;
import top.mrxiaom.pluginbase.func.AutoRegister;

@AutoRegister
public class ShiftFModule extends AbstractModule {

    private ShiftFConfig config;
    private ShiftFListener listener;

    public ShiftFModule(DreamRealms plugin) {
        super(plugin, "shiftf");
    }

    public static ShiftFModule inst() {
        return instanceOf(ShiftFModule.class);
    }
    
    @Override
    protected String getModuleDescription() {
        return "Shift+F 模块 - 快捷键执行命令";
    }

    public ShiftFConfig getModuleConfig() {
        return config;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            if (listener != null) {
                HandlerList.unregisterAll(listener);
                listener = null;
            }
            return;
        }

        if (config == null) {
            config = new ShiftFConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        if (listener == null) {
            listener = new ShiftFListener(this);
            registerEvents(listener);
        }

        info("模块已加载");
    }
}
