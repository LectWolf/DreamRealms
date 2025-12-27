package cn.mcloli.dreamrealms.modules.ownerbind.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class OwnerBindConfig {
    private final DreamRealms plugin;
    private final OwnerBindModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;
    
    // 绑定识别方式
    private boolean loreBindEnabled;
    private List<String> bindableLores;
    private boolean nbtBindEnabled;
    
    // Lore 配置
    private String boundLore;
    
    // 功能开关
    private boolean antiDrop;
    private boolean antiContainerPickup;
    private boolean repairMode;
    
    // Hook 配置
    private boolean hookSweetMail;
    private String mailSenderName;
    private String mailIcon;
    private String mailTitle;
    private List<String> mailContent;
    private int mailOutdateDays;
    
    private boolean hookGlobalMarketPlus;
    private boolean hookZAuctionHouse;
    private boolean hookQuickShop;

    public OwnerBindConfig(DreamRealms plugin, OwnerBindModule module) {
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
        
        // 绑定识别方式
        loreBindEnabled = config.getBoolean("bind-detection.lore.enabled", true);
        bindableLores = config.getStringList("bind-detection.lore.patterns");
        nbtBindEnabled = config.getBoolean("bind-detection.nbt.enabled", true);
        
        // Lore 配置
        boundLore = config.getString("lore.bound", "&7⛓ 已绑定: &f%player%");
        
        // 功能开关
        antiDrop = config.getBoolean("anti-drop", false);
        antiContainerPickup = config.getBoolean("anti-container-pickup", true);
        repairMode = config.getBoolean("repair-mode", true);
        
        // Hook 配置
        hookSweetMail = config.getBoolean("hooks.sweetmail.enabled", false);
        mailSenderName = config.getString("hooks.sweetmail.sender-name", "系统");
        mailIcon = config.getString("hooks.sweetmail.icon", "CHEST");
        mailTitle = config.getString("hooks.sweetmail.title", "物品归还");
        mailContent = config.getStringList("hooks.sweetmail.content");
        mailOutdateDays = config.getInt("hooks.sweetmail.outdate-days", 30);
        
        hookGlobalMarketPlus = config.getBoolean("hooks.globalmarketplus.enabled", false);
        hookZAuctionHouse = config.getBoolean("hooks.zauctionhouse.enabled", false);
        hookQuickShop = config.getBoolean("hooks.quickshop.enabled", false);
    }

    // Getters
    public boolean isDebug() { return debug; }
    
    public boolean isLoreBindEnabled() { return loreBindEnabled; }
    public List<String> getBindableLores() { return bindableLores; }
    public boolean isNbtBindEnabled() { return nbtBindEnabled; }
    
    public String getBoundLore() { return boundLore; }
    
    public boolean isAntiDrop() { return antiDrop; }
    public boolean isAntiContainerPickup() { return antiContainerPickup; }
    public boolean isRepairMode() { return repairMode; }
    
    public boolean isHookSweetMail() { return hookSweetMail; }
    public String getMailSenderName() { return mailSenderName; }
    public String getMailIcon() { return mailIcon; }
    public String getMailTitle() { return mailTitle; }
    public List<String> getMailContent() { return mailContent; }
    public int getMailOutdateDays() { return mailOutdateDays; }
    
    public boolean isHookGlobalMarketPlus() { return hookGlobalMarketPlus; }
    public boolean isHookZAuctionHouse() { return hookZAuctionHouse; }
    public boolean isHookQuickShop() { return hookQuickShop; }
}
