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
    private int handSwingInterval;
    private float soundVolume;
    private String milkMaterial;
    private Integer customModelData;
    private String milkColor;
    private String milkName;
    private List<String> milkLore;
    private boolean milkNbtEnabled;
    private String milkNbtKey;
    private boolean scissorsEnabled;
    private long scissorsDisableDuration;
    private int scissorsDurabilityCost;
    private int scissorsCooldown;
    private int scissorsRecoveryInterval;
    private boolean scissorsHitEffectEnabled;
    private double scissorsHitDamage;
    private boolean scissorsCutDamageEnabled;
    private double scissorsCutDamagePercent;
    private boolean pantProtectionEnabled;
    private int pantsDurabilityCost;
    private java.util.Map<String, Integer> pantsCutsRequired;
    private java.util.Map<String, Integer> enchantmentBonus;
    private boolean scissorsDropEnabled;
    private String scissorsDropMaterial;
    private Integer scissorsDropCustomModelData;
    private String scissorsDropName;
    private List<String> scissorsDropLore;
    private int scissorsOwnerPickupDelay;
    private boolean scissorsDropNbtEnabled;
    private String scissorsDropNbtKey;
    private double shootSpeedMin;
    private double shootSpeedMax;
    private boolean shootDamageEnabled;
    private double shootDamage;
    private String resetTime;
    private String resetWorld;

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
        handSwingInterval = config.getInt("wank-animation.hand-swing-interval", 3);
        soundVolume = (float) config.getDouble("wank-animation.sound-volume", 0.3);
        milkMaterial = config.getString("milk-item.material", "POTION");
        customModelData = config.contains("milk-item.custom-model-data") ? config.getInt("milk-item.custom-model-data") : null;
        milkColor = config.getString("milk-item.color", null);
        milkName = config.getString("milk-item.name", "&b%player%的精液");
        milkLore = config.getStringList("milk-item.lore");
        milkNbtEnabled = config.getBoolean("milk-item.nbt.enabled", true);
        milkNbtKey = config.getString("milk-item.nbt.key", "dreamrealms_milk");
        scissorsEnabled = config.getBoolean("scissors.enabled", true);
        scissorsDisableDuration = config.getLong("scissors.disable-duration-ticks", 24000L);
        scissorsDurabilityCost = config.getInt("scissors.durability-cost", 1);
        scissorsCooldown = config.getInt("scissors.cooldown", 3);
        scissorsRecoveryInterval = config.getInt("scissors.recovery-interval", 10);
        scissorsHitEffectEnabled = config.getBoolean("scissors.hit-effect.enabled", true);
        scissorsHitDamage = config.getDouble("scissors.hit-effect.damage", 0);
        scissorsCutDamageEnabled = config.getBoolean("scissors.cut-damage.enabled", true);
        scissorsCutDamagePercent = config.getDouble("scissors.cut-damage.percent", 0.5);
        pantProtectionEnabled = config.getBoolean("scissors.pants-protection.enabled", true);
        pantsDurabilityCost = config.getInt("scissors.pants-protection.pants-durability-cost", 5);
        pantsCutsRequired = new java.util.HashMap<>();
        var cutsSection = config.getConfigurationSection("scissors.pants-protection.cuts-required");
        if (cutsSection != null) {
            for (String key : cutsSection.getKeys(false)) {
                pantsCutsRequired.put(key.toUpperCase(), cutsSection.getInt(key));
            }
        }
        enchantmentBonus = new java.util.HashMap<>();
        var enchantSection = config.getConfigurationSection("scissors.pants-protection.enchantment-bonus");
        if (enchantSection != null) {
            for (String key : enchantSection.getKeys(false)) {
                enchantmentBonus.put(key.toUpperCase(), enchantSection.getInt(key));
            }
        }
        scissorsDropEnabled = config.getBoolean("scissors.drop.enabled", true);
        scissorsDropMaterial = config.getString("scissors.drop.material", "PORKCHOP");
        scissorsDropCustomModelData = config.contains("scissors.drop.custom-model-data") ? config.getInt("scissors.drop.custom-model-data") : null;
        scissorsDropName = config.getString("scissors.drop.name", "&c%player%的牛牛");
        scissorsDropLore = config.getStringList("scissors.drop.lore");
        scissorsOwnerPickupDelay = config.getInt("scissors.drop.owner-pickup-delay", 5);
        scissorsDropNbtEnabled = config.getBoolean("scissors.drop.nbt.enabled", true);
        scissorsDropNbtKey = config.getString("scissors.drop.nbt.key", "dreamrealms_cut");
        shootSpeedMin = config.getDouble("shoot-particle.speed-min", 0.3);
        shootSpeedMax = config.getDouble("shoot-particle.speed-max", 0.8);
        shootDamageEnabled = config.getBoolean("shoot-particle.damage-enabled", true);
        shootDamage = config.getDouble("shoot-particle.damage", 0);
        resetTime = config.getString("reset.time", "06:00");
        resetWorld = config.getString("reset.world", "world");
    }

    public boolean isDebug() { return debug; }
    public double getPromptChance() { return promptChance; }
    public int getPromptDelayMin() { return promptDelayMin; }
    public int getPromptDelayMax() { return promptDelayMax; }
    public int getCooldown() { return cooldown; }
    public int getHandSwingInterval() { return handSwingInterval; }
    public float getSoundVolume() { return soundVolume; }
    public String getMilkMaterial() { return milkMaterial; }
    public Integer getCustomModelData() { return customModelData; }
    public String getMilkColor() { return milkColor; }
    public String getMilkName() { return milkName; }
    public List<String> getMilkLore() { return milkLore; }
    public boolean isMilkNbtEnabled() { return milkNbtEnabled; }
    public String getMilkNbtKey() { return milkNbtKey; }
    public boolean isScissorsEnabled() { return scissorsEnabled; }
    public long getScissorsDisableDuration() { return scissorsDisableDuration; }
    public int getScissorsDurabilityCost() { return scissorsDurabilityCost; }
    public int getScissorsCooldown() { return scissorsCooldown; }
    public int getScissorsRecoveryInterval() { return scissorsRecoveryInterval; }
    public boolean isScissorsHitEffectEnabled() { return scissorsHitEffectEnabled; }
    public double getScissorsHitDamage() { return scissorsHitDamage; }
    public boolean isScissorsCutDamageEnabled() { return scissorsCutDamageEnabled; }
    public double getScissorsCutDamagePercent() { return scissorsCutDamagePercent; }
    public boolean isPantProtectionEnabled() { return pantProtectionEnabled; }
    public int getPantsDurabilityCost() { return pantsDurabilityCost; }
    public java.util.Map<String, Integer> getPantsCutsRequired() { return pantsCutsRequired; }
    public java.util.Map<String, Integer> getEnchantmentBonus() { return enchantmentBonus; }
    public boolean isScissorsDropEnabled() { return scissorsDropEnabled; }
    public String getScissorsDropMaterial() { return scissorsDropMaterial; }
    public Integer getScissorsDropCustomModelData() { return scissorsDropCustomModelData; }
    public String getScissorsDropName() { return scissorsDropName; }
    public List<String> getScissorsDropLore() { return scissorsDropLore; }
    public int getScissorsOwnerPickupDelay() { return scissorsOwnerPickupDelay; }
    public boolean isScissorsDropNbtEnabled() { return scissorsDropNbtEnabled; }
    public String getScissorsDropNbtKey() { return scissorsDropNbtKey; }
    public boolean isShootDamageEnabled() { return shootDamageEnabled; }
    public double getShootDamage() { return shootDamage; }
    public String getResetTime() { return resetTime; }
    public String getResetWorld() { return resetWorld; }

    public double getRandomShootSpeed() {
        return ThreadLocalRandom.current().nextDouble(shootSpeedMin, shootSpeedMax);
    }

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
