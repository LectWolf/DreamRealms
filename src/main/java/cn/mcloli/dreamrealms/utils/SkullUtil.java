package cn.mcloli.dreamrealms.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * 头颅工具类
 */
@SuppressWarnings("deprecation")
public class SkullUtil {

    /**
     * 创建玩家头颅
     * @param playerName 玩家名
     * @return 头颅物品
     */
    @NotNull
    public static ItemStack createPlayerSkull(@NotNull String playerName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        setOwner(skull, playerName);
        return skull;
    }

    /**
     * 创建自定义纹理头颅
     * @param texture Base64 纹理
     * @return 头颅物品
     */
    @NotNull
    public static ItemStack createTextureSkull(@NotNull String texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        setTexture(skull, texture);
        return skull;
    }

    /**
     * 设置头颅玩家
     * @param item 头颅物品
     * @param playerName 玩家名
     */
    public static void setOwner(@NotNull ItemStack item, @NotNull String playerName) {
        if (!isSkull(item)) return;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return;

        meta.setOwner(playerName);
        item.setItemMeta(meta);
    }

    /**
     * 设置头颅纹理 (Base64)
     * @param item 头颅物品
     * @param texture Base64 纹理字符串
     */
    public static void setTexture(@NotNull ItemStack item, @NotNull String texture) {
        if (!isSkull(item)) return;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return;

        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", texture));

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);

            item.setItemMeta(meta);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 检查物品是否为头颅
     */
    public static boolean isSkull(@Nullable ItemStack item) {
        if (item == null) return false;
        String typeName = item.getType().name();
        return typeName.contains("HEAD") || typeName.contains("SKULL");
    }
}
