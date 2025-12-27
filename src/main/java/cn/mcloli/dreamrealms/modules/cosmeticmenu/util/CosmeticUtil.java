package cn.mcloli.dreamrealms.modules.cosmeticmenu.util;

import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * HMCCosmetics 工具类
 */
public class CosmeticUtil {

    /**
     * 获取玩家拥有的指定槽位的时装列表
     */
    public static List<Cosmetic> getPlayerCosmeticsForSlot(Player player, CosmeticSlot slot) {
        List<Cosmetic> result = new ArrayList<>();
        
        for (Cosmetic cosmetic : HMCCosmeticsAPI.getAllCosmetics()) {
            if (cosmetic.getSlot() != slot) continue;
            
            // 检查玩家是否有权限使用该时装
            String permission = cosmetic.getPermission();
            if (permission == null || permission.isEmpty() || player.hasPermission(permission)) {
                result.add(cosmetic);
            }
        }
        
        return result;
    }

    /**
     * 检查玩家是否已装备指定时装
     */
    public static boolean isEquipped(Player player, Cosmetic cosmetic) {
        CosmeticUser user = HMCCosmeticsAPI.getUser(player.getUniqueId());
        if (user == null) return false;
        
        Cosmetic equipped = user.getCosmetic(cosmetic.getSlot());
        return equipped != null && equipped.getId().equals(cosmetic.getId());
    }

    /**
     * 检查玩家在指定槽位是否有装备时装
     */
    public static boolean hasEquippedInSlot(Player player, CosmeticSlot slot) {
        CosmeticUser user = HMCCosmeticsAPI.getUser(player.getUniqueId());
        if (user == null) return false;
        
        return user.getCosmetic(slot) != null;
    }

    /**
     * 获取时装的显示物品
     */
    public static ItemStack getCosmeticDisplayItem(Cosmetic cosmetic) {
        try {
            return cosmetic.getItem();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取时装名称
     */
    public static String getCosmeticName(Cosmetic cosmetic) {
        try {
            ItemStack item = cosmetic.getItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().getDisplayName();
            }
        } catch (Exception ignored) {}
        return cosmetic.getId();
    }

    /**
     * 获取玩家拥有的所有时装总数
     */
    public static int getTotalCosmeticsCount(Player player) {
        int total = 0;
        for (Cosmetic cosmetic : HMCCosmeticsAPI.getAllCosmetics()) {
            String permission = cosmetic.getPermission();
            if (permission == null || permission.isEmpty() || player.hasPermission(permission)) {
                total++;
            }
        }
        return total;
    }
}
