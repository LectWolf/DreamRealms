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
 * 分类菜单配置
 */
public class CategoryMenuConfig extends AbstractMenuConfig<IGui> {

    private final ItemManagerModule module;

    // 主图标
    private Icon categoryIcon;
    private Icon addCategoryIcon;
    private Icon uncategorizedIcon;

    public CategoryMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/category.yml");
        this.module = module;
    }

    public Icon getCategoryIcon() {
        return categoryIcon;
    }

    public Icon getAddCategoryIcon() {
        return addCategoryIcon;
    }

    public Icon getUncategorizedIcon() {
        return uncategorizedIcon;
    }

    @Override
    protected void clearMainIcons() {
        categoryIcon = null;
        addCategoryIcon = null;
        uncategorizedIcon = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "C" -> categoryIcon = icon;
            case "A" -> addCategoryIcon = icon;
            case "U" -> uncategorizedIcon = icon;
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    @Nullable
    protected ItemStack tryApplyMainIcon(IGui gui, String key, Player player, int iconIndex) {
        // 主图标由 GUI 实例动态生成
        return null;
    }
}
