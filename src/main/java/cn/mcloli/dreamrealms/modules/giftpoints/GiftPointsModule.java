package cn.mcloli.dreamrealms.modules.giftpoints;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.giftpoints.command.GiftCommand;
import cn.mcloli.dreamrealms.modules.giftpoints.config.GiftPointsConfig;
import cn.mcloli.dreamrealms.modules.giftpoints.lang.GiftPointsMessages;
import cn.mcloli.dreamrealms.modules.giftpoints.menu.AmountSelectMenuConfig;
import cn.mcloli.dreamrealms.modules.giftpoints.menu.PaymentSelectMenuConfig;
import cn.mcloli.dreamrealms.modules.giftpoints.menu.PlayerSelectMenuConfig;
import cn.mcloli.dreamrealms.utils.CommandRegister;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;

@AutoRegister
public class GiftPointsModule extends AbstractModule {

    private GiftPointsConfig config;
    private GiftPointsMessages.Holder lang;
    private GiftCommand command;

    // 菜单配置
    private PlayerSelectMenuConfig playerSelectMenuConfig;
    private PaymentSelectMenuConfig paymentSelectMenuConfig;
    private AmountSelectMenuConfig amountSelectMenuConfig;

    public GiftPointsModule(DreamRealms plugin) {
        super(plugin, "giftpoints");
    }

    public static GiftPointsModule inst() {
        return instanceOf(GiftPointsModule.class);
    }

    @Override
    protected String getModuleDescription() {
        return "点券赠送模块 - 玩家可以充值赠送点券给其他玩家";
    }

    public GiftPointsConfig getModuleConfig() {
        return config;
    }

    public PlayerSelectMenuConfig getPlayerSelectMenuConfig() {
        return playerSelectMenuConfig;
    }

    public PaymentSelectMenuConfig getPaymentSelectMenuConfig() {
        return paymentSelectMenuConfig;
    }

    public AmountSelectMenuConfig getAmountSelectMenuConfig() {
        return amountSelectMenuConfig;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            return;
        }

        // 检查 SweetCheckout 是否已加载
        if (!Bukkit.getPluginManager().isPluginEnabled("SweetCheckout")) {
            warn("SweetCheckout 未安装或未启用，模块无法加载");
            return;
        }

        // 检查 PlayerPoints 是否已加载
        if (!Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            warn("PlayerPoints 未安装或未启用，模块无法加载");
            return;
        }

        // 首次加载时注册语言
        if (lang == null) {
            lang = GiftPointsMessages.register();
        }

        // 初始化配置
        if (config == null) {
            config = new GiftPointsConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        // 初始化菜单配置
        if (playerSelectMenuConfig == null) {
            playerSelectMenuConfig = new PlayerSelectMenuConfig(plugin, this);
        }
        playerSelectMenuConfig.reloadConfig(cfg);

        if (paymentSelectMenuConfig == null) {
            paymentSelectMenuConfig = new PaymentSelectMenuConfig(plugin, this);
        }
        paymentSelectMenuConfig.reloadConfig(cfg);

        if (amountSelectMenuConfig == null) {
            amountSelectMenuConfig = new AmountSelectMenuConfig(plugin, this);
        }
        amountSelectMenuConfig.reloadConfig(cfg);

        // 注册独立命令 /gift
        command = new GiftCommand(this);
        CommandRegister.unregister("gift");
        CommandRegister.register(
                "gift",
                new String[]{},
                "点券赠送",
                command
        );

        info("模块已加载");
    }
}
