package cn.mcloli.dreamrealms.modules.timesync.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.timesync.TimeSyncModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class TimeSyncConfig {

    private final DreamRealms plugin;
    private final TimeSyncModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;
    private TimeZone timeZone;
    private long syncInterval;
    private boolean whitelistMode;
    private List<String> worlds;
    private boolean disableSleep;

    public TimeSyncConfig(DreamRealms plugin, TimeSyncModule module) {
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
        String tzStr = config.getString("timezone", "Asia/Shanghai");
        timeZone = TimeZone.getTimeZone(tzStr);
        syncInterval = config.getLong("sync-interval", 1200L);
        whitelistMode = config.getBoolean("whitelist-mode", true);
        worlds = new ArrayList<>(config.getStringList("worlds"));
        disableSleep = config.getBoolean("disable-sleep", true);
    }

    public void save() {
        config.set("debug", debug);
        config.set("timezone", timeZone.getID());
        config.set("sync-interval", syncInterval);
        config.set("whitelist-mode", whitelistMode);
        config.set("worlds", worlds);
        config.set("disable-sleep", disableSleep);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.warn("保存配置失败: " + e.getMessage());
        }
    }

    // Getters
    public boolean isDebug() { return debug; }
    public TimeZone getTimeZone() { return timeZone; }
    public long getSyncInterval() { return syncInterval; }
    public boolean isWhitelistMode() { return whitelistMode; }
    public List<String> getWorlds() { return worlds; }
    public boolean isDisableSleep() { return disableSleep; }

    // Setters
    public void setTimeZone(String tzId) {
        this.timeZone = TimeZone.getTimeZone(tzId);
        save();
    }

    public void setWhitelistMode(boolean whitelist) {
        this.whitelistMode = whitelist;
        save();
    }

    public void setDisableSleep(boolean disable) {
        this.disableSleep = disable;
        save();
    }

    public boolean addWorld(String world) {
        if (!worlds.contains(world)) {
            worlds.add(world);
            save();
            return true;
        }
        return false;
    }

    public boolean removeWorld(String world) {
        if (worlds.remove(world)) {
            save();
            return true;
        }
        return false;
    }
}
