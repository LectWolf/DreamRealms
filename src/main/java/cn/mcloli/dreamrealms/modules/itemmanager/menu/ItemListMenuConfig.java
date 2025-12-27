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
 * 物品列表菜单配置
 */
public class ItemListMenuConfig extends AbstractMenuConfig<IGui> {

    private final ItemManagerModule module;

    // 主图标
    private Icon itemSlotIcon;
    private Icon addItemIcon;

    public ItemListMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/item_list.yml");
        this.module = module;
    }

    public Icon getItemSlotIcon() {
        return itemSlotIcon;
    }

    public Icon getAddItemIcon() {
        return addItemIcon;
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
        itemSlotIcon = null;
        addItemIcon = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "I" -> itemSlotIcon = icon;
            case "A" -> addItemIcon = icon;
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
