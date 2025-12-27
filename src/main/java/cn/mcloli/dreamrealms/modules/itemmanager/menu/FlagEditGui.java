package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Flag 编辑 GUI
 */
public class FlagEditGui extends AbstractInteractiveGui<FlagEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemEditGui parentGui;

    public FlagEditGui(Player player, FlagEditMenuConfig config, StoredItem storedItem, ItemEditGui parentGui) {
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
        ItemFlag[] flags = ItemFlag.values();
        int flagIndex = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'F' -> {
                    if (flagIndex < flags.length) {
                        inventory.setItem(i, getFlagItem(flags[flagIndex]));
                        flagIndex++;
                    } else {
                        inventory.setItem(i, null);
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private ItemStack getFlagItem(ItemFlag flag) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        boolean hasFlag = meta != null && meta.hasItemFlag(flag);

        ItemStack icon = new ItemStack(hasFlag ? Material.LIME_DYE : Material.GRAY_DYE);
        String flagName = getFlagDisplayName(flag);
        String status = hasFlag ? "&a已启用" : "&7已禁用";
        
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor((hasFlag ? "&a" : "&7") + flagName));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7状态: " + status));
        lore.add("");
        lore.add(ColorHelper.parseColor("&7" + getFlagDescription(flag)));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        ItemStackUtil.setItemLore(icon, lore);

        return icon;
    }


    private String getFlagDisplayName(ItemFlag flag) {
        return switch (flag.name()) {
            case "HIDE_ENCHANTS" -> "隐藏附魔";
            case "HIDE_ATTRIBUTES" -> "隐藏属性";
            case "HIDE_UNBREAKABLE" -> "隐藏无法破坏";
            case "HIDE_DESTROYS" -> "隐藏可破坏方块";
            case "HIDE_PLACED_ON" -> "隐藏可放置方块";
            case "HIDE_ADDITIONAL_TOOLTIP" -> "隐藏额外提示";
            case "HIDE_DYE" -> "隐藏染色";
            case "HIDE_ARMOR_TRIM" -> "隐藏盔甲纹饰";
            case "HIDE_STORED_ENCHANTS" -> "隐藏存储的附魔";
            default -> flag.name();
        };
    }

    private String getFlagDescription(ItemFlag flag) {
        return switch (flag.name()) {
            case "HIDE_ENCHANTS" -> "隐藏物品的附魔信息";
            case "HIDE_ATTRIBUTES" -> "隐藏物品的属性修饰符";
            case "HIDE_UNBREAKABLE" -> "隐藏无法破坏标签";
            case "HIDE_DESTROYS" -> "隐藏可破坏的方块列表";
            case "HIDE_PLACED_ON" -> "隐藏可放置的方块列表";
            case "HIDE_ADDITIONAL_TOOLTIP" -> "隐藏额外的提示信息";
            case "HIDE_DYE" -> "隐藏皮革装备的染色信息";
            case "HIDE_ARMOR_TRIM" -> "隐藏盔甲纹饰信息";
            case "HIDE_STORED_ENCHANTS" -> "隐藏附魔书存储的附魔";
            default -> "未知标志";
        };
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("FlagEditGui click: key=" + key + ", index=" + index);

        switch (key) {
            case 'F' -> handleFlagClick(index);
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handleFlagClick(int flagIndex) {
        // index 就是第几个 F 槽位，直接对应 flag 索引
        ItemFlag[] flags = ItemFlag.values();
        if (flagIndex >= 0 && flagIndex < flags.length) {
            toggleFlag(flags[flagIndex]);
        }
    }

    private void toggleFlag(ItemFlag flag) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta.hasItemFlag(flag)) {
            meta.removeItemFlags(flag);
            ItemManagerMessages.flag__removed.t(player);
        } else {
            meta.addItemFlags(flag);
            ItemManagerMessages.flag__added.t(player);
        }

        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);
        refreshInventory();
    }
}
