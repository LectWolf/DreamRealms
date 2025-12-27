package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 物品属性编辑菜单配置
 */
public class ItemPropertiesMenuConfig extends AbstractMenuConfig<ItemPropertiesGui> {

    private Icon damageIcon;
    private Icon maxDamageIcon;
    private Icon maxStackIcon;
    private Icon unbreakableIcon;
    private Icon rarityIcon;
    private Icon hideTooltipIcon;
    private Icon glintIcon;
    private Icon fireResistantIcon;
    private Icon customModelDataIcon;
    private Icon itemNameIcon;
    private Icon itemModelIcon;
    private Icon enchantableIcon;

    public ItemPropertiesMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/item_properties.yml");
    }

    @Nullable public Icon getDamageIcon() { return damageIcon; }
    @Nullable public Icon getMaxDamageIcon() { return maxDamageIcon; }
    @Nullable public Icon getMaxStackIcon() { return maxStackIcon; }
    @Nullable public Icon getUnbreakableIcon() { return unbreakableIcon; }
    @Nullable public Icon getRarityIcon() { return rarityIcon; }
    @Nullable public Icon getHideTooltipIcon() { return hideTooltipIcon; }
    @Nullable public Icon getGlintIcon() { return glintIcon; }
    @Nullable public Icon getFireResistantIcon() { return fireResistantIcon; }
    @Nullable public Icon getCustomModelDataIcon() { return customModelDataIcon; }
    @Nullable public Icon getItemNameIcon() { return itemNameIcon; }
    @Nullable public Icon getItemModelIcon() { return itemModelIcon; }
    @Nullable public Icon getEnchantableIcon() { return enchantableIcon; }

    @Override
    protected void clearMainIcons() {
        damageIcon = null;
        maxDamageIcon = null;
        maxStackIcon = null;
        unbreakableIcon = null;
        rarityIcon = null;
        hideTooltipIcon = null;
        glintIcon = null;
        fireResistantIcon = null;
        customModelDataIcon = null;
        itemNameIcon = null;
        itemModelIcon = null;
        enchantableIcon = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "D" -> damageIcon = icon;
            case "M" -> maxDamageIcon = icon;
            case "S" -> maxStackIcon = icon;
            case "U" -> unbreakableIcon = icon;
            case "R" -> rarityIcon = icon;
            case "H" -> hideTooltipIcon = icon;
            case "G" -> glintIcon = icon;
            case "F" -> fireResistantIcon = icon;
            case "C" -> customModelDataIcon = icon;
            case "N" -> itemNameIcon = icon;
            case "I" -> itemModelIcon = icon;
            case "E" -> enchantableIcon = icon;
            default -> { return false; }
        }
        return true;
    }

    @Override
    @Nullable
    protected ItemStack tryApplyMainIcon(ItemPropertiesGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
