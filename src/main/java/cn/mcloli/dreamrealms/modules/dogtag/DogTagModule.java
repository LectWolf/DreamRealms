package cn.mcloli.dreamrealms.modules.dogtag;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.dogtag.config.DogTagConfig;
import cn.mcloli.dreamrealms.modules.dogtag.listener.DogTagListener;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.HandlerList;
import top.mrxiaom.pluginbase.func.AutoRegister;

@AutoRegister
public class DogTagModule extends AbstractModule {

    private DogTagConfig config;
    private DogTagListener listener;

    public DogTagModule(DreamRealms plugin) {
        super(plugin, "dogtag");
    }

    public static DogTagModule inst() {
        return instanceOf(DogTagModule.class);
    }

    public DogTagConfig getModuleConfig() {
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
            config = new DogTagConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        if (listener == null) {
            listener = new DogTagListener(this);
            registerEvents(listener);
        }

        info("模块已加载, 共 " + config.getDogTags().size() + " 个狗牌配置");
    }
}
