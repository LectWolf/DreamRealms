package cn.mcloli.dreamrealms.modules.timesync.listener;

import cn.mcloli.dreamrealms.modules.timesync.TimeSyncModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class TimeSyncListener implements Listener {

    private final TimeSyncModule module;

    public TimeSyncListener(TimeSyncModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!module.isModuleEnabled()) return;
        if (!module.getModuleConfig().isDisableSleep()) return;

        if (module.shouldSync(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            module.debug("阻止玩家 " + event.getPlayer().getName() + " 睡觉");
        }
    }
}
