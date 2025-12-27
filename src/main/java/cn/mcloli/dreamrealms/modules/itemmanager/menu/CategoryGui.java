package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.ItemCategory;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.ChatInputUtil;
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
 * 分类列表 GUI
 */
public class CategoryGui extends AbstractInteractiveGui<CategoryMenuConfig> {

    private final ItemManagerModule module;
    private List<ItemCategory> categories;
    private int page = 0;
    private int slotsPerPage = 0;

    public CategoryGui(Player player, CategoryMenuConfig config) {
        super(player, config);
        this.module = config.getModule();
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        // 加载分类数据
        categories = module.getDatabase().getAllCategories();

        // 先创建 inventory
        this.inventory = config.createInventory(this, player);

        // 再计算每页槽位数
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
                    AbstractMenuConfig.Icon icon = config.getAddCategoryIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                case 'U' -> {
                    AbstractMenuConfig.Icon icon = config.getUncategorizedIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                case '<' -> {
                    // 上一页
                    if (page > 0) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        // 显示空图标
                        inventory.setItem(i, config.getEmptyPrevIcon(player));
                    }
                }
                case '>' -> {
                    // 下一页
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
        lore.add(ColorHelper.parseColor(ItemManagerMessages.gui__category_edit.str()));
        lore.add(ColorHelper.parseColor(ItemManagerMessages.gui__category_sort.str()));
        lore.add(ColorHelper.parseColor(ItemManagerMessages.gui__category_delete.str()));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("CategoryGui click: key=" + key + ", index=" + index + ", click=" + click);

        switch (key) {
            case 'C' -> handleCategoryClick(click, index + page * slotsPerPage);
            case 'A' -> handleAddCategory();
            case 'U' -> handleUncategorized();
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

    private void handleCategoryClick(ClickType click, int index) {
        if (index >= categories.size()) return;

        ItemCategory category = categories.get(index);

        if (click == ClickType.LEFT) {
            // 左键 - 打开分类
            new ItemListGui(player, module.getItemListMenuConfig(), category).open();
        } else if (click == ClickType.CONTROL_DROP) {
            // Shift+Q - 删除分类
            module.getDatabase().deleteCategory(category.getId());
            categories.remove(index);
            refreshInventory();
        } else if (click == ClickType.SHIFT_LEFT && index > 0) {
            // Shift+左键 - 前移
            swapCategoryOrder(index, index - 1);
            refreshInventory();
        } else if (click == ClickType.SHIFT_RIGHT && index < categories.size() - 1) {
            // Shift+右键 - 后移
            swapCategoryOrder(index, index + 1);
            refreshInventory();
        }
    }

    private void swapCategoryOrder(int index1, int index2) {
        ItemCategory cat1 = categories.get(index1);
        ItemCategory cat2 = categories.get(index2);

        int order1 = cat1.getSortOrder();
        int order2 = cat2.getSortOrder();

        cat1.setSortOrder(order2);
        cat2.setSortOrder(order1);

        module.getDatabase().saveCategory(cat1);
        module.getDatabase().saveCategory(cat2);

        // 重新排序列表
        categories = module.getDatabase().getAllCategories();
    }

    private void handleAddCategory() {
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__category_name.str(), input -> {
            if (input != null) {
                ItemCategory category = ItemCategory.create(input);
                category.setSortOrder(categories.size());
                module.getDatabase().saveCategory(category);
            }
            // 重新打开菜单
            new CategoryGui(player, config).open();
        });
    }

    private void handleUncategorized() {
        // 打开未分类物品列表
        new ItemListGui(player, module.getItemListMenuConfig(), null).open();
    }
}
