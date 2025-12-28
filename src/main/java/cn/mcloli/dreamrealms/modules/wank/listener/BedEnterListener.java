package cn.mcloli.dreamrealms.modules.wank.listener;

import cn.mcloli.dreamrealms.modules.wank.WankModule;
import cn.mcloli.dreamrealms.modules.wank.lang.WankMessages;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.concurrent.ThreadLocalRandom;

public class BedEnterListener implements Listener {

    private final WankModule module;

    public BedEnterListener(WankModule module) {
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        Player player = event.getPlayer();

        // 检查是否已达到最大次数
        if (module.getWankTimes(player.getUniqueId()) >= module.getMaxWankTimes(player.getUniqueId())) {
            return;
        }

        // 概率触发
        double chance = module.getModuleConfig().getPromptChance();
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }

        // 随机延迟后发送提示
        int delay = module.getModuleConfig().getRandomPromptDelay();
        Util.runLater(() -> {
            // 再次检查玩家是否还在睡觉
            if (player.isOnline() && player.isSleeping()) {
                // 检查是否已达到最大次数
                if (module.getWankTimes(player.getUniqueId()) >= module.getMaxWankTimes(player.getUniqueId())) {
                    return;
                }
                WankMessages.prompt.t(player);
            }
        }, delay * 20L);
    }
}
