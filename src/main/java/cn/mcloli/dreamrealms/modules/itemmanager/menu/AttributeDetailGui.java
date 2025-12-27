package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.AttributeUtil;
import cn.mcloli.dreamrealms.utils.ChatInputUtil;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性详情编辑 GUI
 */
public class AttributeDetailGui extends AbstractInteractiveGui<AttributeDetailMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final AttributeEditGui parentGui;
    private Attribute attribute;
    private AttributeModifier modifier;

    public AttributeDetailGui(Player player, AttributeDetailMenuConfig config, StoredItem storedItem,
                              Attribute attribute, AttributeModifier modifier, AttributeEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.attribute = attribute;
        this.modifier = modifier;
        this.parentGui = parentGui;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = config.createInventory(this, player);
        refreshInventory();
        return inventory;
    }

    private void refreshInventory() {
        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'I' -> inventory.setItem(i, getPreviewItem());
                case 'V' -> inventory.setItem(i, getValueItem());
                case 'O' -> inventory.setItem(i, getOperationItem());
                case 'S' -> inventory.setItem(i, getSlotItem());
                case 'D' -> inventory.setItem(i, getDeleteItem());
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }


    private ItemStack getPreviewItem() {
        // 尝试获取分类图标
        String attrKey = attribute.getKey().getKey();
        ItemStack item = module.getAttributeSelectMenuConfig().getCategoryIconForAttribute(attrKey);
        if (item == null) {
            item = new ItemStack(Material.NETHER_STAR);
        }
        
        String attrName = AttributeUtil.getAttributeName(attribute);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&b" + attrName));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7数值: &f" + formatValue()));
        lore.add(ColorHelper.parseColor("&7操作: &f" + getOperationName(modifier.getOperation())));
        lore.add(ColorHelper.parseColor("&7槽位: &f" + AttributeUtil.getSlotName(modifier.getSlotGroup())));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private ItemStack getValueItem() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&e修改数值"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7当前: &f" + formatValue()));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private ItemStack getOperationItem() {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&e修改操作类型"));

        List<String> lore = new ArrayList<>();
        lore.add("");
        for (AttributeModifier.Operation op : AttributeModifier.Operation.values()) {
            String prefix = op == modifier.getOperation() ? "&a▸ " : "&7  ";
            lore.add(ColorHelper.parseColor(prefix + getOperationName(op)));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private ItemStack getSlotItem() {
        ItemStack item = new ItemStack(Material.ARMOR_STAND);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&e修改槽位"));

        List<String> lore = new ArrayList<>();
        lore.add("");
        for (EquipmentSlotGroup slot : SLOT_OPTIONS) {
            String prefix = slot.equals(modifier.getSlotGroup()) ? "&a▸ " : "&7  ";
            lore.add(ColorHelper.parseColor(prefix + AttributeUtil.getSlotName(slot)));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private ItemStack getDeleteItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&c删除属性"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7点击删除此属性"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private String formatValue() {
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

    private static final EquipmentSlotGroup[] SLOT_OPTIONS = {
            EquipmentSlotGroup.ANY,
            EquipmentSlotGroup.MAINHAND,
            EquipmentSlotGroup.OFFHAND,
            EquipmentSlotGroup.HAND,
            EquipmentSlotGroup.HEAD,
            EquipmentSlotGroup.CHEST,
            EquipmentSlotGroup.LEGS,
            EquipmentSlotGroup.FEET,
            EquipmentSlotGroup.ARMOR
    };

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("AttributeDetailGui click: key=" + key + ", click=" + click);

        switch (key) {
            case 'V' -> handleValueEdit();
            case 'O' -> handleOperationChange();
            case 'S' -> handleSlotChange();
            case 'D' -> handleDelete();
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handleValueEdit() {
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__attribute_value.str(), input -> {
            if (input != null) {
                Util.parseDouble(input).ifPresent(newValue -> {
                    updateModifier(newValue, modifier.getOperation(), modifier.getSlotGroup());
                    ItemManagerMessages.attribute__modified.t(player);
                });
            }
            new AttributeDetailGui(player, config, storedItem, attribute, modifier, parentGui).open();
        });
    }

    private void handleOperationChange() {
        AttributeModifier.Operation nextOp = getNextOperation(modifier.getOperation());
        updateModifier(modifier.getAmount(), nextOp, modifier.getSlotGroup());
        refreshInventory();
        ItemManagerMessages.attribute__modified.t(player);
    }

    private void handleSlotChange() {
        EquipmentSlotGroup nextSlot = getNextSlot(modifier.getSlotGroup());
        updateModifier(modifier.getAmount(), modifier.getOperation(), nextSlot);
        refreshInventory();
        ItemManagerMessages.attribute__modified.t(player);
    }

    private void handleDelete() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.removeAttributeModifier(attribute, modifier);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            ItemManagerMessages.attribute__removed.t(player);
        }
        parentGui.refresh();
        parentGui.open();
    }

    private void updateModifier(double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.removeAttributeModifier(attribute, modifier);
            AttributeModifier newModifier = new AttributeModifier(
                    modifier.getKey(),
                    amount,
                    operation,
                    slot
            );
            meta.addAttributeModifier(attribute, newModifier);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            this.modifier = newModifier;
        }
    }

    private AttributeModifier.Operation getNextOperation(AttributeModifier.Operation current) {
        AttributeModifier.Operation[] ops = AttributeModifier.Operation.values();
        for (int i = 0; i < ops.length; i++) {
            if (ops[i] == current) {
                return ops[(i + 1) % ops.length];
            }
        }
        return AttributeModifier.Operation.ADD_NUMBER;
    }

    private EquipmentSlotGroup getNextSlot(EquipmentSlotGroup current) {
        for (int i = 0; i < SLOT_OPTIONS.length; i++) {
            if (SLOT_OPTIONS[i].equals(current)) {
                return SLOT_OPTIONS[(i + 1) % SLOT_OPTIONS.length];
            }
        }
        return EquipmentSlotGroup.ANY;
    }
}
