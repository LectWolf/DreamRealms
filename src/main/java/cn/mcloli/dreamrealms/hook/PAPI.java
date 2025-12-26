package cn.mcloli.dreamrealms.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * PlaceholderAPI 集成
 */
public class PAPI {

    private static boolean available = false;

    /**
     * 初始化
     */
    public static void init() {
        available = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * 检查是否可用
     */
    public static boolean isAvailable() {
        return available;
    }

    /**
     * 替换占位符
     */
    public static String setPlaceholders(@Nullable OfflinePlayer player, String text) {
        if (!available || text == null) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    /**
     * 替换占位符 (列表)
     */
    public static List<String> setPlaceholders(@Nullable OfflinePlayer player, List<String> list) {
        if (!available || list == null) return list;
        List<String> result = new ArrayList<>();
        for (String s : list) {
            result.add(PlaceholderAPI.setPlaceholders(player, s));
        }
        return result;
    }

    /**
     * 检查文本是否包含占位符
     */
    public static boolean containsPlaceholders(String text) {
        if (!available || text == null) return false;
        return PlaceholderAPI.containsPlaceholders(text);
    }
}
