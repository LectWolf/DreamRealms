package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.ChatInputUtil;
import cn.mcloli.dreamrealms.utils.ItemNameUtil;
import cn.mcloli.dreamrealms.utils.PaperUtil;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * 食物组件编辑 GUI
 */
public class FoodEditGui extends AbstractInteractiveGui<FoodEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final ItemPropertiesGui parentGui;

    public FoodEditGui(Player player, FoodEditMenuConfig config, StoredItem storedItem, ItemPropertiesGui parentGui) {
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
        FoodComponent food = meta != null && meta.hasFood() ? meta.getFood() : null;

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'P' -> inventory.setItem(i, item.clone());
                case 'N' -> inventory.setItem(i, getNutritionIcon(food));
                case 'S' -> inventory.setItem(i, getSaturationIcon(food));
                case 'T' -> inventory.setItem(i, getEatTimeIcon());
                case 'C' -> inventory.setItem(i, getCanAlwaysEatIcon(food));
                case 'U' -> inventory.setItem(i, getUsingConvertsToIcon(food));
                case 'A' -> inventory.setItem(i, getEffectsIcon(food));
                case 'R' -> inventory.setItem(i, getRemoveIcon(food));
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private ItemStack getNutritionIcon(FoodComponent food) {
        ItemStack icon = new ItemStack(Material.COOKED_BEEF);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e营养值"));

        List<String> lore = new ArrayList<>();
        if (food != null) {
            lore.add(ColorHelper.parseColor("&7当前: &f" + food.getNutrition()));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &7未设置"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getSaturationIcon(FoodComponent food) {
        ItemStack icon = new ItemStack(Material.GOLDEN_APPLE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e饱和度"));

        List<String> lore = new ArrayList<>();
        if (food != null) {
            lore.add(ColorHelper.parseColor("&7当前: &f" + String.format("%.2f", food.getSaturation())));
        } else {
            lore.add(ColorHelper.parseColor("&7当前: &7未设置"));
        }
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getEatTimeIcon() {
        ItemStack icon = new ItemStack(Material.CLOCK);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e食用时间"));

        List<String> lore = new ArrayList<>();
        if (PaperUtil.isPaper()) {
            Float seconds = PaperUtil.getConsumeSeconds(storedItem.getItemStack());
            if (seconds != null) {
                lore.add(ColorHelper.parseColor("&7当前: &f" + String.format("%.2f", seconds) + " &7秒"));
            } else {
                lore.add(ColorHelper.parseColor("&7当前: &f1.6 &7秒 &8(默认)"));
            }
            lore.add("");
            lore.add(ColorHelper.parseColor("&e点击修改"));
        } else {
            lore.add(ColorHelper.parseColor("&c需要 Paper 服务端"));
        }
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getCanAlwaysEatIcon(FoodComponent food) {
        boolean canAlwaysEat = food != null && food.canAlwaysEat();
        ItemStack icon = new ItemStack(canAlwaysEat ? Material.CAKE : Material.BREAD);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e可以一直吃"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7状态: " + (canAlwaysEat ? "&a已启用" : "&c已禁用")));
        lore.add(ColorHelper.parseColor("&7即使饱食度满也能吃"));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击切换"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getUsingConvertsToIcon(FoodComponent food) {
        ItemStack icon = new ItemStack(Material.BOWL);
        String displayName = ColorHelper.parseColor("&e使用后转换");

        List<String> lore = new ArrayList<>();
        if (PaperUtil.isPaper()) {
            // 先尝试从 FoodComponent 获取 (1.21.1-)
            ItemStack convertsTo = PaperUtil.getUsingConvertsTo(food);
            module.debug("getUsingConvertsToIcon: from FoodComponent = " + convertsTo);
            
            // 如果为空，尝试从 USE_REMAINDER 组件获取 (1.21.2+)
            if (convertsTo == null || Util.isAir(convertsTo)) {
                convertsTo = PaperUtil.getUsingConvertsToViaConsumable(storedItem.getItemStack());
                module.debug("getUsingConvertsToIcon: from USE_REMAINDER = " + convertsTo);
            }
            
            if (convertsTo != null && !Util.isAir(convertsTo)) {
                String itemName = ItemNameUtil.getItemName(convertsTo);
                module.debug("getUsingConvertsToIcon: itemName = " + itemName);
                lore.add(ColorHelper.parseColor("&7当前: &f" + itemName));
                icon = convertsTo.clone();
            } else {
                lore.add(ColorHelper.parseColor("&7当前: &7无"));
            }
            lore.add("");
            lore.add(ColorHelper.parseColor("&e手持物品点击设置"));
            lore.add(ColorHelper.parseColor("&7右键清除"));
        } else {
            lore.add(ColorHelper.parseColor("&c需要 Paper 服务端"));
        }
        ItemStackUtil.setItemDisplayName(icon, displayName);
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getEffectsIcon(FoodComponent food) {
        ItemStack icon = new ItemStack(Material.POTION);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e食用效果"));

        List<String> lore = new ArrayList<>();
        if (PaperUtil.isPaper()) {
            // 先尝试从 FoodComponent 获取效果数量 (1.21.1-)
            int effectCount = PaperUtil.getFoodEffectCount(food);
            
            // 如果为0，尝试从 ConsumableComponent 获取 (1.21.2+)
            if (effectCount == 0) {
                effectCount = PaperUtil.getConsumableEffects(storedItem.getItemStack()).size();
            }
            
            if (effectCount > 0) {
                lore.add(ColorHelper.parseColor("&7当前效果数: &f" + effectCount));
            } else {
                lore.add(ColorHelper.parseColor("&7当前: &7无效果"));
            }
            lore.add("");
            lore.add(ColorHelper.parseColor("&e点击编辑效果"));
        } else {
            lore.add(ColorHelper.parseColor("&c需要 Paper 服务端"));
        }
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getRemoveIcon(FoodComponent food) {
        ItemStack icon = new ItemStack(food != null ? Material.BARRIER : Material.GRAY_DYE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor(food != null ? "&c移除食物组件" : "&7无食物组件"));

        List<String> lore = new ArrayList<>();
        if (food != null) {
            lore.add(ColorHelper.parseColor("&7点击移除食物组件"));
            lore.add(ColorHelper.parseColor("&7物品将不再可食用"));
        } else {
            lore.add(ColorHelper.parseColor("&7此物品没有食物组件"));
        }
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("FoodEditGui click: key=" + key + ", click=" + click);

        switch (key) {
            case 'P' -> handlePreviewClick(click);
            case 'N' -> handleNutritionEdit();
            case 'S' -> handleSaturationEdit();
            case 'T' -> handleEatTimeEdit();
            case 'C' -> handleCanAlwaysEatToggle();
            case 'U' -> handleUsingConvertsToEdit(click, event);
            case 'A' -> handleEffectsEdit();
            case 'R' -> handleRemoveFood();
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handlePreviewClick(ClickType click) {
        if (click == ClickType.LEFT) {
            ItemStack item = storedItem.getItemStack().clone();
            item.setAmount(1);
            Util.giveItem(player, item);
        }
    }

    private FoodComponent getOrCreateFood() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        if (meta.hasFood()) {
            return meta.getFood();
        } else {
            // 创建默认食物组件
            FoodComponent food = meta.getFood();
            food.setNutrition(4);
            food.setSaturation(2.4f);
            food.setCanAlwaysEat(false);
            return food;
        }
    }

    private void handleNutritionEdit() {
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__nutrition.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    ItemStack item = storedItem.getItemStack();
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        FoodComponent food = getOrCreateFood();
                        if (food != null) {
                            food.setNutrition(Math.max(0, value));
                            meta.setFood(food);
                            item.setItemMeta(meta);
                            module.getDatabase().saveItem(storedItem);
                            ItemManagerMessages.properties__food_nutrition_set.t(player);
                        }
                    }
                });
            }
            new FoodEditGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleSaturationEdit() {
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__saturation.str(), input -> {
            if (input != null) {
                Util.parseFloat(input).ifPresent(value -> {
                    ItemStack item = storedItem.getItemStack();
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        FoodComponent food = getOrCreateFood();
                        if (food != null) {
                            food.setSaturation(Math.max(0, value));
                            meta.setFood(food);
                            item.setItemMeta(meta);
                            module.getDatabase().saveItem(storedItem);
                            ItemManagerMessages.properties__food_saturation_set.t(player);
                        }
                    }
                });
            }
            new FoodEditGui(player, config, storedItem, parentGui).open();
        });
    }

    private void handleCanAlwaysEatToggle() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        FoodComponent food = getOrCreateFood();
        if (food != null) {
            food.setCanAlwaysEat(!food.canAlwaysEat());
            meta.setFood(food);
            item.setItemMeta(meta);
            module.getDatabase().saveItem(storedItem);
            refreshInventory();
            ItemManagerMessages.properties__food_can_always_eat_toggled.t(player);
        }
    }

    private void handleRemoveFood() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasFood()) return;

        meta.setFood(null);
        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);
        refreshInventory();
        ItemManagerMessages.properties__food_removed.t(player);
    }
    
    private void handleEatTimeEdit() {
        if (!PaperUtil.isPaper()) {
            ItemManagerMessages.properties__need_paper.t(player);
            return;
        }
        
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__eat_time.str(), input -> {
            if (input != null) {
                Util.parseFloat(input).ifPresent(value -> {
                    ItemStack item = storedItem.getItemStack();
                    if (PaperUtil.setConsumeSeconds(item, Math.max(0, value))) {
                        module.getDatabase().saveItem(storedItem);
                        ItemManagerMessages.properties__food_eat_time_set.t(player);
                    }
                });
            }
            new FoodEditGui(player, config, storedItem, parentGui).open();
        });
    }
    
    private void handleUsingConvertsToEdit(ClickType click, InventoryClickEvent event) {
        module.debug("handleUsingConvertsToEdit: click=" + click);
        if (!PaperUtil.isPaper()) {
            ItemManagerMessages.properties__need_paper.t(player);
            return;
        }
        
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            module.debug("handleUsingConvertsToEdit: meta is null");
            return;
        }
        
        if (click == ClickType.RIGHT) {
            // 右键清除 - 尝试两种方式
            FoodComponent food = getOrCreateFood();
            boolean cleared = false;
            if (food != null) {
                cleared = PaperUtil.setUsingConvertsTo(food, null);
                if (cleared) {
                    meta.setFood(food);
                    item.setItemMeta(meta);
                }
            }
            if (!cleared) {
                // 尝试通过 ConsumableComponent 清除
                cleared = PaperUtil.setUsingConvertsToViaConsumable(item, null);
            }
            if (cleared) {
                module.getDatabase().saveItem(storedItem);
                refreshInventory();
                ItemManagerMessages.properties__food_converts_to_cleared.t(player);
            }
            return;
        }
        
        // 左键 - 检查玩家手持物品
        ItemStack cursor = event.getCursor();
        module.debug("handleUsingConvertsToEdit: cursor=" + cursor + ", isAir=" + Util.isAir(cursor));
        if (Util.notAir(cursor)) {
            // 手持物品 - 设置为转换物品
            ItemStack convertsTo = cursor.clone();
            convertsTo.setAmount(1);
            
            // 先尝试 FoodComponent 方式
            FoodComponent food = getOrCreateFood();
            boolean success = false;
            module.debug("handleUsingConvertsToEdit: food=" + food);
            if (food != null) {
                success = PaperUtil.setUsingConvertsTo(food, convertsTo);
                if (success) {
                    meta.setFood(food);
                    item.setItemMeta(meta);
                }
            }
            
            // 如果失败，尝试 ConsumableComponent 方式 (1.21.2+)
            if (!success) {
                module.debug("handleUsingConvertsToEdit: trying ConsumableComponent way");
                success = PaperUtil.setUsingConvertsToViaConsumable(item, convertsTo);
            }
            
            module.debug("handleUsingConvertsToEdit: final result=" + success);
            if (success) {
                module.getDatabase().saveItem(storedItem);
                refreshInventory();
                ItemManagerMessages.properties__food_converts_to_set.t(player, 
                    Pair.of("{item}", ItemNameUtil.getItemName(convertsTo)));
            }
        } else {
            ItemManagerMessages.properties__food_converts_to_hint.t(player);
        }
    }
    
    private void handleEffectsEdit() {
        if (!PaperUtil.isPaper()) {
            ItemManagerMessages.properties__need_paper.t(player);
            return;
        }
        // 打开效果编辑菜单
        new FoodEffectEditGui(player, module.getFoodEffectEditMenuConfig(), storedItem, this).open();
    }

    public void refresh() {
        refreshInventory();
    }
}
