package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
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
    private static final int EFFECTS_PER_PAGE = 7;

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
        refreshInventory();
        return inventory;
    }

    private void refreshInventory() {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        FoodComponent food = meta != null && meta.hasFood() ? meta.getFood() : null;
        List<Object> effects = PaperUtil.getFoodEffects(food);

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'P' -> inventory.setItem(i, item.clone());
                case 'A' -> inventory.setItem(i, getAddEffectIcon());
                case '<' -> inventory.setItem(i, getPrevPageIcon());
                case '>' -> inventory.setItem(i, getNextPageIcon(effects.size()));
                default -> {
                    // 数字键 1-7 显示效果列表
                    if (key >= '1' && key <= '7') {
                        int effectIndex = page * EFFECTS_PER_PAGE + (key - '1');
                        if (effectIndex < effects.size()) {
                            inventory.setItem(i, getEffectIcon(effects.get(effectIndex), effectIndex));
                        } else {
                            inventory.setItem(i, null);
                        }
                    } else {
                        config.applyIcon(this, inventory, player, i);
                    }
                }
            }
        }
    }

    private ItemStack getAddEffectIcon() {
        ItemStack icon = new ItemStack(Material.BREWING_STAND);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&a添加效果"));
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7点击添加新的食用效果"));
        ItemStackUtil.setItemLore(icon, lore);
        return icon;
    }

    private ItemStack getPrevPageIcon() {
        ItemStack icon = new ItemStack(page > 0 ? Material.ARROW : Material.GRAY_DYE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor(page > 0 ? "&e◀ 上一页" : "&7已是第一页"));
        return icon;
    }

    private ItemStack getNextPageIcon(int totalEffects) {
        int maxPage = (totalEffects - 1) / EFFECTS_PER_PAGE;
        boolean hasNext = page < maxPage;
        ItemStack icon = new ItemStack(hasNext ? Material.ARROW : Material.GRAY_DYE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor(hasNext ? "&e下一页 ▶" : "&7已是最后一页"));
        return icon;
    }

    private ItemStack getEffectIcon(Object foodEffect, int index) {
        PotionEffect effect = PaperUtil.getEffectFromFoodEffect(foodEffect);
        if (effect == null) return new ItemStack(Material.BARRIER);
        
        ItemStack icon = new ItemStack(Material.POTION);
        
        String effectName = getEffectName(effect.getType());
        int level = effect.getAmplifier() + 1;
        int duration = effect.getDuration() / 20;
        float probability = PaperUtil.getProbabilityFromFoodEffect(foodEffect) * 100;
        
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&b" + effectName + " " + level));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7持续时间: &f" + duration + " &7秒"));
        lore.add(ColorHelper.parseColor("&7触发概率: &f" + String.format("%.0f", probability) + "%"));
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
        module.debug("FoodEffectEditGui click: key=" + key + ", click=" + click);

        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        FoodComponent food = meta != null && meta.hasFood() ? meta.getFood() : null;
        List<Object> effects = PaperUtil.getFoodEffects(food);

        switch (key) {
            case 'P' -> handlePreviewClick(click);
            case 'A' -> handleAddEffect();
            case '<' -> {
                if (page > 0) {
                    page--;
                    refreshInventory();
                }
            }
            case '>' -> {
                int maxPage = (effects.size() - 1) / EFFECTS_PER_PAGE;
                if (page < maxPage) {
                    page++;
                    refreshInventory();
                }
            }
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            default -> {
                // 数字键 1-7 处理效果点击
                if (key >= '1' && key <= '7') {
                    int effectIndex = page * EFFECTS_PER_PAGE + (key - '1');
                    if (effectIndex < effects.size()) {
                        if (click == ClickType.CONTROL_DROP) {
                            // Ctrl+Q 删除
                            handleRemoveEffect(effectIndex);
                        } else if (click == ClickType.LEFT) {
                            // 左键编辑
                            handleEditEffect(effectIndex, effects.get(effectIndex));
                        }
                    }
                } else {
                    config.handleOtherIconClick(player, click, key);
                }
            }
        }
    }

    private void handlePreviewClick(ClickType click) {
        if (click == ClickType.LEFT) {
            ItemStack item = storedItem.getItemStack().clone();
            item.setAmount(1);
            Util.giveItem(player, item);
        }
    }

    private void handleAddEffect() {
        // 打开效果选择菜单
        new FoodEffectSelectGui(player, module.getFoodEffectSelectMenuConfig(), storedItem, this).open();
    }

    private void handleRemoveEffect(int effectIndex) {
        ItemStack item = storedItem.getItemStack();
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

    private void handleEditEffect(int effectIndex, Object foodEffect) {
        // 打开效果详情编辑
        new FoodEffectDetailGui(player, module.getFoodEffectDetailMenuConfig(), storedItem, this, effectIndex, foodEffect).open();
    }

    public void refresh() {
        refreshInventory();
    }
}
