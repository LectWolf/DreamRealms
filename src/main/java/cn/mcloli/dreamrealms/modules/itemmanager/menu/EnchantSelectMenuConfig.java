package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.gui.IGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 附魔选择菜单配置
 */
public class EnchantSelectMenuConfig extends AbstractMenuConfig<IGui> {

    /**
     * 附魔分类 (从配置文件读取)
     */
    public record EnchantCategory(String id, String displayName, Material icon, Set<String> enchantKeys) {
        public boolean contains(Enchantment enchant) {
            return enchantKeys.contains(enchant.getKey().getKey());
        }
    }

    private final ItemManagerModule module;

    // 主图标
    private Icon enchantIcon;
    private Icon allIcon;
    
    // 分类列表
    private final List<EnchantCategory> categories = new ArrayList<>();

    public EnchantSelectMenuConfig(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module.getModuleMenuPath() + "/enchant_select.yml");
        this.module = module;
    }

    public Icon getEnchantIcon() {
        return enchantIcon;
    }

    public Icon getAllIcon() {
        return allIcon;
    }

    public List<EnchantCategory> getCategories() {
        return categories;
    }

    /**
     * 根据附魔键名获取分类图标材质
     */
    @Nullable
    public Material getCategoryIconForEnchant(String enchantKey) {
        for (EnchantCategory category : categories) {
            if (category.enchantKeys().contains(enchantKey)) {
                return category.icon();
            }
        }
        return null;
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
        enchantIcon = null;
        allIcon = null;
        categories.clear();
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "E" -> enchantIcon = icon;
            case "A" -> allIcon = icon;
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        
        // 加载分类配置
        if (config != null) {
            ConfigurationSection categoriesSection = config.getConfigurationSection("categories");
            if (categoriesSection != null) {
                for (String categoryId : categoriesSection.getKeys(false)) {
                    ConfigurationSection catSection = categoriesSection.getConfigurationSection(categoryId);
                    if (catSection == null) continue;

                    String displayName = catSection.getString("display", categoryId);
                    String iconStr = catSection.getString("icon", "ENCHANTED_BOOK");
                    Material icon = Material.matchMaterial(iconStr);
                    if (icon == null) icon = Material.ENCHANTED_BOOK;
                    
                    List<String> enchantList = catSection.getStringList("enchants");
                    Set<String> enchantKeys = new HashSet<>(enchantList);

                    categories.add(new EnchantCategory(categoryId, displayName, icon, enchantKeys));
                    module.debug("加载附魔分类: " + categoryId + " (" + enchantKeys.size() + " 个附魔)");
                }
            }
            
            module.debug("共加载 " + categories.size() + " 个附魔分类");
        }
    }

    @Override
    @Nullable
    protected ItemStack tryApplyMainIcon(IGui gui, String key, Player player, int iconIndex) {
        return null;
    }
}
