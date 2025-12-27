package cn.mcloli.dreamrealms.modules.debug.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.debug.DebugModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DebugConfig {

    private final DreamRealms plugin;
    private final DebugModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;
    private int rayTraceDistance;
    private boolean prettyPrint;

    public DebugConfig(DreamRealms plugin, DebugModule module) {
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
        rayTraceDistance = config.getInt("ray-trace-distance", 10);
        prettyPrint = config.getBoolean("pretty-print", false);
    }

    public boolean isDebug() {
        return debug;
    }

    public int getRayTraceDistance() {
        return rayTraceDistance;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }
}
