package cn.mcloli.dreamrealms.modules.welcome.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.welcome.WelcomeModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WelcomeConfig {

    private final DreamRealms plugin;
    private final WelcomeModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;

    // 欢迎文本配置
    private List<String> welcomeTexts;
    private List<String> welcomeHover;
    private List<String> welcomeMessages;

    // 奖励配置
    private boolean rewardEnabled;
    private int rewardDelayMinutes;
    private boolean rewardOnNewPlayerQuit;
    private int welcomerBalance;
    private int newplayerBalance;
    private boolean allowRepeatWelcome;

    // 命令配置
    private List<String> newPlayerRewardCommands;
    private List<String> welcomerCommands;

    public WelcomeConfig(DreamRealms plugin, WelcomeModule module) {
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

        // 欢迎文本
        welcomeTexts = config.getStringList("welcome-text");
        welcomeHover = config.getStringList("welcome-hover");
        welcomeMessages = config.getStringList("welcome-messages");

        // 奖励配置
        ConfigurationSection rewardSection = config.getConfigurationSection("reward");
        if (rewardSection != null) {
            rewardEnabled = rewardSection.getBoolean("enabled", true);
            rewardDelayMinutes = rewardSection.getInt("delay-minutes", 5);
            rewardOnNewPlayerQuit = rewardSection.getBoolean("on-new-player-quit", false);
            welcomerBalance = rewardSection.getInt("welcomer-balance", 100);
            newplayerBalance = rewardSection.getInt("newplayer-balance", 40);
            allowRepeatWelcome = rewardSection.getBoolean("allow-repeat-welcome", true);
        } else {
            rewardEnabled = true;
            rewardDelayMinutes = 5;
            rewardOnNewPlayerQuit = false;
            welcomerBalance = 100;
            newplayerBalance = 40;
            allowRepeatWelcome = true;
        }

        // 命令配置
        newPlayerRewardCommands = config.getStringList("commands.new-player-reward");
        welcomerCommands = config.getStringList("commands.welcomer");
    }

    public void save() {
        config.set("debug", debug);
        config.set("welcome-text", welcomeTexts);
        config.set("welcome-hover", welcomeHover);
        config.set("welcome-messages", welcomeMessages);
        config.set("reward.enabled", rewardEnabled);
        config.set("reward.delay-minutes", rewardDelayMinutes);
        config.set("reward.on-new-player-quit", rewardOnNewPlayerQuit);
        config.set("reward.welcomer-balance", welcomerBalance);
        config.set("reward.newplayer-balance", newplayerBalance);
        config.set("reward.allow-repeat-welcome", allowRepeatWelcome);
        config.set("commands.new-player-reward", newPlayerRewardCommands);
        config.set("commands.welcomer", welcomerCommands);

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.warn("保存配置失败: " + e.getMessage());
        }
    }

    // Getters
    public boolean isDebug() { return debug; }
    public List<String> getWelcomeTexts() { return new ArrayList<>(welcomeTexts); }
    public List<String> getWelcomeHover() { return new ArrayList<>(welcomeHover); }
    public List<String> getWelcomeMessages() { return new ArrayList<>(welcomeMessages); }
    public boolean isRewardEnabled() { return rewardEnabled; }
    public int getRewardDelayMinutes() { return rewardDelayMinutes; }
    public boolean isRewardOnNewPlayerQuit() { return rewardOnNewPlayerQuit; }
    public int getWelcomerBalance() { return welcomerBalance; }
    public int getNewplayerBalance() { return newplayerBalance; }
    public boolean isAllowRepeatWelcome() { return allowRepeatWelcome; }
    public List<String> getNewPlayerRewardCommands() { return new ArrayList<>(newPlayerRewardCommands); }
    public List<String> getWelcomerCommands() { return new ArrayList<>(welcomerCommands); }

    // Setters
    public void setRewardEnabled(boolean enabled) {
        this.rewardEnabled = enabled;
        save();
    }

    public void setRewardDelayMinutes(int minutes) {
        this.rewardDelayMinutes = minutes;
        save();
    }

    public void setRewardOnNewPlayerQuit(boolean value) {
        this.rewardOnNewPlayerQuit = value;
        save();
    }

    public void setWelcomerBalance(int value) {
        this.welcomerBalance = value;
        save();
    }

    public void setNewplayerBalance(int value) {
        this.newplayerBalance = value;
        save();
    }
}
