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
 * 物品编辑菜单配置
 */
public class ItemEditMenuConfig extends AbstractMenuConfig<IGui> {

    private final ItemManagerModule module;

    // 主图标
    private Icon previewIcon;
    private Icon commandIcon;
    private Icon serializeIcon;
    private Icon nameIcon;
    private Icon loreIcon;
    private Icon enchantIcon;
    private Icon attributeIcon;
    private Icon flagIcon;
    private Icon durabilityIcon;
    private Icon identifierIcon;
    private Icon categoryIcon;

    public ItemEditMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/item_edit.yml");
        this.module = module;
    }

    public ItemManagerModule getModule() {
        return module;
    }

    public Icon getPreviewIcon() {
        return previewIcon;
    }

    public Icon getCommandIcon() {
        return commandIcon;
    }

    public Icon getSerializeIcon() {
        return serializeIcon;
    }

    public Icon getNameIcon() {
        return nameIcon;
    }

    public Icon getLoreIcon() {
        return loreIcon;
    }

    public Icon getEnchantIcon() {
        return enchantIcon;
    }

    public Icon getAttributeIcon() {
        return attributeIcon;
    }

    public Icon getFlagIcon() {
        return flagIcon;
    }

    public Icon getDurabilityIcon() {
        return durabilityIcon;
    }

    public Icon getIdentifierIcon() {
        return identifierIcon;
    }

    public Icon getCategoryIcon() {
        return categoryIcon;
    }

    @Override
    protected void clearMainIcons() {
        previewIcon = null;
        commandIcon = null;
        serializeIcon = null;
        nameIcon = null;
        loreIcon = null;
        enchantIcon = null;
        attributeIcon = null;
        flagIcon = null;
        durabilityIcon = null;
        identifierIcon = null;
        categoryIcon = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "P" -> previewIcon = icon;
            case "O" -> commandIcon = icon;
            case "S" -> serializeIcon = icon;
            case "N" -> nameIcon = icon;
            case "L" -> loreIcon = icon;
            case "E" -> enchantIcon = icon;
            case "A" -> attributeIcon = icon;
            case "F" -> flagIcon = icon;
            case "D" -> durabilityIcon = icon;
            case "I" -> identifierIcon = icon;
            case "G" -> categoryIcon = icon;
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
