package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.EnchantmentUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 附魔选择 GUI
 * 支持分类浏览和查看所有附魔
 */
public class EnchantSelectGui extends AbstractInteractiveGui<EnchantSelectMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final EnchantEditGui parentGui;
    private List<Enchantment> availableEnchants;
    private int page = 0;
    private int slotsPerPage = 0;
    
    // 当前选中的分类 (null = 显示分类列表)
    @Nullable
    private EnchantSelectMenuConfig.EnchantCategory currentCategory = null;
    private boolean showAll = false;

    public EnchantSelectGui(Player player, EnchantSelectMenuConfig config, StoredItem storedItem, EnchantEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
        
        // Debug 输出所有附魔
        if (module.isDebug()) {
            module.debug("=== 所有附魔列表 ===");
            for (Enchantment enchant : EnchantmentUtil.getAllEnchantments()) {
                module.debug("  " + enchant.getKey().getKey() + " - " + EnchantmentUtil.getEnchantmentName(enchant));
            }
            module.debug("=== 附魔列表结束 ===");
        }
    }

    private void loadAvailableEnchants() {
        ItemStack item = storedItem.getItemStack();
        this.availableEnchants = new ArrayList<>();
        
        for (Enchantment enchant : EnchantmentUtil.getAllEnchantments()) {
            if (!item.containsEnchantment(enchant)) {
                // 如果有分类筛选
                if (currentCategory != null && !showAll) {
                    if (currentCategory.contains(enchant)) {
                        availableEnchants.add(enchant);
                    }
                } else {
                    availableEnchants.add(enchant);
                }
            }
        }
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = config.createInventory(this, player);
        slotsPerPage = countSlots('E');
        refreshInventory();
        return inventory;
    }

    private int countSlots(char key) {
        int count = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            Character k = config.getSlotKey(i);
            if (k != null && k == key) count++;
        }
        return count;
    }

    private void refreshInventory() {
        List<EnchantSelectMenuConfig.EnchantCategory> categories = config.getCategories();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            String keyStr = String.valueOf(key);
            if (keyStr.equals(" ") || keyStr.equals("　")) {
                inventory.setItem(i, null);
                continue;
            }

            switch (key) {
                case 'E' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    if (currentCategory == null && !showAll) {
                        // 显示分类
                        inventory.setItem(i, getCategoryItem(categories, index));
                    } else {
                        // 显示附魔
                        inventory.setItem(i, getEnchantItem(index));
                    }
                }
                case 'A' -> {
                    // 查看所有附魔按钮
                    inventory.setItem(i, config.getAllIcon() != null ? config.getAllIcon().generateIcon(player) : null);
                }
                case '<' -> {
                    if (page > 0) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        inventory.setItem(i, config.getEmptyPrevIcon(player));
                    }
                }
                case '>' -> {
                    if (hasNextPage(categories)) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        inventory.setItem(i, config.getEmptyNextIcon(player));
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private boolean hasNextPage(List<EnchantSelectMenuConfig.EnchantCategory> categories) {
        if (currentCategory == null && !showAll) {
            return (page + 1) * slotsPerPage < categories.size();
        }
        return (page + 1) * slotsPerPage < availableEnchants.size();
    }

    private ItemStack getCategoryItem(List<EnchantSelectMenuConfig.EnchantCategory> categories, int index) {
        if (index >= categories.size()) {
            return null;
        }

        EnchantSelectMenuConfig.EnchantCategory category = categories.get(index);
        ItemStack item = new ItemStack(category.icon());
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&e" + category.displayName()));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7点击查看该分类的附魔"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private ItemStack getEnchantItem(int index) {
        if (index >= availableEnchants.size()) {
            return null;
        }

        Enchantment enchant = availableEnchants.get(index);
        String enchantKey = enchant.getKey().getKey();
        
        // 尝试获取分类图标材质
        Material iconMaterial = config.getCategoryIconForEnchant(enchantKey);
        if (iconMaterial == null) {
            iconMaterial = Material.ENCHANTED_BOOK;
        }
        
        ItemStack item = new ItemStack(iconMaterial);
        String enchantName = EnchantmentUtil.getEnchantmentName(enchant);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&b" + enchantName));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7最大等级: &f" + enchant.getMaxLevel()));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击添加"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("EnchantSelectGui click: key=" + key + ", index=" + index + ", click=" + click);

        List<EnchantSelectMenuConfig.EnchantCategory> categories = config.getCategories();

        switch (key) {
            case 'E' -> {
                int realIndex = index + page * slotsPerPage;
                if (currentCategory == null && !showAll) {
                    // 点击分类
                    handleCategoryClick(categories, realIndex);
                } else {
                    // 点击附魔
                    handleEnchantClick(realIndex);
                }
            }
            case 'A' -> {
                // 查看所有附魔
                showAll = true;
                currentCategory = null;
                page = 0;
                loadAvailableEnchants();
                refreshInventory();
            }
            case 'B' -> {
                if (currentCategory != null || showAll) {
                    // 返回分类列表
                    currentCategory = null;
                    showAll = false;
                    page = 0;
                    refreshInventory();
                } else {
                    // 返回附魔编辑页
                    parentGui.refresh();
                    parentGui.open();
                }
            }
            case '<' -> {
                if (page > 0) {
                    page--;
                    refreshInventory();
                }
            }
            case '>' -> {
                if (hasNextPage(categories)) {
                    page++;
                    refreshInventory();
                }
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handleCategoryClick(List<EnchantSelectMenuConfig.EnchantCategory> categories, int index) {
        if (index >= categories.size()) return;

        currentCategory = categories.get(index);
        showAll = false;
        page = 0;
        loadAvailableEnchants();
        refreshInventory();
    }

    private void handleEnchantClick(int index) {
        if (index >= availableEnchants.size()) return;

        Enchantment enchant = availableEnchants.get(index);
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 添加附魔 (等级1)
        meta.addEnchant(enchant, 1, true);
        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);

        ItemManagerMessages.enchant__added.t(player);

        // 返回附魔编辑页
        parentGui.refresh();
        parentGui.open();
    }
}
