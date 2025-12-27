package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.gui.IGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 属性编辑菜单配置
 */
public class AttributeEditMenuConfig extends AbstractMenuConfig<IGui> {

    private final ItemManagerModule module;

    private Icon attributeIcon;
    private Icon addIcon;

    public AttributeEditMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/attribute_edit.yml");
        this.module = module;
    }

    public Icon getAttributeIcon() {
        return attributeIcon;
    }

    public Icon getAddIcon() {
        return addIcon;
    }

    @Nullable
    public ItemStack getEmptyPrevIcon(Player player) {
        Icon icon = otherIcons.get("<_empty");
        return icon != null ? icon.generateIcon(player) : null;
    }

    @Nullable
    public ItemStack getEmptyNextIcon(Player player) {
        Icon icon = otherIcons.get(">_empty");
        return icon != null ? icon.generateIcon(player) : null;
    }

    @Override
    protected void clearMainIcons() {
        attributeIcon = null;
        addIcon = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "A" -> attributeIcon = icon;
            case "N" -> addIcon = icon;
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    @Nullable
    protected ItemStack tryApplyMainIcon(IGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
