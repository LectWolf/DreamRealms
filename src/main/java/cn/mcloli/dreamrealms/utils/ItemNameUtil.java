package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.hook.CraftEngineHook;
import cn.mcloli.dreamrealms.hook.ItemsAdderHook;
import cn.mcloli.dreamrealms.lang.ItemLanguage;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

/**
 * 物品名称工具类
 * 支持获取原版物品、CraftEngine、ItemsAdder 物品的本地化名称
 */
public class ItemNameUtil {

    /**
     * 获取物品的显示名称
     * 优先级: 自定义名称 > CraftEngine > ItemsAdder > 语言文件 > 格式化名称
     *
     * @param item 物品
     * @return 物品名称
     */
    @NotNull
    public static String getItemName(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "空";
        }

        // 1. 优先使用自定义显示名称
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }

        // 2. 尝试获取 CraftEngine 物品名称
        if (CraftEngineHook.isAvailable()) {
            String ceName = getCraftEngineName(item);
            if (ceName != null) {
                return ceName;
            }
        }

        // 3. 尝试获取 ItemsAdder 物品名称
        if (ItemsAdderHook.isAvailable()) {
            String iaName = getItemsAdderName(item);
            if (iaName != null) {
                return iaName;
            }
        }

        // 4. 使用语言文件获取本地化名称
        ItemLanguage lang = DreamRealms.getInstance().getItemLanguage();
        if (lang != null && lang.isLoaded()) {
            return lang.getItemName(item);
        }

        // 5. 回退到格式化名称
        return formatMaterialName(item.getType());
    }

    /**
     * 获取 CraftEngine 物品名称
     * 解析 item-name 中的 <l10n:xxx> 标签并替换为翻译
     */
    @Nullable
    private static String getCraftEngineName(ItemStack item) {
        try {
            CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(item);
            if (customItem != null) {
                // 获取物品 ID (如 dreamrealms:normal_key)
                String itemId = customItem.id().toString();
                
                // 尝试从物品的 ItemMeta 获取显示名称 (包含 MiniMessage 格式)
                ItemStack built = customItem.buildItemStack(1);
                if (built != null && built.hasItemMeta()) {
                    ItemMeta meta = built.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        String displayName = meta.getDisplayName();
                        
                        // 解析 <l10n:xxx> 标签
                        String resolved = resolveL10nTags(displayName);
                        
                        // 转换 MiniMessage 为传统格式
                        if (resolved.contains("<") && resolved.contains(">")) {
                            return LegacyComponentSerializer.legacySection()
                                    .serialize(AdventureUtil.miniMessage(resolved));
                        }
                        return resolved;
                    }
                }
                
                // 回退：尝试从语言文件获取翻译
                ItemLanguage lang = DreamRealms.getInstance().getItemLanguage();
                if (lang != null && lang.isLoaded()) {
                    String itemName = itemId.contains(":") ? itemId.substring(itemId.indexOf(":") + 1) : itemId;
                    String langKey = "item." + itemName;
                    String translated = lang.get(langKey);
                    if (translated != null) {
                        return translated;
                    }
                }
                
                // 最终回退：格式化物品 ID (normal_key -> Normal Key)
                String itemName = itemId.contains(":") ? itemId.substring(itemId.indexOf(":") + 1) : itemId;
                return formatItemId(itemName);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
    
    /**
     * 解析字符串中的 <l10n:xxx> 标签，替换为翻译
     */
    @NotNull
    private static String resolveL10nTags(String text) {
        if (text == null || !text.contains("<l10n:")) {
            return text != null ? text : "";
        }
        
        ItemLanguage lang = DreamRealms.getInstance().getItemLanguage();
        if (lang == null || !lang.isLoaded()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            int start = text.indexOf("<l10n:", i);
            if (start == -1) {
                result.append(text.substring(i));
                break;
            }
            
            // 添加 <l10n: 之前的内容
            result.append(text.substring(i, start));
            
            // 查找结束的 >
            int end = text.indexOf(">", start);
            if (end == -1) {
                result.append(text.substring(start));
                break;
            }
            
            // 提取翻译 key
            String key = text.substring(start + 6, end); // 6 = "<l10n:".length()
            String translated = lang.get(key);
            if (translated != null) {
                result.append(translated);
            } else {
                // 没有翻译，保留原始标签或使用 key
                result.append(key);
            }
            
            i = end + 1;
        }
        
        return result.toString();
    }
    
    /**
     * 格式化物品 ID (normal_key -> Normal Key)
     */
    @NotNull
    private static String formatItemId(@NotNull String itemId) {
        String name = itemId.toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toCharArray()) {
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

    /**
     * 获取 ItemsAdder 物品名称
     */
    @Nullable
    private static String getItemsAdderName(ItemStack item) {
        try {
            CustomStack customStack = CustomStack.byItemStack(item);
            if (customStack != null) {
                String displayName = customStack.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    return displayName;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * 格式化材质名称 (DIAMOND_SWORD -> Diamond Sword)
     */
    @NotNull
    private static String formatMaterialName(@NotNull Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toCharArray()) {
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
