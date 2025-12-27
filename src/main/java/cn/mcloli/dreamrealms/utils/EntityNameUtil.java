package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.lang.ItemLanguage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 实体名称工具类
 * 支持获取实体的本地化名称
 */
public class EntityNameUtil {

    /**
     * 获取实体的显示名称
     * 优先级: 自定义名称 > 语言文件翻译 > 格式化名称
     *
     * @param entity 实体
     * @return 实体名称
     */
    @NotNull
    public static String getEntityName(@Nullable Entity entity) {
        if (entity == null) {
            return "未知";
        }

        // 1. 优先使用自定义名称
        if (entity.getCustomName() != null) {
            return entity.getCustomName();
        }

        // 2. 使用语言文件获取本地化名称
        return getEntityName(entity.getType());
    }

    /**
     * 获取实体类型的本地化名称
     *
     * @param entityType 实体类型
     * @return 实体名称
     */
    @NotNull
    public static String getEntityName(@NotNull EntityType entityType) {
        // 尝试从语言文件获取翻译
        ItemLanguage lang = DreamRealms.getInstance().getItemLanguage();
        if (lang != null && lang.isLoaded()) {
            String entityKey = "entity.minecraft." + entityType.name().toLowerCase();
            String translated = lang.get(entityKey);
            if (translated != null) {
                return translated;
            }
        }

        // 回退到格式化名称
        return formatEntityName(entityType.name());
    }

    /**
     * 格式化实体名称 (ZOMBIE -> Zombie, WITHER_SKELETON -> Wither Skeleton)
     */
    @NotNull
    private static String formatEntityName(@NotNull String name) {
        String formatted = name.toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : formatted.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
