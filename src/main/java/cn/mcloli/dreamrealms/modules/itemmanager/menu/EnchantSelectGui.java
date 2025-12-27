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
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 附魔选择 GUI
 */
public class EnchantSelectGui extends AbstractInteractiveGui<EnchantSelectMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final EnchantEditGui parentGui;
    private List<Enchantment> availableEnchants;
    private int page = 0;
    private int slotsPerPage = 0;

    public EnchantSelectGui(Player player, EnchantSelectMenuConfig config, StoredItem storedItem, EnchantEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
        loadAvailableEnchants();
    }

    private void loadAvailableEnchants() {
        // 获取物品当前没有的附魔
        ItemStack item = storedItem.getItemStack();
        this.availableEnchants = new ArrayList<>();
        
        for (Enchantment enchant : EnchantmentUtil.getAllEnchantments()) {
            if (!item.containsEnchantment(enchant)) {
                availableEnchants.add(enchant);
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
                    inventory.setItem(i, getEnchantItem(index));
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
        return (page + 1) * slotsPerPage < availableEnchants.size();
    }

    private ItemStack getEnchantItem(int index) {
        if (index >= availableEnchants.size()) {
            return null;
        }

        Enchantment enchant = availableEnchants.get(index);
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
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

        switch (key) {
            case 'E' -> handleEnchantClick(index + page * slotsPerPage);
            case 'B' -> {
                // 返回
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
