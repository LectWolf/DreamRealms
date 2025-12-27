package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
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
 * 工具挖掘规则编辑 GUI
 */
public class ToolRuleEditGui extends AbstractInteractiveGui<ToolRuleEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ToolEditGui parentGui;
    private int page = 0;
    private int slotsPerPage = 0;

    public ToolRuleEditGui(Player player, ToolRuleEditMenuConfig config, StoredItem storedItem, ToolEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
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

    private List<Object> getRules() {
        return PaperUtil.getToolRules(storedItem.getItemStack());
    }

    private boolean hasNextPage() {
        return (page + 1) * slotsPerPage < getRules().size();
    }

    private void refreshInventory() {
        ItemStack item = storedItem.getItemStack();
        List<Object> rules = getRules();

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'P' -> inventory.setItem(i, item.clone());
                case 'E' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    inventory.setItem(i, getRuleItem(rules, index));
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

    private ItemStack getRuleItem(List<Object> rules, int index) {
        if (index >= rules.size()) {
            return null;
        }

        Object rule = rules.get(index);
        ItemStack icon = new ItemStack(Material.PAPER);
        
        String blocks = PaperUtil.getRuleBlockTypes(rule);
        Float speed = PaperUtil.getRuleSpeed(rule);
        Boolean correct = PaperUtil.getRuleCorrectForDrops(rule);
        
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&b规则 #" + (index + 1)));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7方块: &f" + (blocks != null ? truncate(blocks, 30) : "未设置")));
        lore.add(ColorHelper.parseColor("&7速度: &f" + (speed != null ? String.format("%.2f", speed) : "默认")));
        lore.add(ColorHelper.parseColor("&7正确工具: &f" + (correct == null ? "默认" : (correct ? "是" : "否"))));
        lore.add("");
        lore.add(ColorHelper.parseColor("&cCtrl+Q删除"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private String truncate(String str, int maxLen) {
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        List<Object> rules = getRules();

        switch (key) {
            case 'P' -> {
                if (click == ClickType.LEFT) {
                    ItemStack item = storedItem.getItemStack().clone();
                    item.setAmount(1);
                    Util.giveItem(player, item);
                }
            }
            case 'E' -> {
                int ruleIndex = index + page * slotsPerPage;
                if (ruleIndex < rules.size()) {
                    if (click == ClickType.CONTROL_DROP) {
                        handleRemoveRule(ruleIndex);
                    }
                }
            }
            case 'A' -> {
                // 添加规则功能暂时简化 - 提示用户
                ItemManagerMessages.properties__tool_rule_add_hint.t(player);
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
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handleRemoveRule(int ruleIndex) {
        ItemStack item = storedItem.getItemStack();
        List<Object> rules = new ArrayList<>(getRules());
        
        if (ruleIndex >= 0 && ruleIndex < rules.size()) {
            rules.remove(ruleIndex);
            
            Float speed = PaperUtil.getDefaultMiningSpeed(item);
            Integer damage = PaperUtil.getDamagePerBlock(item);
            
            if (PaperUtil.setToolProperties(item, 
                    speed != null ? speed : 1.0f, 
                    damage != null ? damage : 1, 
                    rules)) {
                module.getDatabase().saveItem(storedItem);
                refreshInventory();
                ItemManagerMessages.properties__tool_rule_removed.t(player);
            }
        }
    }

    public void refresh() {
        refreshInventory();
    }
}
