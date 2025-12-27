package cn.mcloli.dreamrealms.modules.ownerbind.listener;

import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import cn.mcloli.dreamrealms.modules.ownerbind.lang.OwnerBindMessages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import studio.trc.bukkit.globalmarketplus.api.event.AuctionStartEvent;
import studio.trc.bukkit.globalmarketplus.api.event.MerchandiseBuyEvent;
import studio.trc.bukkit.globalmarketplus.api.event.MerchandiseSellEvent;

public class GlobalMarketPlusListener implements Listener {

    private final OwnerBindModule module;

    public GlobalMarketPlusListener(OwnerBindModule module) {
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMerchandiseBuy(MerchandiseBuyEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (module.hasAnyBindInfo(item)) {
            event.setCancelled(true);
            OwnerBindMessages.anti_market_tip.tm(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMerchandiseSell(MerchandiseSellEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (module.hasAnyBindInfo(item)) {
            event.setCancelled(true);
            OwnerBindMessages.anti_market_tip.tm(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAuctionStart(AuctionStartEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (module.hasAnyBindInfo(item)) {
            event.setCancelled(true);
            OwnerBindMessages.anti_market_tip.tm(player);
        }
    }
}
