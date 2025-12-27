package cn.mcloli.dreamrealms.modules.itemmanager.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.utils.PaperUtil;
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
 * 食物效果选择 GUI - 选择要添加的效果类型
 */
public class FoodEffectSelectGui extends AbstractInteractiveGui<FoodEffectSelectMenuConfig> {

    private final ItemManagerModule module;
    private final StoredItem storedItem;
    private final FoodEffectEditGui parentGui;
    private int page = 0;
    private static final int EFFECTS_PER_PAGE = 28; // 4行7列
    
    // 所有可用的药水效果类型
    private static final PotionEffectType[] EFFECT_TYPES = {
        PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE,
        PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.INSTANT_HEALTH,
        PotionEffectType.INSTANT_DAMAGE, PotionEffectType.JUMP_BOOST, PotionEffectType.NAUSEA,
        PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
        PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY, PotionEffectType.BLINDNESS,
        PotionEffectType.NIGHT_VISION, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS,
        PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.HEALTH_BOOST,
        PotionEffectType.ABSORPTION, PotionEffectType.SATURATION, PotionEffectType.GLOWING,
        PotionEffectType.LEVITATION, PotionEffectType.LUCK, PotionEffectType.UNLUCK,
        PotionEffectType.SLOW_FALLING, PotionEffectType.CONDUIT_POWER, PotionEffectType.DOLPHINS_GRACE,
        PotionEffectType.DARKNESS, PotionEffectType.WIND_CHARGED, PotionEffectType.WEAVING,
        PotionEffectType.OOZING, PotionEffectType.INFESTED
    };

    public FoodEffectSelectGui(Player player, FoodEffectSelectMenuConfig config, StoredItem storedItem, FoodEffectEditGui parentGui) {
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
        // 效果槽位: 第2-5行的中间7格 (索引 10-16, 19-25, 28-34, 37-43)
        int[] effectSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case '<' -> inventory.setItem(i, getPrevPageIcon());
                case '>' -> inventory.setItem(i, getNextPageIcon());
                case 'E' -> {
                    // 效果槽位
                    int slotIndex = -1;
                    for (int j = 0; j < effectSlots.length; j++) {
                        if (effectSlots[j] == i) {
                            slotIndex = j;
                            break;
                        }
                    }
                    if (slotIndex >= 0) {
                        int effectIndex = page * EFFECTS_PER_PAGE + slotIndex;
                        if (effectIndex < EFFECT_TYPES.length) {
                            inventory.setItem(i, getEffectTypeIcon(EFFECT_TYPES[effectIndex]));
                        } else {
                            inventory.setItem(i, null);
                        }
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private ItemStack getPrevPageIcon() {
        ItemStack icon = new ItemStack(page > 0 ? Material.ARROW : Material.GRAY_DYE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor(page > 0 ? "&e◀ 上一页" : "&7已是第一页"));
        return icon;
    }

    private ItemStack getNextPageIcon() {
        int maxPage = (EFFECT_TYPES.length - 1) / EFFECTS_PER_PAGE;
        boolean hasNext = page < maxPage;
        ItemStack icon = new ItemStack(hasNext ? Material.ARROW : Material.GRAY_DYE);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor(hasNext ? "&e下一页 ▶" : "&7已是最后一页"));
        return icon;
    }

    private ItemStack getEffectTypeIcon(PotionEffectType type) {
        ItemStack icon = new ItemStack(Material.POTION);
        String effectName = getEffectName(type);
        ItemStackUtil.setItemDisplayName(icon, ColorHelper.parseColor("&b" + effectName));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7ID: &f" + type.getKey().getKey()));
        lore.add("");
        lore.add(ColorHelper.parseColor("&e点击添加此效果"));
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
        module.debug("FoodEffectSelectGui click: key=" + key + ", click=" + click);

        switch (key) {
            case '<' -> {
                if (page > 0) {
                    page--;
                    refreshInventory();
                }
            }
            case '>' -> {
                int maxPage = (EFFECT_TYPES.length - 1) / EFFECTS_PER_PAGE;
                if (page < maxPage) {
                    page++;
                    refreshInventory();
                }
            }
            case 'B' -> {
                parentGui.refresh();
                parentGui.open();
            }
            case 'E' -> {
                // 计算点击的效果索引
                int[] effectSlots = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34,
                    37, 38, 39, 40, 41, 42, 43
                };
                int slotIndex = -1;
                for (int j = 0; j < effectSlots.length; j++) {
                    if (effectSlots[j] == index) {
                        slotIndex = j;
                        break;
                    }
                }
                if (slotIndex >= 0) {
                    int effectIndex = page * EFFECTS_PER_PAGE + slotIndex;
                    if (effectIndex < EFFECT_TYPES.length) {
                        addEffect(EFFECT_TYPES[effectIndex]);
                    }
                }
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void addEffect(PotionEffectType type) {
        ItemStack item = storedItem.getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        FoodComponent food;
        if (meta.hasFood()) {
            food = meta.getFood();
        } else {
            food = meta.getFood();
            food.setNutrition(4);
            food.setSaturation(2.4f);
        }

        // 添加默认效果: 等级1, 持续10秒, 100%概率
        PotionEffect effect = new PotionEffect(type, 200, 0); // 200 ticks = 10秒
        List<Object> effects = PaperUtil.getFoodEffects(food);
        Object newEffect = PaperUtil.addFoodEffect(food, effect, 1.0f);
        if (newEffect != null) {
            effects.add(newEffect);
            PaperUtil.setFoodEffects(food, effects);
        }
        
        meta.setFood(food);
        item.setItemMeta(meta);
        module.getDatabase().saveItem(storedItem);
        
        ItemManagerMessages.properties__food_effect_added.t(player);
        
        // 返回效果列表
        parentGui.refresh();
        parentGui.open();
    }
}
