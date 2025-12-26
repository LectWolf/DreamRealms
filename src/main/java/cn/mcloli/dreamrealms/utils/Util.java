package cn.mcloli.dreamrealms.utils;

import cn.mcloli.dreamrealms.DreamRealms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 通用工具类
 */
public class Util {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.##");
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    /**
     * 解析整数
     */
    public static Optional<Integer> parseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 解析双精度浮点数
     */
    public static Optional<Double> parseDouble(String str) {
        try {
            return Optional.of(Double.parseDouble(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 解析长整数
     */
    public static Optional<Long> parseLong(String str) {
        try {
            return Optional.of(Long.parseLong(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 格式化数字
     */
    public static String formatNumber(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    /**
     * 检查字符串是否为有效 UUID
     */
    public static boolean isValidUUID(String str) {
        return str != null && UUID_PATTERN.matcher(str).matches();
    }

    /**
     * 解析 UUID
     */
    public static Optional<UUID> parseUUID(String str) {
        if (!isValidUUID(str)) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * 检查物品是否为空
     */
    public static boolean isAir(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
    }

    /**
     * 检查物品是否不为空
     */
    public static boolean notAir(@Nullable ItemStack item) {
        return !isAir(item);
    }

    /**
     * 给玩家物品，满了掉落
     */
    public static void giveItem(@NotNull Player player, @NotNull ItemStack item) {
        if (isAir(item)) return;
        var overflow = player.getInventory().addItem(item.clone());
        for (ItemStack drop : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    /**
     * 给玩家物品，满了掉落
     */
    public static void giveItems(@NotNull Player player, @NotNull Iterable<ItemStack> items) {
        for (ItemStack item : items) {
            giveItem(player, item);
        }
    }

    /**
     * 序列化位置
     */
    public static String serializeLocation(@NotNull Location loc) {
        return loc.getWorld().getName() + "," +
                loc.getX() + "," +
                loc.getY() + "," +
                loc.getZ() + "," +
                loc.getYaw() + "," +
                loc.getPitch();
    }

    /**
     * 反序列化位置
     */
    @Nullable
    public static Location deserializeLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        if (parts.length < 4) return null;

        try {
            var world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 创建目录
     */
    public static boolean mkdirs(File file) {
        if (file.exists()) return true;
        return file.mkdirs();
    }

    /**
     * 获取插件实例
     */
    public static DreamRealms plugin() {
        return DreamRealms.getInstance();
    }

    /**
     * 在主线程执行
     */
    public static void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            plugin().getScheduler().runTask(runnable);
        }
    }

    /**
     * 异步执行
     */
    public static void runAsync(Runnable runnable) {
        plugin().getScheduler().runTaskAsync(runnable);
    }

    /**
     * 延迟执行 (tick)
     */
    public static void runLater(Runnable runnable, long delayTicks) {
        plugin().getScheduler().runTaskLater(runnable, delayTicks);
    }
}
