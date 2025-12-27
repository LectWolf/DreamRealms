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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 食物效果编辑 GUI
 */
public class FoodEffectEditGui extends AbstractInteractiveGui<FoodEffectEditMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final FoodEditGui parentGui;
    private int page = 0;
    private int slotsPerPage = 0;

    public FoodEffectEditGui(Player player, FoodEffectEditMenuConfig config, StoredItem storedItem, FoodEditGui parentGui) {
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

    private List<Object> getEffects() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        FoodComponent food = meta != null && meta.hasFood() ? meta.getFood() : null;
        
        // 先尝试从 FoodComponent 获取效果 (1.21.1-)
        List<Object> effects = PaperUtil.getFoodEffects(food);
        
        // 如果为空，尝试从 ConsumableComponent 获取 (1.21.2+)
        if (effects.isEmpty()) {
            effects = PaperUtil.getConsumableEffects(item);
        }
        
        return effects;
    }

    private boolean hasNextPage() {
        return (page + 1) * slotsPerPage < getEffects().size();
    }

    private void refreshInventory() {
        ItemStack item = storedItem.getItemStack();
        List<Object> effects = getEffects();

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            String keyStr = String.valueOf(key);
            if (keyStr.equals(" ") || keyStr.equals("　")) {
                inventory.setItem(i, null);
                continue;
            }

            switch (key) {
                case 'P' -> inventory.setItem(i, item.clone());
                case 'E' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    inventory.setItem(i, getEffectItem(effects, index));
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

    private ItemStack getEffectItem(List<Object> effects, int index) {
        if (index >= effects.size()) {
            return null;
        }

        Object effectObj = effects.get(index);
        
        // 尝试从 FoodEffect 获取 (1.21.1-)
        PotionEffect effect = PaperUtil.getEffectFromFoodEffect(effectObj);
        float probability = PaperUtil.getProbabilityFromFoodEffect(effectObj);
        
        // 如果失败，尝试从 ConsumeEffect 获取 (1.21.2+)
        if (effect == null) {
            List<PotionEffect> potionEffects = PaperUtil.getPotionEffectsFromConsumeEffect(effectObj);
            if (!potionEffects.isEmpty()) {
                effect = potionEffects.get(0);
            }
            probability = PaperUtil.getProbabilityFromConsumeEffect(effectObj);
        }
        
        if (effect == null) {
            return new ItemStack(Material.BARRIER);
        }
        
        ItemStack icon = new ItemStack(Material.POTION);
        
        String effectName = getEffectName(effect.getType());
        int level = effect.getAmplifier() + 1;
        int duration = effect.getDuration() / 20;
        
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&b" + effectName + " " + level));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7等级: &f" + level));
        lore.add(ColorHelper.parseColor("&7持续时间: &f" + duration + " &7秒"));
        lore.add(ColorHelper.parseColor("&7触发概率: &f" + String.format("%.0f", probability * 100) + "%"));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e左键编辑"));
        lore.add(ColorHelper.parseColor("&cCtrl+Q删除"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private String getEffectName(PotionEffectType type) {
        return switch (type.getKey().getKey()) {
            case "speed" -> "速度";
            case "slowness" -> "缓慢";
            case "haste" -> "急迫";
            case "mining_fatigue" -> "挖掘疲劳";
            case "strength" -> "力量";
            case "instant_health" -> "瞬间治疗";
            case "instant_damage" -> "瞬间伤害";
            case "jump_boost" -> "跳跃提升";
            case "nausea" -> "反胃";
            case "regeneration" -> "生命恢复";
            case "resistance" -> "抗性提升";
            case "fire_resistance" -> "防火";
            case "water_breathing" -> "水下呼吸";
            case "invisibility" -> "隐身";
            case "blindness" -> "失明";
            case "night_vision" -> "夜视";
            case "hunger" -> "饥饿";
            case "weakness" -> "虚弱";
            case "poison" -> "中毒";
            case "wither" -> "凋零";
            case "health_boost" -> "生命提升";
            case "absorption" -> "伤害吸收";
            case "saturation" -> "饱和";
            case "glowing" -> "发光";
            case "levitation" -> "漂浮";
            case "luck" -> "幸运";
            case "unluck" -> "霉运";
            case "slow_falling" -> "缓降";
            case "conduit_power" -> "潮涌能量";
            case "dolphins_grace" -> "海豚的恩惠";
            case "darkness" -> "黑暗";
            case "wind_charged" -> "风能充沛";
            case "weaving" -> "编织";
            case "oozing" -> "渗浆";
            case "infested" -> "虫噬";
            default -> type.getKey().getKey();
        };
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        List<Object> effects = getEffects();
        
        // 判断是否使用 ConsumableComponent
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        FoodComponent food = meta != null && meta.hasFood() ? meta.getFood() : null;
        boolean useConsumable = PaperUtil.getFoodEffects(food).isEmpty() && !effects.isEmpty();

        switch (key) {
            case 'P' -> {
                if (click == ClickType.LEFT) {
                    ItemStack clone = storedItem.getItemStack().clone();
                    clone.setAmount(1);
                    Util.giveItem(player, clone);
                }
            }
            case 'E' -> {
                int effectIndex = index + page * slotsPerPage;
                if (effectIndex < effects.size()) {
                    if (click == ClickType.CONTROL_DROP) {
                        // Ctrl+Q 删除
                        handleRemoveEffect(effectIndex, useConsumable);
                    } else if (click == ClickType.LEFT) {
                        // 左键编辑
                        handleEditEffect(effectIndex, effects.get(effectIndex));
                    }
                }
            }
            case 'A' -> {
                // 打开效果选择菜单
                new FoodEffectSelectGui(player, module.getFoodEffectSelectMenuConfig(), storedItem, this).open();
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

    private void handleRemoveEffect(int effectIndex, boolean useConsumable) {
        ItemStack item = storedItem.getItemStack();
        
        if (useConsumable) {
            // 1.21.2+ 使用 ConsumableComponent
            if (PaperUtil.removeConsumableEffect(item, effectIndex)) {
                module.getDatabase().saveItem(storedItem);
                refreshInventory();
                ItemManagerMessages.properties__food_effect_removed.t(player);
            }
        } else {
            // 1.21.1- 使用 FoodComponent
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasFood()) return;

            FoodComponent food = meta.getFood();
            List<Object> effects = PaperUtil.getFoodEffects(food);
            if (effectIndex >= 0 && effectIndex < effects.size()) {
                effects.remove(effectIndex);
                PaperUtil.setFoodEffects(food, effects);
                meta.setFood(food);
                item.setItemMeta(meta);
                module.getDatabase().saveItem(storedItem);
                refreshInventory();
                ItemManagerMessages.properties__food_effect_removed.t(player);
            }
        }
    }

    private void handleEditEffect(int effectIndex, Object foodEffect) {
        // 打开效果详情编辑
        new FoodEffectDetailGui(player, module.getFoodEffectDetailMenuConfig(), storedItem, this, effectIndex, foodEffect).open();
    }

    public void refresh() {
        refreshInventory();
    }
}
