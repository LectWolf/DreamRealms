package cn.mcloli.dreamrealms.modules.cosmeticmenu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.command.CosmeticCommand;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.config.CosmeticMenuConfig;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.lang.CosmeticMenuMessages;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.menu.CosmeticListMenuConfig;
import cn.mcloli.dreamrealms.utils.CommandRegister;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;

@AutoRegister
public class CosmeticMenuModule extends AbstractModule {

    private CosmeticMenuConfig config;
    private CosmeticMenuMessages.Holder lang;
    private CosmeticCommand command;
    
    // 菜单配置
    private CosmeticListMenuConfig cosmeticListMenuConfig;
    
    // 命令是否已注册
    private boolean commandRegistered = false;

    public CosmeticMenuModule(DreamRealms plugin) {
        super(plugin, "cosmeticmenu");
    }

    public static CosmeticMenuModule inst() {
        return instanceOf(CosmeticMenuModule.class);
    }

    @Override
    protected String getModuleDescription() {
        return "时装菜单模块 - 展示玩家拥有的 HMCCosmetics 时装";
    }

    public CosmeticMenuConfig getModuleConfig() {
        return config;
    }

    public CosmeticListMenuConfig getCosmeticListMenuConfig() {
        return cosmeticListMenuConfig;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            // 模块禁用时注销命令
            unregisterCommand();
            return;
        }

        // 检查 HMCCosmetics 是否已加载
        if (!Bukkit.getPluginManager().isPluginEnabled("HMCCosmetics")) {
            warn("HMCCosmetics 未安装或未启用，模块无法加载");
            unregisterCommand();
            return;
        }

        // 首次加载时注册语言
        if (lang == null) {
            lang = CosmeticMenuMessages.register();
        }

        // 初始化配置
        if (config == null) {
            config = new CosmeticMenuConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        // 初始化菜单配置
        if (cosmeticListMenuConfig == null) {
            cosmeticListMenuConfig = new CosmeticListMenuConfig(plugin, this);
        }
        cosmeticListMenuConfig.reloadConfig(cfg);

        // 注册独立命令 /cos (动态注册，无需 plugin.yml)
        registerCommand();

        info("模块已加载");
    }
    
    /**
     * 注册命令
     */
    private void registerCommand() {
        // 先注销旧命令，确保使用新的 executor
        CommandRegister.unregister("cos");
        
        command = new CosmeticCommand(this);
        if (CommandRegister.register(
                "cos",
                new String[]{},
                "时装菜单",
                command
        )) {
            commandRegistered = true;
        }
    }
    
    /**
     * 注销命令
     */
    private void unregisterCommand() {
        if (commandRegistered) {
            CommandRegister.unregister("cos");
            commandRegistered = false;
            command = null;
        }
    }
}
