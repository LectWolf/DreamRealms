package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.ItemCategory;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品列表 GUI
 */
public class ItemListGui extends AbstractInteractiveGui<ItemListMenuConfig> {

    private final ItemManagerModule module;
    @Nullable
    private final ItemCategory category;
    private List<StoredItem> items;
    private int page = 0;
    private int slotsPerPage = 0;

    public ItemListGui(Player player, ItemListMenuConfig config, @Nullable ItemCategory category) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.category = category;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        // 加载物品数据
        items = module.getDatabase().getItemsByCategory(category != null ? category.getId() : null);

        this.inventory = config.createInventory(this, player);

        // 计算每页槽位数
        slotsPerPage = countSlots('I');

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
                case 'I' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    inventory.setItem(i, getItemDisplay(index));
                }
                case 'A' -> {
                    AbstractMenuConfig.Icon icon = config.getAddItemIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                case '<' -> {
                    if (page > 0) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        inventory.setItem(i, null);
                    }
                }
                case '>' -> {
                    if (hasNextPage()) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        inventory.setItem(i, null);
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private boolean hasNextPage() {
        return (page + 1) * slotsPerPage < items.size();
    }

    private ItemStack getItemDisplay(int index) {
        if (index >= items.size()) {
            return null;
        }

        StoredItem storedItem = items.get(index);
        ItemStack display = storedItem.getItemStack().clone();
        
        // 统一显示数量为 1
        display.setAmount(1);

        // 添加操作提示到 Lore
        List<String> lore = ItemStackUtil.getItemLore(display);
        if (lore == null) lore = new ArrayList<>();

        lore.add("");
        lore.add(ColorHelper.parseColor("&7标识: &f" + storedItem.getDisplayIdentifier()));
        lore.add(ColorHelper.parseColor("&7序列化: " + (storedItem.isSerialized() ? "&a是" : "&c否")));
        lore.add("");
        lore.add(ColorHelper.parseColor(ItemManagerMessages.gui__item_edit.str()));
        lore.add(ColorHelper.parseColor(ItemManagerMessages.gui__item_get.str()));
        lore.add(ColorHelper.parseColor(ItemManagerMessages.gui__item_delete.str()));
        lore.add(ColorHelper.parseColor(ItemManagerMessages.gui__item_sort.str()));

        ItemStackUtil.setItemLore(display, lore);
        return display;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("ItemListGui click: key=" + key + ", index=" + index + ", click=" + click);

        switch (key) {
            case 'I' -> handleItemClick(click, index + page * slotsPerPage);
            case 'A' -> {
                // 添加按钮 - 点击添加手持物品 (不删除手持物品)
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    // 手持有物品，复制一份添加到列表
                    StoredItem storedItem = StoredItem.create(cursor.clone());
                    if (category != null) {
                        storedItem.setCategoryId(category.getId());
                    }
                    storedItem.setSortOrder(items.size());
                    module.getDatabase().saveItem(storedItem);
                    items.add(storedItem);
                    
                    refreshInventory();
                    ItemManagerMessages.item__added.t(player);
                }
            }
            case 'R' -> {
                // 设置分类图标 - 手持物品点击设置
                if (category != null) {
                    ItemStack cursor = event.getCursor();
                    if (cursor != null && !cursor.getType().isAir()) {
                        // 设置分类图标为手持物品的材质
                        category.setIcon(cursor.getType().name());
                        module.getDatabase().saveCategory(category);
                        ItemManagerMessages.category__icon_set.t(player);
                    }
                }
            }
            case 'B' -> {
                // 返回按钮
                new CategoryGui(player, module.getCategoryMenuConfig()).open();
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

    private void handleItemClick(ClickType click, int index) {
        if (index >= items.size()) return;

        StoredItem storedItem = items.get(index);

        if (click == ClickType.LEFT) {
            // 左键 - 编辑
            new ItemEditGui(player, module.getItemEditMenuConfig(), storedItem, this).open();
        } else if (click == ClickType.RIGHT) {
            // 右键 - 获取 (只给 1 个)
            ItemStack item = storedItem.getItemStack().clone();
            item.setAmount(1);
            Util.giveItem(player, item);
        } else if (click == ClickType.DROP) {
            // Q键 - 删除
            module.getDatabase().deleteItem(storedItem.getGuid());
            items.remove(index);
            refreshInventory();
        } else if (click == ClickType.SHIFT_LEFT && index > 0) {
            // Shift+左键 - 前移
            swapItemOrder(index, index - 1);
            refreshInventory();
        } else if (click == ClickType.SHIFT_RIGHT && index < items.size() - 1) {
            // Shift+右键 - 后移
            swapItemOrder(index, index + 1);
            refreshInventory();
        }
    }

    private void swapItemOrder(int index1, int index2) {
        StoredItem item1 = items.get(index1);
        StoredItem item2 = items.get(index2);

        int order1 = item1.getSortOrder();
        int order2 = item2.getSortOrder();

        item1.setSortOrder(order2);
        item2.setSortOrder(order1);

        module.getDatabase().saveItem(item1);
        module.getDatabase().saveItem(item2);

        // 重新排序列表
        items = module.getDatabase().getItemsByCategory(category != null ? category.getId() : null);
    }

    @Override
    protected void onItemPlaced(int slot, ItemStack item) {
        // 物品放入添加按钮 - 添加新物品
        Character key = config.getSlotKey(slot);
        if (key != null && key == 'A') {
            StoredItem storedItem = StoredItem.create(item);
            if (category != null) {
                storedItem.setCategoryId(category.getId());
            }
            storedItem.setSortOrder(items.size());
            module.getDatabase().saveItem(storedItem);
            items.add(storedItem);

            // 清空放入的物品
            placedItems.remove(slot);
            inventory.setItem(slot, config.getAddItemIcon().generateIcon(player));

            refreshInventory();
        }
    }

    /**
     * 刷新界面 (供编辑页返回时调用)
     */
    public void refresh() {
        items = module.getDatabase().getItemsByCategory(category != null ? category.getId() : null);
        refreshInventory();
    }
}
