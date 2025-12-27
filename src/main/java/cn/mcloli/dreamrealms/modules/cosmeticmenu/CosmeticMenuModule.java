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
            return;
        }

        // 检查 HMCCosmetics 是否已加载
        if (!Bukkit.getPluginManager().isPluginEnabled("HMCCosmetics")) {
            warn("HMCCosmetics 未安装或未启用，模块无法加载");
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
        command = new CosmeticCommand(this);
        CommandRegister.unregister("cos");
        CommandRegister.register(
                "cos",
                new String[]{},
                "时装菜单",
                command
        );

        info("模块已加载");
    }
}
