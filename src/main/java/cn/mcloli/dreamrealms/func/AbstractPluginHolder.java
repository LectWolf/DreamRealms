package cn.mcloli.dreamrealms.func;

import cn.mcloli.dreamrealms.DreamRealms;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<DreamRealms> {
    public AbstractPluginHolder(DreamRealms plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(DreamRealms plugin, boolean register) {
        super(plugin, register);
    }
}
