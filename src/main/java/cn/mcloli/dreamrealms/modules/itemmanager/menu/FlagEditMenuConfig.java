package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Flag 编辑菜单配置
 */
public class FlagEditMenuConfig extends AbstractMenuConfig<FlagEditGui> {

    public FlagEditMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/flag_edit.yml");
    }

    @Override
    protected void clearMainIcons() {
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        return false;
    }

    @Override
    protected ItemStack tryApplyMainIcon(FlagEditGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
