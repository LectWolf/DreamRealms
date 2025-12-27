package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.ChatInputUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Lore 编辑 GUI
 */
public class LoreEditGui extends AbstractInteractiveGui<LoreEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemEditGui parentGui;
    private List<String> loreList;
    private int page = 0;
    private int slotsPerPage = 0;

    public LoreEditGui(Player player, LoreEditMenuConfig config, StoredItem storedItem, ItemEditGui parentGui) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
        // 获取当前 Lore
        List<String> currentLore = ItemStackUtil.getItemLore(storedItem.getItemStack());
        this.loreList = currentLore != null ? new ArrayList<>(currentLore) : new ArrayList<>();
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = config.createInventory(this, player);
        slotsPerPage = countSlots('L');
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
                case 'L' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    inventory.setItem(i, getLoreLineItem(index));
                }
                case 'A' -> {
                    // 添加按钮
                    AbstractMenuConfig.Icon icon = config.getAddIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                case 'E' -> {
                    // 添加空行按钮
                    AbstractMenuConfig.Icon icon = config.getEmptyLineIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                case '<' -> {
                    if (page > 0) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        // 显示空图标
                        inventory.setItem(i, config.getEmptyPrevIcon(player));
                    }
                }
                case '>' -> {
                    if (hasNextPage()) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        // 显示空图标
                        inventory.setItem(i, config.getEmptyNextIcon(player));
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private boolean hasNextPage() {
        return (page + 1) * slotsPerPage < loreList.size();
    }

    private ItemStack getLoreLineItem(int index) {
        if (index >= loreList.size()) {
            return null;
        }

        String line = loreList.get(index);
        ItemStack item = new ItemStack(Material.PAPER);
        ItemStackUtil.setItemDisplayName(item, ColorHelper.parseColor("&f第 " + (index + 1) + " 行"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7内容: &r" + line));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e左键编辑"));
        lore.add(ColorHelper.parseColor("&c右键删除"));
        lore.add(ColorHelper.parseColor("&7Shift+左键上移"));
        lore.add(ColorHelper.parseColor("&7Shift+右键下移"));
        ItemStackUtil.setItemLore(item, lore);
        
        return item;
    }


    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("LoreEditGui click: key=" + key + ", index=" + index + ", click=" + click);

        switch (key) {
            case 'L' -> handleLoreLineClick(click, index + page * slotsPerPage);
            case 'A' -> handleAddLine();
            case 'E' -> handleAddEmptyLine();
            case 'B' -> {
                // 返回 - 保存并返回
                saveLore();
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

    private void handleLoreLineClick(ClickType click, int index) {
        if (index >= loreList.size()) return;

        if (click == ClickType.LEFT) {
            // 左键 - 编辑
            handleEditLine(index);
        } else if (click == ClickType.RIGHT) {
            // 右键 - 删除
            loreList.remove(index);
            saveLore();
            refreshInventory();
            ItemManagerMessages.lore__deleted.t(player);
        } else if (click == ClickType.SHIFT_LEFT && index > 0) {
            // Shift+左键 - 上移
            String line = loreList.remove(index);
            loreList.add(index - 1, line);
            saveLore();
            refreshInventory();
        } else if (click == ClickType.SHIFT_RIGHT && index < loreList.size() - 1) {
            // Shift+右键 - 下移
            String line = loreList.remove(index);
            loreList.add(index + 1, line);
            saveLore();
            refreshInventory();
        }
    }

    private void handleAddLine() {
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__lore_add.str(), input -> {
            if (input != null) {
                loreList.add(parseColorInput(input));
                saveLore();
                ItemManagerMessages.lore__added.t(player);
            }
            // 重新打开菜单
            new LoreEditGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleAddEmptyLine() {
        loreList.add("");
        saveLore();
        refreshInventory();
        ItemManagerMessages.lore__added.t(player);
    }

    private void handleEditLine(int index) {
        player.closeInventory();
        
        // 显示当前内容供复制
        String currentLine = loreList.get(index);
        ItemManagerMessages.lore__current.tm(player, 
                Pair.of("{index}", String.valueOf(index + 1)),
                Pair.of("{content}", currentLine));
        
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__lore_edit.str(), input -> {
            if (input != null) {
                loreList.set(index, parseColorInput(input));
                saveLore();
                ItemManagerMessages.lore__edited.t(player);
            }
            // 重新打开菜单
            new LoreEditGui(player, config, storedItem, parentGui).open();
        });
    }

    /**
     * 解析颜色输入 (支持 MiniMessage 和传统颜色代码)
     */
    private String parseColorInput(String input) {
        // 如果包含 MiniMessage 标签，使用 MiniMessage 解析后转为传统格式
        if (input.contains("<") && input.contains(">")) {
            return LegacyComponentSerializer.legacySection()
                    .serialize(AdventureUtil.miniMessage(input));
        }
        // 否则使用传统颜色代码解析
        return ColorHelper.parseColor(input);
    }

    private void saveLore() {
        ItemStack item = storedItem.getItemStack();
        ItemStackUtil.setItemLore(item, loreList);
        module.getDatabase().saveItem(storedItem);
    }
}
