package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.gui.IGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性选择菜单配置
 */
public class AttributeSelectMenuConfig extends AbstractMenuConfig<IGui> {

    private final ItemManagerModule module;

    private Icon attributeIcon;
    private Icon viewAllIcon;
    private final List<AttributeCategory> categories = new ArrayList<>();

    public record AttributeCategory(String id, String name, ItemStack icon, List<String> attributes) {}

    public AttributeSelectMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/attribute_select.yml");
        this.module = module;
    }

    public Icon getAttributeIcon() {
        return attributeIcon;
    }

    public Icon getViewAllIcon() {
        return viewAllIcon;
    }

    public List<AttributeCategory> getCategories() {
        return categories;
    }

    /**
     * 根据属性键名获取分类图标
     */
    @Nullable
    public ItemStack getCategoryIconForAttribute(String attributeKey) {
        for (AttributeCategory category : categories) {
            if (category.attributes().contains(attributeKey)) {
                return category.icon().clone();
            }
        }
        return null;
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
        viewAllIcon = null;
        categories.clear();
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        if (key.equals("A")) {
            attributeIcon = icon;
            return true;
        }
        if (key.equals("V")) {
            viewAllIcon = icon;
            return true;
        }
        return false;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        
        // 加载分类配置
        if (config == null) return;
        
        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String categoryId : categoriesSection.getKeys(false)) {
                ConfigurationSection catSection = categoriesSection.getConfigurationSection(categoryId);
                if (catSection == null) continue;

                String name = catSection.getString("display", categoryId);
                String iconStr = catSection.getString("icon", "NETHER_STAR");
                List<String> attributes = catSection.getStringList("attributes");

                ItemStack icon = ItemBuilder.parseItem(iconStr);
                if (icon == null) {
                    icon = new ItemStack(Material.NETHER_STAR);
                }

                categories.add(new AttributeCategory(categoryId, name, icon, attributes));
            }
        }
    }

    @Override
    @Nullable
    protected ItemStack tryApplyMainIcon(IGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
