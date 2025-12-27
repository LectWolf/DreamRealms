package cn.mcloli.dreamrealms.modules.cosmeticmenu.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.CosmeticMenuModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class CosmeticMenuConfig {
    private final DreamRealms plugin;
    private final CosmeticMenuModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;

    public CosmeticMenuConfig(DreamRealms plugin, CosmeticMenuModule module) {
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
    }

    public boolean isDebug() {
        return debug;
    }
}
