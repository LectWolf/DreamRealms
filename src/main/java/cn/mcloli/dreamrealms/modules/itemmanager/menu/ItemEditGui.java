package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.ChatInputUtil;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

/**
 * 物品编辑 GUI
 */
public class ItemEditGui extends AbstractInteractiveGui<ItemEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemListGui parentGui;

    public ItemEditGui(Player player, ItemEditMenuConfig config, StoredItem storedItem, ItemListGui parentGui) {
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
        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            String keyStr = String.valueOf(key);
            if (keyStr.equals(" ") || keyStr.equals("　")) {
                inventory.setItem(i, null);
                continue;
            }

            switch (key) {
                case 'P' -> {
                    // 预览物品
                    inventory.setItem(i, storedItem.getItemStack().clone());
                }
                case 'O' -> {
                    // 查看命令按钮
                    AbstractMenuConfig.Icon icon = config.getCommandIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                case 'S' -> {
                    // 序列化按钮 - 仅非序列化物品显示
                    if (!storedItem.isSerialized()) {
                        AbstractMenuConfig.Icon icon = config.getSerializeIcon();
                        if (icon != null) {
                            inventory.setItem(i, icon.generateIcon(player));
                        }
                    } else {
                        inventory.setItem(i, null);
                    }
                }
                case 'N', 'L', 'E', 'A', 'F', 'D' -> {
                    // 编辑按钮 - 仅序列化物品显示
                    if (storedItem.isSerialized()) {
                        applyEditIcon(i, key);
                    } else {
                        inventory.setItem(i, null);
                    }
                }
                case 'I' -> {
                    // 标识名按钮
                    AbstractMenuConfig.Icon icon = config.getIdentifierIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                case 'G' -> {
                    // 分类按钮
                    AbstractMenuConfig.Icon icon = config.getCategoryIcon();
                    if (icon != null) {
                        inventory.setItem(i, icon.generateIcon(player));
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private void applyEditIcon(int slot, char key) {
        AbstractMenuConfig.Icon icon = switch (key) {
            case 'N' -> config.getNameIcon();
            case 'L' -> config.getLoreIcon();
            case 'E' -> config.getEnchantIcon();
            case 'A' -> config.getAttributeIcon();
            case 'F' -> config.getFlagIcon();
            case 'D' -> config.getDurabilityIcon();
            default -> null;
        };
        if (icon != null) {
            inventory.setItem(slot, icon.generateIcon(player));
        }
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("ItemEditGui click: key=" + key + ", click=" + click);

        switch (key) {
            case 'P' -> handlePreviewClick(click);
            case 'O' -> handleCommandClick();
            case 'S' -> handleSerialize();
            case 'N' -> handleEditName();
            case 'L' -> handleEditLore();
            case 'E' -> handleEditEnchant();
            case 'A' -> handleEditAttribute();
            case 'F' -> handleEditFlag();
            case 'D' -> handleEditDurability();
            case 'I' -> handleEditIdentifier();
            case 'G' -> handleEditCategory();
            case 'B' -> {
                // 返回
                parentGui.refresh();
                parentGui.open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handlePreviewClick(ClickType click) {
        if (click == ClickType.LEFT) {
            // 左键 - 获取物品
            ItemStack item = storedItem.getItemStack().clone();
            item.setAmount(1);
            Util.giveItem(player, item);
        }
    }

    private void handleCommandClick() {
        // 关闭菜单后显示获取命令
        player.closeInventory();
        
        String identifier = storedItem.getIdentifier() != null 
                ? storedItem.getIdentifier() 
                : storedItem.getGuid().toString();
        String command = "/dr im give " + player.getName() + " " + identifier + " 1";

        // 发送带按钮的消息
        ItemManagerMessages.chat__cmd_buttons.tm(player, Pair.of("{command}", command));
    }

    private void handleSerialize() {
        if (storedItem.isSerialized()) return;

        // 序列化物品
        storedItem.setSerialized(true);
        module.getDatabase().saveItem(storedItem);
        refreshInventory();
    }

    private void handleEditName() {
        if (!storedItem.isSerialized()) return;
        
        player.closeInventory();
        
        // 显示当前名称供复制
        ItemStack item = storedItem.getItemStack();
        String currentName = ItemStackUtil.getItemDisplayName(item);
        if (currentName != null && !currentName.isEmpty()) {
            ItemManagerMessages.edit__current_name.tm(player, Pair.of("{name}", currentName));
        }
        
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__item_name.str(), input -> {
            if (input != null) {
                ItemStack itemStack = storedItem.getItemStack();
                ItemStackUtil.setItemDisplayName(itemStack, ColorHelper.parseColor(input));
                module.getDatabase().saveItem(storedItem);
                ItemManagerMessages.edit__name_success.t(player);
            }
            // 重新打开菜单
            new ItemEditGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleEditLore() {
        if (!storedItem.isSerialized()) return;
        // 打开 Lore 编辑菜单
        new LoreEditGui(player, module.getLoreEditMenuConfig(), storedItem, this).open();
    }

    private void handleEditEnchant() {
        if (!storedItem.isSerialized()) return;
        ItemManagerMessages.edit__enchant_wip.t(player);
    }

    private void handleEditAttribute() {
        if (!storedItem.isSerialized()) return;
        ItemManagerMessages.edit__attribute_wip.t(player);
    }

    private void handleEditFlag() {
        if (!storedItem.isSerialized()) return;
        ItemManagerMessages.edit__flag_wip.t(player);
    }

    private void handleEditDurability() {
        if (!storedItem.isSerialized()) return;
        ItemManagerMessages.edit__durability_wip.t(player);
    }

    private void handleEditIdentifier() {
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__identifier.str(), input -> {
            if (input != null) {
                storedItem.setIdentifier(input);
                module.getDatabase().saveItem(storedItem);
            }
            // 重新打开菜单
            new ItemEditGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleEditCategory() {
        // 打开分类选择菜单
        new CategorySelectGui(player, module.getCategoryMenuConfig(), storedItem, this).open();
    }

    /**
     * 刷新界面 (供分类选择返回时调用)
     */
    public void refresh() {
        refreshInventory();
    }
}
