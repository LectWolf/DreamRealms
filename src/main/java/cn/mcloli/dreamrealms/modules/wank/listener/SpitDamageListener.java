package cn.mcloli.dreamrealms.modules.wank.listener;

import cn.mcloli.dreamrealms.modules.wank.WankModule;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class SpitDamageListener implements Listener {

    private final WankModule module;

    public SpitDamageListener(WankModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpitHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof LlamaSpit spit)) return;
        if (!(spit.getShooter() instanceof Player shooter)) return;

        var config = module.getModuleConfig();
        if (!config.isShootDamageEnabled()) {
            // 伤害未启用，取消碰撞效果
            event.setCancelled(true);
            spit.remove();
            return;
        }

        // 击中实体
        if (event.getHitEntity() != null) {
            double damage = config.getShootDamage();
            if (event.getHitEntity() instanceof org.bukkit.entity.LivingEntity target) {
                if (damage > 0) {
                    target.damage(damage, shooter);
                } else {
                    // damage = 0 时只有受击效果
                    target.damage(0.001, shooter);
                }
            }
        }
    }
}
