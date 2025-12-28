package cn.mcloli.dreamrealms.modules.ownerbind.listener;

import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import cn.mcloli.dreamrealms.modules.ownerbind.lang.OwnerBindMessages;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * QuickShop-Hikari 商店创建监听器
 * 阻止绑定物品上架到 QuickShop 商店
 */
public class QuickShopListener implements Listener {

    private final OwnerBindModule module;

    public QuickShopListener(OwnerBindModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onShopCreate(ShopCreateEvent event) {
        // 只处理 PRE_CANCELLABLE 阶段（可取消的预创建阶段）
        if (!event.isPhase(Phase.PRE_CANCELLABLE)) {
            return;
        }
        
        // 获取玩家
        UUID userUuid = event.user().getUniqueId();
        if (userUuid == null) {
            return;
        }
        
        Player player = Bukkit.getPlayer(userUuid);
        if (player == null) {
            return;
        }
        
        // 获取玩家手持物品（PRE_CANCELLABLE 阶段 shop 可能为 null）
        ItemStack item = player.getInventory().getItemInMainHand();
        
        module.debug("ShopCreateEvent PRE_CANCELLABLE 触发 - 玩家: " + player.getName());
        module.debug("手持物品: " + item.getType() + " x" + item.getAmount());
        module.debug("是否有绑定信息: " + module.hasAnyBindInfo(item));
        
        if (module.hasAnyBindInfo(item)) {
            event.setCancelled(true, (String) null);
            OwnerBindMessages.anti_market_tip.tm(player);
            module.debug("阻止创建绑定物品商店: " + item.getType());
        }
    }
}
