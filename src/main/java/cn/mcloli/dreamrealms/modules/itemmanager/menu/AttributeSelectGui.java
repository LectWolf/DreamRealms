package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.AttributeUtil;
import cn.mcloli.dreamrealms.utils.ChatInputUtil;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
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

import java.util.*;

/**
 * 属性选择 GUI (支持分类)
 */
public class AttributeSelectGui extends AbstractInteractiveGui<AttributeSelectMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final AttributeEditGui parentGui;
    
    // 视图状态
    private ViewState viewState = ViewState.CATEGORIES;
    private String currentCategory = null;
    private List<Attribute> currentAttributes = new ArrayList<>();
    private int page = 0;
    private int slotsPerPage = 0;

    private enum ViewState {
        CATEGORIES,     // 显示分类列表
        ATTRIBUTES      // 显示属性列表
    }

    public AttributeSelectGui(Player player, AttributeSelectMenuConfig config, StoredItem storedItem, AttributeEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
        
        // Debug 模式输出所有属性
        if (module.isDebug()) {
            module.info("[DEBUG] 所有属性键名:");
            for (Attribute attr : Registry.ATTRIBUTE) {
                module.info("  - " + attr.getKey().getKey());
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
                    if (viewState == ViewState.CATEGORIES) {
                        inventory.setItem(i, getCategoryItem(index));
                    } else {
                        inventory.setItem(i, getAttributeItem(index));
                    }
                }
                case 'V' -> {
                    // 查看所有属性按钮
                    AbstractMenuConfig.Icon icon = config.getViewAllIcon();
                    if (icon != null && viewState == ViewState.CATEGORIES) {
                        inventory.setItem(i, icon.generateIcon(player));
                    } else {
                        inventory.setItem(i, null);
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
        int totalItems = viewState == ViewState.CATEGORIES 
                ? config.getCategories().size() 
                : currentAttributes.size();
        return (page + 1) * slotsPerPage < totalItems;
    }

    private ItemStack getCategoryItem(int index) {
        List<AttributeSelectMenuConfig.AttributeCategory> categories = config.getCategories();
        if (index >= categories.size()) {
            return null;
        }

        AttributeSelectMenuConfig.AttributeCategory category = categories.get(index);
        ItemStack item = category.icon().clone();
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&e" + category.name()));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7包含 &f" + category.attributes().size() + " &7个属性"));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击查看"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private ItemStack getAttributeItem(int index) {
        if (index >= currentAttributes.size()) {
            return null;
        }

        Attribute attr = currentAttributes.get(index);
        String attrKey = attr.getKey().getKey();
        
        // 尝试获取分类图标
        ItemStack item = config.getCategoryIconForAttribute(attrKey);
        if (item == null) {
            item = new ItemStack(Material.NETHER_STAR);
        }
        
        String attrName = AttributeUtil.getAttributeName(attr);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&b" + attrName));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7键: &f" + attrKey));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击添加"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("AttributeSelectGui click: key=" + key + ", index=" + index + ", click=" + click + ", viewState=" + viewState);

        switch (key) {
            case 'A' -> {
                int itemIndex = index + page * slotsPerPage;
                if (viewState == ViewState.CATEGORIES) {
                    handleCategorySelect(itemIndex);
                } else {
                    handleAttributeSelect(itemIndex);
                }
            }
            case 'V' -> {
                if (viewState == ViewState.CATEGORIES) {
                    showAllAttributes();
                }
            }
            case 'B' -> handleBack();
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

    private void handleCategorySelect(int index) {
        List<AttributeSelectMenuConfig.AttributeCategory> categories = config.getCategories();
        if (index >= categories.size()) return;

        AttributeSelectMenuConfig.AttributeCategory category = categories.get(index);
        currentCategory = category.id();
        currentAttributes = getAttributesForCategory(category);
        viewState = ViewState.ATTRIBUTES;
        page = 0;
        refreshInventory();
    }

    private List<Attribute> getAttributesForCategory(AttributeSelectMenuConfig.AttributeCategory category) {
        List<Attribute> result = new ArrayList<>();
        for (String attrKey : category.attributes()) {
            for (Attribute attr : Registry.ATTRIBUTE) {
                if (attr.getKey().getKey().equals(attrKey)) {
                    result.add(attr);
                    break;
                }
            }
        }
        return result;
    }

    private void showAllAttributes() {
        currentCategory = null;
        currentAttributes = AttributeUtil.getAllAttributes();
        viewState = ViewState.ATTRIBUTES;
        page = 0;
        refreshInventory();
    }

    private void handleBack() {
        if (viewState == ViewState.ATTRIBUTES) {
            // 返回分类列表
            viewState = ViewState.CATEGORIES;
            currentCategory = null;
            currentAttributes.clear();
            page = 0;
            refreshInventory();
        } else {
            // 返回属性编辑页
            parentGui.refresh();
            parentGui.open();
        }
    }

    private void handleAttributeSelect(int index) {
        if (index >= currentAttributes.size()) return;

        Attribute attr = currentAttributes.get(index);
        player.closeInventory();

        // 输入数值
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__attribute_value.str(), valueInput -> {
            if (valueInput == null) {
                parentGui.refresh();
                parentGui.open();
                return;
            }

            Util.parseDouble(valueInput).ifPresentOrElse(value -> {
                // 添加属性
                ItemStack item = storedItem.getItemStack();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    NamespacedKey modifierKey = new NamespacedKey("dreamrealms", 
                            "attr_" + attr.getKey().getKey() + "_" + UUID.randomUUID().toString().substring(0, 8));
                    AttributeModifier modifier = new AttributeModifier(
                            modifierKey,
                            value,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.ANY
                    );
                    meta.addAttributeModifier(attr, modifier);
                    item.setItemMeta(meta);
                    module.getDatabase().saveItem(storedItem);
                    ItemManagerMessages.attribute__added.t(player);
                }
                parentGui.refresh();
                parentGui.open();
            }, () -> {
                ItemManagerMessages.input__invalid_number.t(player);
                parentGui.refresh();
                parentGui.open();
            });
        });
    }
}
