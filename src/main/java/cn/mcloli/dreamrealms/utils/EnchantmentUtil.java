package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.lang.ItemLanguage;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 附魔工具类
 */
public class EnchantmentUtil {

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
     * 获取附魔的本地化名称
     * 从 Minecraft 语言文件获取翻译
     */
    @NotNull
    public static String getEnchantmentName(@NotNull Enchantment enchantment) {
        ItemLanguage lang = DreamRealms.getInstance().getItemLanguage();
        if (lang != null && lang.isLoaded()) {
            // 使用 Bukkit 的 Translatable 接口获取翻译键
            String translationKey = enchantment.getTranslationKey();
            String name = lang.get(translationKey);
            if (name != null) {
                return name;
            }
        }
        // 回退到格式化的英文名
        return formatEnchantmentKey(enchantment.getKey().getKey());
    }

    /**
     * 格式化附魔键名 (aqua_affinity -> Aqua Affinity)
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
     * 根据键名获取附魔
     */
    @Nullable
    public static Enchantment getEnchantment(@NotNull String name) {
        NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase());
        return Registry.ENCHANTMENT.get(key);
    }
}
