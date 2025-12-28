package cn.mcloli.dreamrealms.modules.wank.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.wank.WankModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WankConfig {
    private final DreamRealms plugin;
    private final WankModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;
    private double promptChance;
    private int promptDelayMin;
    private int promptDelayMax;
    private int maxTimesMin;
    private int maxTimesMax;
    private int wankDurationMin;
    private int wankDurationMax;
    private int shootDurationMin;
    private int shootDurationMax;
    private int cooldown;
    private int customModelData;
    private String milkName;
    private List<String> milkLore;
    private boolean scissorsEnabled;
    private long scissorsDisableDuration;

    public WankConfig(DreamRealms plugin, WankModule module) {
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
        promptChance = config.getDouble("prompt.chance", 0.7);
        promptDelayMin = config.getInt("prompt.delay-min", 1);
        promptDelayMax = config.getInt("prompt.delay-max", 3);
        maxTimesMin = config.getInt("max-times.min", 3);
        maxTimesMax = config.getInt("max-times.max", 5);
        wankDurationMin = config.getInt("wank-duration.min", 3);
        wankDurationMax = config.getInt("wank-duration.max", 30);
        shootDurationMin = config.getInt("shoot-duration.min", 2);
        shootDurationMax = config.getInt("shoot-duration.max", 5);
        cooldown = config.getInt("cooldown", 30);
        customModelData = config.getInt("milk-item.custom-model-data", 10001);
        milkName = config.getString("milk-item.name", "&b%player%的精液");
        milkLore = config.getStringList("milk-item.lore");
        scissorsEnabled = config.getBoolean("scissors.enabled", true);
        scissorsDisableDuration = config.getLong("scissors.disable-duration-ticks", 24000L);
    }

    public boolean isDebug() { return debug; }
    public double getPromptChance() { return promptChance; }
    public int getPromptDelayMin() { return promptDelayMin; }
    public int getPromptDelayMax() { return promptDelayMax; }
    public int getCooldown() { return cooldown; }
    public int getCustomModelData() { return customModelData; }
    public String getMilkName() { return milkName; }
    public List<String> getMilkLore() { return milkLore; }
    public boolean isScissorsEnabled() { return scissorsEnabled; }
    public long getScissorsDisableDuration() { return scissorsDisableDuration; }

    public int getRandomMaxTimes() {
        return ThreadLocalRandom.current().nextInt(maxTimesMin, maxTimesMax + 1);
    }

    public int getRandomWankDuration() {
        return ThreadLocalRandom.current().nextInt(wankDurationMin, wankDurationMax + 1);
    }

    public int getRandomShootDuration() {
        return ThreadLocalRandom.current().nextInt(shootDurationMin, shootDurationMax + 1);
    }

    public int getRandomPromptDelay() {
        return ThreadLocalRandom.current().nextInt(promptDelayMin, promptDelayMax + 1);
    }
}
