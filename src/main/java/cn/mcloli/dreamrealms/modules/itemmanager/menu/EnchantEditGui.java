package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
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
import java.util.Map;

/**
 * 附魔编辑 GUI
 */
public class EnchantEditGui extends AbstractInteractiveGui<EnchantEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemEditGui parentGui;
    private List<Map.Entry<Enchantment, Integer>> enchantList;
    private int page = 0;
    private int slotsPerPage = 0;

    public EnchantEditGui(Player player, EnchantEditMenuConfig config, StoredItem storedItem, ItemEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
        refreshEnchantList();
    }

    private void refreshEnchantList() {
        ItemStack item = storedItem.getItemStack();
        Map<Enchantment, Integer> enchants = item.getEnchantments();
        this.enchantList = new ArrayList<>(enchants.entrySet());
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
                case 'A' -> {
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
        return (page + 1) * slotsPerPage < enchantList.size();
    }

    private ItemStack getEnchantItem(int index) {
        if (index >= enchantList.size()) {
            return null;
        }

        Map.Entry<Enchantment, Integer> entry = enchantList.get(index);
        Enchantment enchant = entry.getKey();
        int level = entry.getValue();

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        String enchantName = EnchantmentUtil.getEnchantmentName(enchant);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&b" + enchantName));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7等级: &f" + level + " &7/ &f" + enchant.getMaxLevel()));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e左键增加等级"));
        lore.add(ColorHelper.parseColor("&c右键减少等级"));
        lore.add(ColorHelper.parseColor("&cCtrl+Q删除"));
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("EnchantEditGui click: key=" + key + ", index=" + index + ", click=" + click);

        switch (key) {
            case 'E' -> handleEnchantClick(click, index + page * slotsPerPage);
            case 'A' -> handleAddEnchant();
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

    private void handleEnchantClick(ClickType click, int index) {
        if (index >= enchantList.size()) return;

        Map.Entry<Enchantment, Integer> entry = enchantList.get(index);
        Enchantment enchant = entry.getKey();
        int currentLevel = entry.getValue();

        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (click == ClickType.LEFT) {
            // 左键 - 增加等级
            int newLevel = currentLevel + 1;
            meta.removeEnchant(enchant);
            meta.addEnchant(enchant, newLevel, true);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshEnchantList();
            refreshInventory();
            ItemManagerMessages.enchant__level_increased.t(player);
        } else if (click == ClickType.RIGHT) {
            // 右键 - 减少等级
            if (currentLevel > 1) {
                int newLevel = currentLevel - 1;
                meta.removeEnchant(enchant);
                meta.addEnchant(enchant, newLevel, true);
                item.setItemMeta(meta);
                module.getDatabase().saveItem(storedItem);
                refreshEnchantList();
                refreshInventory();
                ItemManagerMessages.enchant__level_decreased.t(player);
            }
        } else if (click == ClickType.CONTROL_DROP) {
            // Ctrl+Q - 删除附魔
            meta.removeEnchant(enchant);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshEnchantList();
            refreshInventory();
            ItemManagerMessages.enchant__removed.t(player);
        }
    }

    private void handleAddEnchant() {
        // 打开附魔选择菜单
        new EnchantSelectGui(player, module.getEnchantSelectMenuConfig(), storedItem, this).open();
    }

    /**
     * 刷新界面 (供附魔选择返回时调用)
     */
    public void refresh() {
        refreshEnchantList();
        refreshInventory();
    }
}
