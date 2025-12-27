package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.ItemCategory;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类选择 GUI
 */
public class CategorySelectGui extends AbstractInteractiveGui<CategoryMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemEditGui parentGui;
    private List<ItemCategory> categories;
    private int page = 0;
    private int slotsPerPage = 0;

    public CategorySelectGui(Player player, CategoryMenuConfig config, StoredItem storedItem, ItemEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        // 加载分类数据
        categories = module.getDatabase().getAllCategories();

        this.inventory = config.createInventory(this, player);

        // 计算每页槽位数
        slotsPerPage = countSlots('C');

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
        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            String keyStr = String.valueOf(key);
            if (keyStr.equals(" ") || keyStr.equals("　")) {
                inventory.setItem(i, null);
                continue;
            }

            switch (key) {
                case 'C' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    inventory.setItem(i, getCategoryItem(index));
                }
                case 'A' -> {
                    // 隐藏添加按钮
                    inventory.setItem(i, null);
                }
                case 'U' -> {
                    // 未分类按钮 - 点击移除分类
                    AbstractMenuConfig.Icon icon = config.getUncategorizedIcon();
                    if (icon != null) {
                        ItemStack item = icon.generateIcon(player);
                        List<String> lore = new ArrayList<>();
                        lore.add(ColorHelper.parseColor("&7点击移除分类"));
                        ItemStackUtil.setItemLore(item, lore);
                        inventory.setItem(i, item);
                    }
                }
                case '<' -> {
                    if (page > 0) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        // 显示空图标
                        inventory.setItem(i, config.getEmptyPrevIcon(player));
                    }
                }
                case '>' -> {
                    if (hasNextPage()) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        // 显示空图标
                        inventory.setItem(i, config.getEmptyNextIcon(player));
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private boolean hasNextPage() {
        return (page + 1) * slotsPerPage < categories.size();
    }

    private ItemStack getCategoryItem(int index) {
        if (index >= categories.size()) {
            return null;
        }

        ItemCategory category = categories.get(index);
        AbstractMenuConfig.Icon icon = config.getCategoryIcon();
        if (icon == null) {
            return null;
        }

        // 解析分类图标
        ItemStack item;
        if (category.getIcon() != null) {
            item = ItemBuilder.parseItem(category.getIcon());
            if (item == null) {
                item = new ItemStack(Material.CHEST);
            }
        } else {
            item = icon.generateIcon(player,
                    Pair.of("%name%", category.getName()),
                    Pair.of("%count%", String.valueOf(module.getDatabase().getItemsByCategory(category.getId()).size()))
            );
        }

        // 设置名称和 Lore
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&e" + category.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7物品数量: &f" + module.getDatabase().getItemsByCategory(category.getId()).size()));
        lore.add("");
        // 标记当前分类
        if (category.getId().equals(storedItem.getCategoryId())) {
            lore.add(ColorHelper.parseColor("&a✔ 当前分类"));
        } else {
            lore.add(ColorHelper.parseColor("&7点击选择"));
        }
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("CategorySelectGui click: key=" + key + ", index=" + index + ", click=" + click);

        switch (key) {
            case 'C' -> handleCategoryClick(index + page * slotsPerPage);
            case 'U' -> {
                // 移除分类
                storedItem.setCategoryId(null);
                module.getDatabase().saveItem(storedItem);
                // 返回编辑页
                parentGui.refresh();
                parentGui.open();
            }
            case '<' -> {
                if (page > 0) {
                    page--;
                    refreshInventory();
                }
            }
            case '>' -> {
                if (hasNextPage()) {
                    page++;
                    refreshInventory();
                }
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handleCategoryClick(int index) {
        if (index >= categories.size()) return;

        ItemCategory category = categories.get(index);

        // 设置分类
        storedItem.setCategoryId(category.getId());
        module.getDatabase().saveItem(storedItem);

        // 返回编辑页
        parentGui.refresh();
        parentGui.open();
    }
}
