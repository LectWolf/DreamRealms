package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.hook.CraftEngineHook;
import cn.mcloli.dreamrealms.hook.ItemsAdderHook;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物品构建工具类
 * 支持 CraftEngine、ItemsAdder 和原版物品
 */
public class ItemBuilder {

    /**
     * 解析物品材质
     * 支持格式:
     * - craftengine:namespace:item_id
     * - itemsadder:namespace:item_id
     * - MATERIAL_NAME
     * - MATERIAL_NAME:damage
     *
     * @param material 材质字符串
     * @return 物品，解析失败返回 PAPER
     */
    @NotNull
    public static ItemStack parseItem(String material) {
        if (material == null || material.isEmpty()) {
            return new ItemStack(Material.PAPER);
        }

        // CraftEngine 物品
        if (material.startsWith("craftengine:")) {
            ItemStack item = CraftEngineHook.getItem(material);
            if (item != null) {
                return item;
            }
            return new ItemStack(Material.PAPER);
        }

        // ItemsAdder 物品
        if (material.startsWith("itemsadder:")) {
            ItemStack item = ItemsAdderHook.getItem(material);
            if (item != null) {
                return item;
            }
            return new ItemStack(Material.PAPER);
        }

        // 标准物品
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(material);
        if (pair == null) {
            return new ItemStack(Material.PAPER);
        }
        return ItemStackUtil.legacy(pair);
    }

    /**
     * 创建物品
     *
     * @param material        材质 (支持 craftengine:xxx)
     * @param displayName     显示名称 (支持颜色代码)
     * @param lore            Lore (支持颜色代码)
     * @param customModelData CustomModelData (可为 null)
     * @return 构建的物品
     */
    @NotNull
    public static ItemStack create(String material, @Nullable String displayName,
                                   @Nullable List<String> lore, @Nullable Integer customModelData) {
        ItemStack item = parseItem(material);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        // 显示名称
        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(ColorHelper.parseColor(displayName));
        }

        // Lore
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = lore.stream()
                    .map(ColorHelper::parseColor)
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
        }

        // CustomModelData
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 设置物品显示名称
     */
    public static void setDisplayName(@NotNull ItemStack item, @Nullable String name) {
        if (name == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorHelper.parseColor(name));
            item.setItemMeta(meta);
        }
    }

    /**
     * 设置物品 Lore
     */
    public static void setLore(@NotNull ItemStack item, @Nullable List<String> lore) {
        if (lore == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> coloredLore = lore.stream()
                    .map(ColorHelper::parseColor)
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
    }

    /**
     * 设置 CustomModelData
     */
    public static void setCustomModelData(@NotNull ItemStack item, @Nullable Integer data) {
        if (data == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(data);
            item.setItemMeta(meta);
        }
    }
}
