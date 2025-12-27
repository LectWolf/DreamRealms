package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 属性详情编辑菜单配置
 */
public class AttributeDetailMenuConfig extends AbstractMenuConfig<AttributeDetailGui> {

    public AttributeDetailMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/attribute_detail.yml");
    }

    @Override
    protected void clearMainIcons() {
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        return false;
    }

    @Override
    protected ItemStack tryApplyMainIcon(AttributeDetailGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
