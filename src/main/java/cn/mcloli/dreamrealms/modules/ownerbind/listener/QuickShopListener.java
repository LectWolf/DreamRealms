package cn.mcloli.dreamrealms.modules.ownerbind.listener;

import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import cn.mcloli.dreamrealms.modules.ownerbind.lang.OwnerBindMessages;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * QuickShop-Hikari 挂钩监听器
 * 阻止绑定物品创建商店
 */
public class QuickShopListener implements Listener {

    private final OwnerBindModule module;

    public QuickShopListener(OwnerBindModule module) {
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onShopCreate(ShopCreateEvent event) {
        Shop shop = event.getShop();
        ItemStack item = shop.getItem();
        if (module.hasAnyBindInfo(item)) {
            event.setCancelled(true, Component.text("物品已绑定，无法创建商店"));
            UUID creatorUuid = event.getCreator().getUniqueIdIfRealPlayer().orElse(null);
            if (creatorUuid != null) {
                Player player = Bukkit.getPlayer(creatorUuid);
                if (player != null) {
                    OwnerBindMessages.anti_market_tip.tm(player);
                }
            }
        }
    }
}
