package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.lang.ItemLanguage;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性工具类
 */
public class AttributeUtil {

    /**
     * 获取所有属性
     */
    @NotNull
    public static List<Attribute> getAllAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        for (Attribute attr : Registry.ATTRIBUTE) {
            attributes.add(attr);
        }
        return attributes;
    }

    /**
     * 获取属性的本地化名称
     */
    @NotNull
    public static String getAttributeName(@NotNull Attribute attribute) {
        ItemLanguage lang = DreamRealms.getInstance().getItemLanguage();
        if (lang != null && lang.isLoaded()) {
            String translationKey = attribute.getTranslationKey();
            String name = lang.get(translationKey);
            if (name != null) {
                return name;
            }
        }
        return formatAttributeKey(attribute.getKey().getKey());
    }

    /**
     * 格式化属性键名
     */
    @NotNull
    private static String formatAttributeKey(@NotNull String key) {
        // generic.max_health -> Max Health
        String[] parts = key.replace("generic.", "")
                .replace("player.", "")
                .split("_");
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
     * 获取装备槽位的显示名称
     */
    @NotNull
    public static String getSlotName(@Nullable EquipmentSlotGroup slot) {
        if (slot == null || slot == EquipmentSlotGroup.ANY) {
            return "任意槽位";
        }
        return switch (slot.toString().toLowerCase()) {
            case "mainhand" -> "主手";
            case "offhand" -> "副手";
            case "head" -> "头部";
            case "chest" -> "胸甲";
            case "legs" -> "护腿";
            case "feet" -> "靴子";
            case "hand" -> "手持";
            case "armor" -> "护甲";
            case "body" -> "身体";
            default -> slot.toString();
        };
    }
}
