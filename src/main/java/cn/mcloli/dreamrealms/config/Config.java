package cn.mcloli.dreamrealms.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractPluginHolder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.HashSet;
import java.util.Set;

/**
 * 全局配置
 */
@AutoRegister(priority = 0)
public class Config extends AbstractPluginHolder {

    // 启用的模块
    private final Set<String> enabledModules = new HashSet<>();

    public Config(DreamRealms plugin) {
        super(plugin, true);
    }

    @Override
    public int priority() {
        return 0; // 最先加载
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        enabledModules.clear();

        // 加载模块开关
        ConfigurationSection modulesSection = config.getConfigurationSection("modules");
        if (modulesSection != null) {
            for (String key : modulesSection.getKeys(false)) {
                if (modulesSection.getBoolean(key, true)) {
                    enabledModules.add(key.toLowerCase());
                }
            }
        }
    }

    /**
     * 检查模块是否启用
     */
    public boolean isModuleEnabled(String moduleId) {
        return enabledModules.contains(moduleId.toLowerCase());
    }

    /**
     * 获取所有启用的模块
     */
    public Set<String> getEnabledModules() {
        return new HashSet<>(enabledModules);
    }

    public static Config inst() {
        return instanceOf(Config.class);
    }
}
