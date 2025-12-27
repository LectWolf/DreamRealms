package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.ChatInputUtil;
import cn.mcloli.dreamrealms.utils.PaperUtil;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具组件编辑 GUI
 */
public class ToolEditGui extends AbstractInteractiveGui<ToolEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemPropertiesGui parentGui;

    public ToolEditGui(Player player, ToolEditMenuConfig config, StoredItem storedItem, ItemPropertiesGui parentGui) {
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

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'P' -> inventory.setItem(i, item.clone());
                case 'S' -> inventory.setItem(i, getSpeedIcon());
                case 'D' -> inventory.setItem(i, getDamageIcon());
                case 'R' -> inventory.setItem(i, getRulesIcon());
                case 'C' -> inventory.setItem(i, getRemoveIcon());
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private ItemStack getSpeedIcon() {
        ItemStack icon = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e默认挖掘速度"));

        List<String> lore = new ArrayList<>();
        Float speed = PaperUtil.getDefaultMiningSpeed(storedItem.getItemStack());
        if (speed != null) {
            lore.add(ColorHelper.parseColor("&7当前: &f" + String.format("%.2f", speed)));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &f1.0 &8(默认)"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getDamageIcon() {
        ItemStack icon = new ItemStack(Material.ANVIL);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e每方块耐久消耗"));

        List<String> lore = new ArrayList<>();
        Integer damage = PaperUtil.getDamagePerBlock(storedItem.getItemStack());
        if (damage != null) {
            lore.add(ColorHelper.parseColor("&7当前: &f" + damage));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &f1 &8(默认)"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getRulesIcon() {
        ItemStack icon = new ItemStack(Material.BOOK);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e挖掘规则"));

        List<String> lore = new ArrayList<>();
        int ruleCount = PaperUtil.getToolRules(storedItem.getItemStack()).size();
        lore.add(ColorHelper.parseColor("&7当前规则数: &f" + ruleCount));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击编辑规则"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getRemoveIcon() {
        boolean hasTool = PaperUtil.hasTool(storedItem.getItemStack());
        ItemStack icon = new ItemStack(hasTool ? Material.BARRIER : Material.GRAY_DYE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor(hasTool ? "&c移除工具组件" : "&7无工具组件"));

        List<String> lore = new ArrayList<>();
        if (hasTool) {
            lore.add(ColorHelper.parseColor("&7点击移除工具组件"));
        } else {
            lore.add(ColorHelper.parseColor("&7此物品没有工具组件"));
            lore.add(ColorHelper.parseColor("&7修改属性将自动创建"));
        }
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        switch (key) {
            case 'P' -> {
                if (click == ClickType.LEFT) {
                    ItemStack item = storedItem.getItemStack().clone();
                    item.setAmount(1);
                    Util.giveItem(player, item);
                }
            }
            case 'S' -> handleSpeedEdit();
            case 'D' -> handleDamageEdit();
            case 'R' -> handleRulesEdit();
            case 'C' -> handleRemoveTool();
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handleSpeedEdit() {
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__tool_speed.str(), input -> {
            if (input != null) {
                Util.parseFloat(input).ifPresent(value -> {
                    ItemStack item = storedItem.getItemStack();
                    Float currentSpeed = PaperUtil.getDefaultMiningSpeed(item);
                    Integer currentDamage = PaperUtil.getDamagePerBlock(item);
                    List<Object> currentRules = PaperUtil.getToolRules(item);
                    
                    float speed = Math.max(0, value);
                    int damage = currentDamage != null ? currentDamage : 1;
                    
                    if (PaperUtil.setToolProperties(item, speed, damage, currentRules)) {
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__tool_speed_set.t(player);
                    }
                });
            }
            new ToolEditGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleDamageEdit() {
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__tool_damage.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    ItemStack item = storedItem.getItemStack();
                    Float currentSpeed = PaperUtil.getDefaultMiningSpeed(item);
                    List<Object> currentRules = PaperUtil.getToolRules(item);
                    
                    float speed = currentSpeed != null ? currentSpeed : 1.0f;
                    int damage = Math.max(0, value);
                    
                    if (PaperUtil.setToolProperties(item, speed, damage, currentRules)) {
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__tool_damage_set.t(player);
                    }
                });
            }
            new ToolEditGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleRulesEdit() {
        new ToolRuleEditGui(player, module.getToolRuleEditMenuConfig(), storedItem, this).open();
    }

    private void handleRemoveTool() {
        if (PaperUtil.hasTool(storedItem.getItemStack())) {
            if (PaperUtil.removeTool(storedItem.getItemStack())) {
                module.getDatabase().saveItem(storedItem);
                refreshInventory();
                ItemManagerMessages.properties__tool_removed.t(player);
            }
        }
    }

    public void refresh() {
        refreshInventory();
    }
}
