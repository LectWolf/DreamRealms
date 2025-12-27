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
 * Lore 编辑菜单配置
 */
public class LoreEditMenuConfig extends AbstractMenuConfig<IGui> {

    private final ItemManagerModule module;

    // 主图标
    private Icon loreLineIcon;
    private Icon addIcon;

    public LoreEditMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/lore_edit.yml");
        this.module = module;
    }

    public Icon getLoreLineIcon() {
        return loreLineIcon;
    }

    public Icon getAddIcon() {
        return addIcon;
    }

    /**
     * 获取空的上一页图标
     */
    @Nullable
    public ItemStack getEmptyPrevIcon(Player player) {
        Icon icon = otherIcons.get("<_empty");
        return icon != null ? icon.generateIcon(player) : null;
    }

    /**
     * 获取空的下一页图标
     */
    @Nullable
    public ItemStack getEmptyNextIcon(Player player) {
        Icon icon = otherIcons.get(">_empty");
        return icon != null ? icon.generateIcon(player) : null;
    }

    @Override
    protected void clearMainIcons() {
        loreLineIcon = null;
        addIcon = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "L" -> loreLineIcon = icon;
            case "A" -> addIcon = icon;
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
