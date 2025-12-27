package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.ChatInputUtil;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品属性编辑 GUI
 */
public class ItemPropertiesGui extends AbstractInteractiveGui<ItemPropertiesMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemEditGui parentGui;

    public ItemPropertiesGui(Player player, ItemPropertiesMenuConfig config, StoredItem storedItem, ItemEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
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
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'P' -> inventory.setItem(i, item.clone());
                case 'D' -> inventory.setItem(i, getDamageIcon(meta));
                case 'M' -> inventory.setItem(i, getMaxDamageIcon(meta));
                case 'S' -> inventory.setItem(i, getMaxStackIcon(meta));
                case 'U' -> inventory.setItem(i, getUnbreakableIcon(meta));
                case 'H' -> inventory.setItem(i, getHideTooltipIcon(meta));
                case 'G' -> inventory.setItem(i, getGlintIcon(meta));
                case 'F' -> inventory.setItem(i, getFireResistantIcon(meta));
                case 'C' -> inventory.setItem(i, getCustomModelDataIcon(meta));
                case 'N' -> inventory.setItem(i, getItemNameIcon(meta));
                case 'I' -> inventory.setItem(i, getItemModelIcon(meta));
                case 'E' -> inventory.setItem(i, getEnchantableIcon(meta));
                case 'R' -> inventory.setItem(i, getRarityIcon(meta));
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }


    private ItemStack getDamageIcon(ItemMeta meta) {
        ItemStack icon = new ItemStack(Material.IRON_PICKAXE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e当前耐久"));

        List<String> lore = new ArrayList<>();
        if (meta instanceof Damageable damageable) {
            int damage = damageable.getDamage();
            int maxDamage = damageable.hasMaxDamage() ? damageable.getMaxDamage() : storedItem.getItemStack().getType().getMaxDurability();
            int remaining = maxDamage - damage;
            lore.add(ColorHelper.parseColor("&7当前损耗: &f" + damage));
            lore.add(ColorHelper.parseColor("&7剩余耐久: &f" + remaining + " &7/ &f" + maxDamage));
        } else {
            lore.add(ColorHelper.parseColor("&c此物品没有耐久"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getMaxDamageIcon(ItemMeta meta) {
        ItemStack icon = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e最大耐久"));

        List<String> lore = new ArrayList<>();
        if (meta instanceof Damageable damageable) {
            int defaultMax = storedItem.getItemStack().getType().getMaxDurability();
            int maxDamage = damageable.hasMaxDamage() ? damageable.getMaxDamage() : defaultMax;
            lore.add(ColorHelper.parseColor("&7当前: &f" + maxDamage));
            lore.add(ColorHelper.parseColor("&7默认: &7" + defaultMax));
        } else {
            lore.add(ColorHelper.parseColor("&c此物品没有耐久"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        lore.add(ColorHelper.parseColor("&7右键重置为默认"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getMaxStackIcon(ItemMeta meta) {
        ItemStack icon = new ItemStack(Material.CHEST);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e最大堆叠"));

        int defaultMax = storedItem.getItemStack().getType().getMaxStackSize();
        int maxStack = meta != null && meta.hasMaxStackSize() ? meta.getMaxStackSize() : defaultMax;

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7当前: &f" + maxStack));
        lore.add(ColorHelper.parseColor("&7默认: &7" + defaultMax));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        lore.add(ColorHelper.parseColor("&7右键重置为默认"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getUnbreakableIcon(ItemMeta meta) {
        boolean unbreakable = meta != null && meta.isUnbreakable();
        ItemStack icon = new ItemStack(unbreakable ? Material.BEDROCK : Material.COBBLESTONE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e无法破坏"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7状态: " + (unbreakable ? "&a已启用" : "&c已禁用")));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getHideTooltipIcon(ItemMeta meta) {
        boolean hidden = meta != null && meta.isHideTooltip();
        ItemStack icon = new ItemStack(hidden ? Material.BARRIER : Material.OAK_SIGN);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e隐藏提示"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7状态: " + (hidden ? "&a已隐藏" : "&c显示中")));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getGlintIcon(ItemMeta meta) {
        Boolean glint = meta != null && meta.hasEnchantmentGlintOverride() ? meta.getEnchantmentGlintOverride() : null;
        ItemStack icon = new ItemStack(Material.GLOWSTONE_DUST);
        if (glint != null && glint) {
            ItemStackUtil.setGlow(icon);
        }
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e附魔光效"));

        String status = glint == null ? "&7默认" : (glint ? "&a强制开启" : "&c强制关闭");
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7状态: " + status));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getFireResistantIcon(ItemMeta meta) {
        boolean fireResistant = meta != null && meta.isFireResistant();
        ItemStack icon = new ItemStack(fireResistant ? Material.MAGMA_CREAM : Material.SNOWBALL);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e防火"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7状态: " + (fireResistant ? "&a已启用" : "&c已禁用")));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getCustomModelDataIcon(ItemMeta meta) {
        ItemStack icon = new ItemStack(Material.PAINTING);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e自定义模型数据"));

        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasCustomModelData()) {
            lore.add(ColorHelper.parseColor("&7当前: &f" + meta.getCustomModelData()));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &7未设置"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        lore.add(ColorHelper.parseColor("&7右键清除"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getItemNameIcon(ItemMeta meta) {
        ItemStack icon = new ItemStack(Material.NAME_TAG);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e物品名称"));

        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasItemName()) {
            lore.add(ColorHelper.parseColor("&7当前: &f" + meta.getItemName()));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &7未设置"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        lore.add(ColorHelper.parseColor("&7右键清除"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getItemModelIcon(ItemMeta meta) {
        ItemStack icon = new ItemStack(Material.ITEM_FRAME);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e物品模型"));

        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasItemModel()) {
            lore.add(ColorHelper.parseColor("&7当前: &f" + meta.getItemModel().toString()));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &7未设置"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        lore.add(ColorHelper.parseColor("&7右键清除"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getEnchantableIcon(ItemMeta meta) {
        ItemStack icon = new ItemStack(Material.ENCHANTING_TABLE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e附魔可用性"));

        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasEnchantable()) {
            lore.add(ColorHelper.parseColor("&7当前: &f" + meta.getEnchantable()));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &7未设置"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        lore.add(ColorHelper.parseColor("&7右键清除"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getRarityIcon(ItemMeta meta) {
        ItemStack icon = new ItemStack(Material.NETHER_STAR);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e稀有度"));

        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasRarity()) {
            ItemRarity rarity = meta.getRarity();
            String rarityDisplay = switch (rarity) {
                case COMMON -> "&f普通";
                case UNCOMMON -> "&e不常见";
                case RARE -> "&b稀有";
                case EPIC -> "&d史诗";
            };
            lore.add(ColorHelper.parseColor("&7当前: " + rarityDisplay));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &7未设置"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        lore.add(ColorHelper.parseColor("&7右键清除"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }


    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("ItemPropertiesGui click: key=" + key + ", click=" + click);

        switch (key) {
            case 'P' -> handlePreviewClick(click);
            case 'D' -> handleDamageEdit();
            case 'M' -> handleMaxDamageEdit(click);
            case 'S' -> handleMaxStackEdit(click);
            case 'U' -> handleUnbreakableToggle();
            case 'H' -> handleHideTooltipToggle();
            case 'G' -> handleGlintToggle();
            case 'F' -> handleFireResistantToggle();
            case 'C' -> handleCustomModelDataEdit(click);
            case 'N' -> handleItemNameEdit(click);
            case 'I' -> handleItemModelEdit(click);
            case 'E' -> handleEnchantableEdit(click);
            case 'R' -> handleRarityToggle(click);
            case '1' -> handleFoodEdit();
            case '2', '3', '4', '5' -> ItemManagerMessages.properties__wip.t(player);
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    public void refresh() {
        refreshInventory();
    }

    private void handlePreviewClick(ClickType click) {
        if (click == ClickType.LEFT) {
            ItemStack item = storedItem.getItemStack().clone();
            item.setAmount(1);
            Util.giveItem(player, item);
        }
    }

    private void handleFoodEdit() {
        new FoodEditGui(player, module.getFoodEditMenuConfig(), storedItem, this).open();
    }

    private void handleDamageEdit() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) {
            ItemManagerMessages.properties__not_damageable.t(player);
            return;
        }

        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__damage_value.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    ItemMeta m = storedItem.getItemStack().getItemMeta();
                    if (m instanceof Damageable damageable) {
                        // 获取最大耐久，限制损耗不能超过最大耐久
                        int maxDamage = damageable.hasMaxDamage() ? damageable.getMaxDamage() : storedItem.getItemStack().getType().getMaxDurability();
                        int clampedValue = Math.max(0, Math.min(value, maxDamage - 1));
                        damageable.setDamage(clampedValue);
                        storedItem.getItemStack().setItemMeta(m);
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__damage_set.t(player);
                    }
                });
            }
            new ItemPropertiesGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleMaxDamageEdit(ClickType click) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            ItemManagerMessages.properties__not_damageable.t(player);
            return;
        }

        if (click == ClickType.RIGHT) {
            // 右键重置
            damageable.setMaxDamage(null);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshInventory();
            ItemManagerMessages.properties__max_damage_reset.t(player);
            return;
        }

        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__max_damage_value.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    ItemMeta m = storedItem.getItemStack().getItemMeta();
                    if (m instanceof Damageable d) {
                        d.setMaxDamage(Math.max(1, value));
                        storedItem.getItemStack().setItemMeta(m);
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__max_damage_set.t(player);
                    }
                });
            }
            new ItemPropertiesGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleMaxStackEdit(ClickType click) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (click == ClickType.RIGHT) {
            // 右键重置
            meta.setMaxStackSize(null);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshInventory();
            ItemManagerMessages.properties__max_stack_reset.t(player);
            return;
        }

        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__max_stack_value.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    ItemMeta m = storedItem.getItemStack().getItemMeta();
                    if (m != null) {
                        m.setMaxStackSize(Math.max(1, Math.min(99, value)));
                        storedItem.getItemStack().setItemMeta(m);
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__max_stack_set.t(player);
                    }
                });
            }
            new ItemPropertiesGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleUnbreakableToggle() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.setUnbreakable(!meta.isUnbreakable());
        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);
        refreshInventory();
        ItemManagerMessages.properties__unbreakable_toggled.t(player);
    }

    private void handleHideTooltipToggle() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.setHideTooltip(!meta.isHideTooltip());
        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);
        refreshInventory();
        ItemManagerMessages.properties__hide_tooltip_toggled.t(player);
    }

    private void handleGlintToggle() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Boolean current = meta.hasEnchantmentGlintOverride() ? meta.getEnchantmentGlintOverride() : null;
        Boolean next;
        if (current == null) {
            next = true;
        } else if (current) {
            next = false;
        } else {
            next = null;
        }
        meta.setEnchantmentGlintOverride(next);
        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);
        refreshInventory();
        ItemManagerMessages.properties__glint_toggled.t(player);
    }

    private void handleFireResistantToggle() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.setFireResistant(!meta.isFireResistant());
        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);
        refreshInventory();
        ItemManagerMessages.properties__fire_resistant_toggled.t(player);
    }

    private void handleCustomModelDataEdit(ClickType click) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (click == ClickType.RIGHT) {
            meta.setCustomModelData(null);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshInventory();
            ItemManagerMessages.properties__custom_model_data_cleared.t(player);
            return;
        }

        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__custom_model_data.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    ItemMeta m = storedItem.getItemStack().getItemMeta();
                    if (m != null) {
                        m.setCustomModelData(value);
                        storedItem.getItemStack().setItemMeta(m);
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__custom_model_data_set.t(player);
                    }
                });
            }
            new ItemPropertiesGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleItemNameEdit(ClickType click) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (click == ClickType.RIGHT) {
            meta.setItemName(null);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshInventory();
            ItemManagerMessages.properties__item_name_cleared.t(player);
            return;
        }

        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__item_name_value.str(), input -> {
            if (input != null) {
                ItemMeta m = storedItem.getItemStack().getItemMeta();
                if (m != null) {
                    m.setItemName(ColorHelper.parseColor(input));
                    storedItem.getItemStack().setItemMeta(m);
                    module.getDatabase().saveItem(storedItem);
                    ItemManagerMessages.properties__item_name_set.t(player);
                }
            }
            new ItemPropertiesGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleItemModelEdit(ClickType click) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (click == ClickType.RIGHT) {
            meta.setItemModel(null);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshInventory();
            ItemManagerMessages.properties__item_model_cleared.t(player);
            return;
        }

        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__item_model.str(), input -> {
            if (input != null) {
                ItemMeta m = storedItem.getItemStack().getItemMeta();
                if (m != null) {
                    NamespacedKey key = NamespacedKey.fromString(input);
                    if (key != null) {
                        m.setItemModel(key);
                        storedItem.getItemStack().setItemMeta(m);
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__item_model_set.t(player);
                    }
                }
            }
            new ItemPropertiesGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleEnchantableEdit(ClickType click) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (click == ClickType.RIGHT) {
            meta.setEnchantable(null);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshInventory();
            ItemManagerMessages.properties__enchantable_cleared.t(player);
            return;
        }

        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__enchantable.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    ItemMeta m = storedItem.getItemStack().getItemMeta();
                    if (m != null) {
                        m.setEnchantable(Math.max(1, value));
                        storedItem.getItemStack().setItemMeta(m);
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__enchantable_set.t(player);
                    }
                });
            }
            new ItemPropertiesGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleRarityToggle(ClickType click) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (click == ClickType.RIGHT) {
            meta.setRarity(null);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshInventory();
            ItemManagerMessages.properties__rarity_cleared.t(player);
            return;
        }

        // 循环切换: 未设置 -> COMMON -> UNCOMMON -> RARE -> EPIC -> 未设置
        ItemRarity current = meta.hasRarity() ? meta.getRarity() : null;
        ItemRarity next;
        if (current == null) {
            next = ItemRarity.COMMON;
        } else {
            next = switch (current) {
                case COMMON -> ItemRarity.UNCOMMON;
                case UNCOMMON -> ItemRarity.RARE;
                case RARE -> ItemRarity.EPIC;
                case EPIC -> null;
            };
        }
        meta.setRarity(next);
        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);
        refreshInventory();
        ItemManagerMessages.properties__rarity_toggled.t(player);
    }
}
