package cn.mcloli.dreamrealms.modules.giftpoints.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.giftpoints.GiftPointsModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GiftPointsConfig {
    private final DreamRealms plugin;
    private final GiftPointsModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;
    private int pointsScale; // 点券比例，如 10 表示 1 元 = 10 点券
    private int paymentTimeout; // 支付超时时间（秒）
    private boolean enableWechat;
    private boolean enableAlipay;
    private List<Double> amountOptions; // 金额选项

    public GiftPointsConfig(DreamRealms plugin, GiftPointsModule module) {
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
        pointsScale = config.getInt("points-scale", 10);
        paymentTimeout = config.getInt("payment-timeout", 300);
        enableWechat = config.getBoolean("payment.wechat", true);
        enableAlipay = config.getBoolean("payment.alipay", true);

        amountOptions = new ArrayList<>();
        for (Object obj : config.getList("amount-options", List.of(1, 5, 10, 20, 50, 100))) {
            if (obj instanceof Number num) {
                amountOptions.add(num.doubleValue());
            }
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public int getPointsScale() {
        return pointsScale;
    }

    public int getPaymentTimeout() {
        return paymentTimeout;
    }

    public boolean isEnableWechat() {
        return enableWechat;
    }

    public boolean isEnableAlipay() {
        return enableAlipay;
    }

    public List<Double> getAmountOptions() {
        return amountOptions;
    }

    public int calculatePoints(double money) {
        return (int) Math.round(money * pointsScale);
    }
}
