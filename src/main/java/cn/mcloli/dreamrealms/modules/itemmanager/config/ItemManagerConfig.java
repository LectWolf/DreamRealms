package cn.mcloli.dreamrealms.modules.itemmanager.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * 物品管理器配置
 */
public class ItemManagerConfig {

    private final DreamRealms plugin;
    private final ItemManagerModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;
    private boolean silentGive;

    public ItemManagerConfig(DreamRealms plugin, ItemManagerModule module) {
        this.plugin = plugin;
        this.module = module;
        this.configFile = module.getModuleConfigFile("settings.yml");
    }

    public void reload() {
        if (!configFile.exists()) {
            module.saveModuleResource("settings.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        debug = config.getBoolean("debug", false);
        silentGive = config.getBoolean("silent-give", true);
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isSilentGive() {
        return silentGive;
    }
}
