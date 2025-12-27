package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.AttributeUtil;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 属性编辑 GUI
 */
public class AttributeEditGui extends AbstractInteractiveGui<AttributeEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemEditGui parentGui;
    private List<Map.Entry<Attribute, AttributeModifier>> attributeList;
    private int page = 0;
    private int slotsPerPage = 0;

    public AttributeEditGui(Player player, AttributeEditMenuConfig config, StoredItem storedItem, ItemEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
        refreshAttributeList();
    }

    private void refreshAttributeList() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        this.attributeList = new ArrayList<>();
        
        if (meta != null && meta.hasAttributeModifiers()) {
            Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
            if (modifiers != null) {
                for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                    attributeList.add(entry);
                }
            }
        }
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = config.createInventory(this, player);
        slotsPerPage = countSlots('A');
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
                case 'A' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    inventory.setItem(i, getAttributeItem(index));
                }
                case 'N' -> {
                    AbstractMenuConfig.Icon icon = config.getAddIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                case '<' -> {
                    if (page > 0) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        inventory.setItem(i, config.getEmptyPrevIcon(player));
                    }
                }
                case '>' -> {
                    if (hasNextPage()) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        inventory.setItem(i, config.getEmptyNextIcon(player));
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private boolean hasNextPage() {
        return (page + 1) * slotsPerPage < attributeList.size();
    }

    private ItemStack getAttributeItem(int index) {
        if (index >= attributeList.size()) {
            return null;
        }

        Map.Entry<Attribute, AttributeModifier> entry = attributeList.get(index);
        Attribute attr = entry.getKey();
        AttributeModifier modifier = entry.getValue();

        // 尝试获取分类图标
        String attrKey = attr.getKey().getKey();
        ItemStack item = module.getAttributeSelectMenuConfig().getCategoryIconForAttribute(attrKey);
        if (item == null) {
            item = new ItemStack(Material.NETHER_STAR);
        }
        
        String attrName = AttributeUtil.getAttributeName(attr);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&b" + attrName));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7数值: &f" + formatValue(modifier)));
        lore.add(ColorHelper.parseColor("&7操作: &f" + getOperationName(modifier.getOperation())));
        lore.add(ColorHelper.parseColor("&7槽位: &f" + AttributeUtil.getSlotName(modifier.getSlotGroup())));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e左键编辑"));
        lore.add(ColorHelper.parseColor("&cCtrl+Q删除"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private String formatValue(AttributeModifier modifier) {
        double amount = modifier.getAmount();
        return switch (modifier.getOperation()) {
            case ADD_NUMBER -> String.format("%+.2f", amount);
            case ADD_SCALAR -> String.format("%+.0f%%", amount * 100);
            case MULTIPLY_SCALAR_1 -> String.format("×%.2f", 1 + amount);
        };
    }

    private String getOperationName(AttributeModifier.Operation operation) {
        return switch (operation) {
            case ADD_NUMBER -> "加法";
            case ADD_SCALAR -> "百分比加成";
            case MULTIPLY_SCALAR_1 -> "乘法";
        };
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("AttributeEditGui click: key=" + key + ", index=" + index + ", click=" + click);

        switch (key) {
            case 'A' -> handleAttributeClick(click, index + page * slotsPerPage);
            case 'N' -> handleAddAttribute();
            case 'B' -> {
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

    private void handleAttributeClick(ClickType click, int index) {
        if (index >= attributeList.size()) return;

        Map.Entry<Attribute, AttributeModifier> entry = attributeList.get(index);
        Attribute attr = entry.getKey();
        AttributeModifier modifier = entry.getValue();

        if (click == ClickType.LEFT) {
            // 左键 - 打开详情编辑菜单
            new AttributeDetailGui(player, module.getAttributeDetailMenuConfig(), storedItem, attr, modifier, this).open();
        } else if (click == ClickType.CONTROL_DROP) {
            // Ctrl+Q - 删除
            ItemStack item = storedItem.getItemStack();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.removeAttributeModifier(attr, modifier);
                item.setItemMeta(meta);
                module.getDatabase().saveItem(storedItem);
                refreshAttributeList();
                refreshInventory();
                ItemManagerMessages.attribute__removed.t(player);
            }
        }
    }

    private void handleAddAttribute() {
        new AttributeSelectGui(player, module.getAttributeSelectMenuConfig(), storedItem, this).open();
    }

    public void refresh() {
        refreshAttributeList();
        refreshInventory();
    }
}
