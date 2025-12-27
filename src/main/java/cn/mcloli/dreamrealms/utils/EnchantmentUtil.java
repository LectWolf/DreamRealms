package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.DreamRealms;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 附魔工具类
 */
public class EnchantmentUtil {

    private static final Map<String, String> ENCHANT_NAMES = new HashMap<>();

    static {
        // 初始化附魔中文名称映射
        ENCHANT_NAMES.put("protection", "保护");
        ENCHANT_NAMES.put("fire_protection", "火焰保护");
        ENCHANT_NAMES.put("feather_falling", "摔落保护");
        ENCHANT_NAMES.put("blast_protection", "爆炸保护");
        ENCHANT_NAMES.put("projectile_protection", "弹射物保护");
        ENCHANT_NAMES.put("respiration", "水下呼吸");
        ENCHANT_NAMES.put("aqua_affinity", "水下速掘");
        ENCHANT_NAMES.put("thorns", "荆棘");
        ENCHANT_NAMES.put("depth_strider", "深海探索者");
        ENCHANT_NAMES.put("frost_walker", "冰霜行者");
        ENCHANT_NAMES.put("binding_curse", "绑定诅咒");
        ENCHANT_NAMES.put("soul_speed", "灵魂疾行");
        ENCHANT_NAMES.put("swift_sneak", "迅捷潜行");
        
        ENCHANT_NAMES.put("sharpness", "锋利");
        ENCHANT_NAMES.put("smite", "亡灵杀手");
        ENCHANT_NAMES.put("bane_of_arthropods", "节肢杀手");
        ENCHANT_NAMES.put("knockback", "击退");
        ENCHANT_NAMES.put("fire_aspect", "火焰附加");
        ENCHANT_NAMES.put("looting", "抢夺");
        ENCHANT_NAMES.put("sweeping_edge", "横扫之刃");
        
        ENCHANT_NAMES.put("efficiency", "效率");
        ENCHANT_NAMES.put("silk_touch", "精准采集");
        ENCHANT_NAMES.put("unbreaking", "耐久");
        ENCHANT_NAMES.put("fortune", "时运");
        
        ENCHANT_NAMES.put("power", "力量");
        ENCHANT_NAMES.put("punch", "冲击");
        ENCHANT_NAMES.put("flame", "火矢");
        ENCHANT_NAMES.put("infinity", "无限");
        
        ENCHANT_NAMES.put("luck_of_the_sea", "海之眷顾");
        ENCHANT_NAMES.put("lure", "饵钓");
        
        ENCHANT_NAMES.put("loyalty", "忠诚");
        ENCHANT_NAMES.put("impaling", "穿刺");
        ENCHANT_NAMES.put("riptide", "激流");
        ENCHANT_NAMES.put("channeling", "引雷");
        
        ENCHANT_NAMES.put("multishot", "多重射击");
        ENCHANT_NAMES.put("quick_charge", "快速装填");
        ENCHANT_NAMES.put("piercing", "穿透");
        
        ENCHANT_NAMES.put("density", "密度");
        ENCHANT_NAMES.put("breach", "破甲");
        ENCHANT_NAMES.put("wind_burst", "风爆");
        
        ENCHANT_NAMES.put("mending", "经验修补");
        ENCHANT_NAMES.put("vanishing_curse", "消失诅咒");
    }

    /**
     * 获取所有附魔
     */
    @NotNull
    public static List<Enchantment> getAllEnchantments() {
        List<Enchantment> enchants = new ArrayList<>();
        for (Enchantment enchant : Registry.ENCHANTMENT) {
            enchants.add(enchant);
        }
        return enchants;
    }

    /**
     * 获取附魔的中文名称
     */
    @NotNull
    public static String getEnchantmentName(@NotNull Enchantment enchantment) {
        String key = enchantment.getKey().getKey();
        String name = ENCHANT_NAMES.get(key);
        if (name != null) {
            return name;
        }
        // 如果没有中文名，返回格式化的英文名
        return formatEnchantmentKey(key);
    }

    /**
     * 格式化附魔键名
     */
    @NotNull
    private static String formatEnchantmentKey(@NotNull String key) {
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 根据名称获取附魔
     */
    @NotNull
    public static Enchantment getEnchantment(@NotNull String name) {
        // 先尝试直接获取
        NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase());
        Enchantment enchant = Registry.ENCHANTMENT.get(key);
        if (enchant != null) {
            return enchant;
        }
        
        // 尝试中文名匹配
        for (Map.Entry<String, String> entry : ENCHANT_NAMES.entrySet()) {
            if (entry.getValue().equals(name)) {
                key = NamespacedKey.minecraft(entry.getKey());
                enchant = Registry.ENCHANTMENT.get(key);
                if (enchant != null) {
                    return enchant;
                }
            }
        }
        
        return null;
    }
}
