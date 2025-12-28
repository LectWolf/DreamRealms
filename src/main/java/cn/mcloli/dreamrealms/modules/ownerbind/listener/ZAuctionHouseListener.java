package cn.mcloli.dreamrealms.modules.ownerbind.listener;

import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import cn.mcloli.dreamrealms.modules.ownerbind.lang.OwnerBindMessages;
import fr.maxlego08.zauctionhouse.api.event.events.AuctionPreSellEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ZAuctionHouseListener implements Listener {

    private final OwnerBindModule module;

    public ZAuctionHouseListener(OwnerBindModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAuctionPreSell(AuctionPreSellEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemStack();
        
        module.debug("AuctionPreSellEvent 触发 - 玩家: " + player.getName());
        module.debug("上架物品: " + item.getType() + " x" + item.getAmount());
        module.debug("是否有绑定信息: " + module.hasAnyBindInfo(item));
        
        if (module.hasAnyBindInfo(item)) {
            event.setCancelled(true);
            OwnerBindMessages.anti_market_tip.tm(player);
            module.debug("阻止上架绑定物品: " + item.getType());
        }
    }
}
