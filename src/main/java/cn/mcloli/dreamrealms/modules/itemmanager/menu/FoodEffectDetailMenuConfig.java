package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 食物效果详情编辑菜单配置
 */
public class FoodEffectDetailMenuConfig extends AbstractMenuConfig<FoodEffectDetailGui> {

    public FoodEffectDetailMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/food_effect_detail.yml");
    }

    @Override
    protected void clearMainIcons() {
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        return false;
    }

    @Override
    @Nullable
    protected ItemStack tryApplyMainIcon(FoodEffectDetailGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
