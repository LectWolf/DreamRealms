package cn.mcloli.dreamrealms.modules.giftpoints.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.giftpoints.GiftPointsModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PaymentSelectMenuConfig extends AbstractMenuConfig<PaymentSelectGui> {

    public PaymentSelectMenuConfig(DreamRealms plugin, GiftPointsModule module) {
        super(plugin, module.getModuleMenuPath() + "/payment_select.yml");
    }

    @Override
    protected void clearMainIcons() {
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        return false;
    }

    @Override
    protected ItemStack tryApplyMainIcon(PaymentSelectGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
