package cn.mcloli.dreamrealms.modules.timesync.listener;

import cn.mcloli.dreamrealms.modules.timesync.TimeSyncModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;

public class TimeSyncListener implements Listener {

    private final TimeSyncModule module;

    public TimeSyncListener(TimeSyncModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        if (!module.isModuleEnabled()) return;
        if (!module.getModuleConfig().isDisableSleep()) return;

        // 只阻止睡觉导致的时间跳过
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            if (module.shouldSync(event.getWorld())) {
                event.setCancelled(true);
                module.debug("阻止世界 " + event.getWorld().getName() + " 跳过黑夜");
            }
        }
    }
}
