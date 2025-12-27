package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 食物组件编辑菜单配置
 */
public class FoodEditMenuConfig extends AbstractMenuConfig<FoodEditGui> {

    private Icon nutritionIcon;
    private Icon saturationIcon;
    private Icon eatTimeIcon;
    private Icon canAlwaysEatIcon;
    private Icon usingConvertsToIcon;
    private Icon effectsIcon;
    private Icon removeIcon;

    public FoodEditMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/food_edit.yml");
        markInteractive('P', 'N', 'S', 'T', 'C', 'U', 'A', 'R');
    }

    @Nullable public Icon getNutritionIcon() { return nutritionIcon; }
    @Nullable public Icon getSaturationIcon() { return saturationIcon; }
    @Nullable public Icon getEatTimeIcon() { return eatTimeIcon; }
    @Nullable public Icon getCanAlwaysEatIcon() { return canAlwaysEatIcon; }
    @Nullable public Icon getUsingConvertsToIcon() { return usingConvertsToIcon; }
    @Nullable public Icon getEffectsIcon() { return effectsIcon; }
    @Nullable public Icon getRemoveIcon() { return removeIcon; }

    @Override
    protected void clearMainIcons() {
        nutritionIcon = null;
        saturationIcon = null;
        eatTimeIcon = null;
        canAlwaysEatIcon = null;
        usingConvertsToIcon = null;
        effectsIcon = null;
        removeIcon = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "N" -> nutritionIcon = icon;
            case "S" -> saturationIcon = icon;
            case "T" -> eatTimeIcon = icon;
            case "C" -> canAlwaysEatIcon = icon;
            case "U" -> usingConvertsToIcon = icon;
            case "A" -> effectsIcon = icon;
            case "R" -> removeIcon = icon;
            default -> { return false; }
        }
        return true;
    }

    @Override
    @Nullable
    protected ItemStack tryApplyMainIcon(FoodEditGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
