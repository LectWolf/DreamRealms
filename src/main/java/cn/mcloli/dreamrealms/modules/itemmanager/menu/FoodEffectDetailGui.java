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
 * 食物效果详情编辑 GUI
 */
public class FoodEffectDetailGui extends AbstractInteractiveGui<FoodEffectDetailMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final FoodEffectEditGui parentGui;
    private final int effectIndex;
    private Object foodEffect;

    public FoodEffectDetailGui(Player player, FoodEffectDetailMenuConfig config, StoredItem storedItem, 
                               FoodEffectEditGui parentGui, int effectIndex, Object foodEffect) {
        super(player, config);
        this.module = ItemManagerModule.inst();
        this.storedItem = storedItem;
        this.parentGui = parentGui;
        this.effectIndex = effectIndex;
        this.foodEffect = foodEffect;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = config.createInventory(this, player);
        refreshInventory();
        return inventory;
    }

    private void refreshInventory() {
        // 先尝试从 FoodEffect 获取 (1.21.1-)
        PotionEffect effect = PaperUtil.getEffectFromFoodEffect(foodEffect);
        float probability = PaperUtil.getProbabilityFromFoodEffect(foodEffect);
        
        // 如果失败，尝试从 ConsumeEffect 获取 (1.21.2+)
        if (effect == null) {
            List<PotionEffect> potionEffects = PaperUtil.getPotionEffectsFromConsumeEffect(foodEffect);
            if (!potionEffects.isEmpty()) {
                effect = potionEffects.get(0);
            }
            probability = PaperUtil.getProbabilityFromConsumeEffect(foodEffect);
        }
        
        if (effect == null) {
            module.debug("FoodEffectDetailGui: effect is null, foodEffect=" + foodEffect);
            return;
        }
        
        final PotionEffect finalEffect = effect;
        final float finalProbability = probability;
        
        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'E' -> inventory.setItem(i, getEffectIcon(finalEffect));
                case 'L' -> inventory.setItem(i, getLevelIcon(finalEffect));
                case 'D' -> inventory.setItem(i, getDurationIcon(finalEffect));
                case 'P' -> inventory.setItem(i, getProbabilityIcon(finalProbability));
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private ItemStack getEffectIcon(PotionEffect effect) {
        ItemStack icon = new ItemStack(Material.POTION);
        String effectName = getEffectName(effect.getType());
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&b" + effectName));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7效果类型"));
        lore.add(ColorHelper.parseColor("&7ID: &f" + effect.getType().getKey().getKey()));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getLevelIcon(PotionEffect effect) {
        ItemStack icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
        int level = effect.getAmplifier() + 1;
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e效果等级"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7当前: &f" + level));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getDurationIcon(PotionEffect effect) {
        ItemStack icon = new ItemStack(Material.CLOCK);
        int seconds = effect.getDuration() / 20;
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e持续时间"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7当前: &f" + seconds + " &7秒"));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getProbabilityIcon(float probability) {
        ItemStack icon = new ItemStack(Material.RABBIT_FOOT);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&e触发概率"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7当前: &f" + String.format("%.0f", probability * 100) + "%"));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击修改"));
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
        module.debug("FoodEffectDetailGui click: key=" + key + ", click=" + click);

        switch (key) {
            case 'L' -> handleEditLevel();
            case 'D' -> handleEditDuration();
            case 'P' -> handleEditProbability();
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handleEditLevel() {
        PotionEffect currentEffect = getCurrentEffect();
        if (currentEffect == null) return;
        
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__effect_amplifier.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    int amplifier = Math.max(0, Math.min(254, value - 1)); // 等级1对应amplifier 0
                    updateEffect(amplifier, currentEffect.getDuration(), getCurrentProbability());
                    ItemManagerMessages.properties__food_effect_modified.t(player);
                });
            }
            reopenGui();
        });
    }

    private void handleEditDuration() {
        PotionEffect currentEffect = getCurrentEffect();
        if (currentEffect == null) return;
        
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__effect_duration.str(), input -> {
            if (input != null) {
                Util.parseInt(input).ifPresent(value -> {
                    int ticks = Math.max(1, value) * 20; // 秒转tick
                    updateEffect(currentEffect.getAmplifier(), ticks, getCurrentProbability());
                    ItemManagerMessages.properties__food_effect_modified.t(player);
                });
            }
            reopenGui();
        });
    }

    private void handleEditProbability() {
        PotionEffect currentEffect = getCurrentEffect();
        if (currentEffect == null) return;
        
        player.closeInventory();
        ChatInputUtil.requestInput(player, ItemManagerMessages.input__effect_probability.str(), input -> {
            if (input != null) {
                Util.parseFloat(input).ifPresent(value -> {
                    float probability = Math.max(0, Math.min(100, value)) / 100f;
                    updateEffect(currentEffect.getAmplifier(), currentEffect.getDuration(), probability);
                    ItemManagerMessages.properties__food_effect_modified.t(player);
                });
            }
            reopenGui();
        });
    }
    
    private PotionEffect getCurrentEffect() {
        // 先尝试从 FoodEffect 获取 (1.21.1-)
        PotionEffect effect = PaperUtil.getEffectFromFoodEffect(foodEffect);
        
        // 如果失败，尝试从 ConsumeEffect 获取 (1.21.2+)
        if (effect == null) {
            List<PotionEffect> potionEffects = PaperUtil.getPotionEffectsFromConsumeEffect(foodEffect);
            if (!potionEffects.isEmpty()) {
                effect = potionEffects.get(0);
            }
        }
        return effect;
    }
    
    private float getCurrentProbability() {
        float probability = PaperUtil.getProbabilityFromFoodEffect(foodEffect);
        if (probability == 0f) {
            probability = PaperUtil.getProbabilityFromConsumeEffect(foodEffect);
        }
        return probability;
    }
    
    private boolean isUsingConsumable() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        FoodComponent food = meta != null && meta.hasFood() ? meta.getFood() : null;
        return PaperUtil.getFoodEffects(food).isEmpty();
    }
    
    private void reopenGui() {
        // 重新获取最新的 foodEffect
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        
        // 先尝试从 FoodComponent 获取 (1.21.1-)
        if (meta != null && meta.hasFood()) {
            FoodComponent food = meta.getFood();
            List<Object> effects = PaperUtil.getFoodEffects(food);
            if (effectIndex >= 0 && effectIndex < effects.size()) {
                this.foodEffect = effects.get(effectIndex);
                new FoodEffectDetailGui(player, config, storedItem, parentGui, effectIndex, foodEffect).open();
                return;
            }
        }
        
        // 尝试从 ConsumableComponent 获取 (1.21.2+)
        List<Object> effects = PaperUtil.getConsumableEffects(item);
        if (effectIndex >= 0 && effectIndex < effects.size()) {
            this.foodEffect = effects.get(effectIndex);
        }
        new FoodEffectDetailGui(player, config, storedItem, parentGui, effectIndex, foodEffect).open();
    }

    private void updateEffect(int amplifier, int duration, float probability) {
        ItemStack item = storedItem.getItemStack();
        PotionEffect currentEffect = getCurrentEffect();
        if (currentEffect == null) return;
        
        PotionEffect newEffect = new PotionEffect(currentEffect.getType(), duration, amplifier);
        
        if (isUsingConsumable()) {
            // 1.21.2+ 使用 ConsumableComponent
            List<Object> effects = PaperUtil.getConsumableEffects(item);
            if (effectIndex >= 0 && effectIndex < effects.size()) {
                effects.remove(effectIndex);
                // 需要重新构建整个效果列表
                if (PaperUtil.setConsumableEffects(item, effects)) {
                    // 添加新效果
                    if (PaperUtil.addFoodEffectViaConsumable(item, newEffect, probability)) {
                        module.getDatabase().saveItem(storedItem);
                    }
                }
            }
        } else {
            // 1.21.1- 使用 FoodComponent
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasFood()) return;

            FoodComponent food = meta.getFood();
            List<Object> effects = PaperUtil.getFoodEffects(food);
            
            if (effectIndex >= 0 && effectIndex < effects.size()) {
                // 移除旧效果，添加新效果
                effects.remove(effectIndex);
                Object newFoodEffect = PaperUtil.addFoodEffect(food, newEffect, probability);
                if (newFoodEffect != null) {
                    effects.add(effectIndex, newFoodEffect);
                    PaperUtil.setFoodEffects(food, effects);
                    
                    meta.setFood(food);
                    item.setItemMeta(meta);
                    module.getDatabase().saveItem(storedItem);
                    
                    // 更新当前效果引用
                    this.foodEffect = newFoodEffect;
                }
            }
        }
    }

    public void refresh() {
        refreshInventory();
    }
}
