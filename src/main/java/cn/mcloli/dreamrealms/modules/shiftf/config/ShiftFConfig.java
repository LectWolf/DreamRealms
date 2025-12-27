package cn.mcloli.dreamrealms.modules.shiftf.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.shiftf.ShiftFModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ShiftFConfig {

    private final DreamRealms plugin;
    private final ShiftFModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;
    private String command;

    public ShiftFConfig(DreamRealms plugin, ShiftFModule module) {
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
        command = config.getString("command", "");
    }

    public boolean isDebug() {
        return debug;
    }

    public String getCommand() {
        return command;
    }
}
