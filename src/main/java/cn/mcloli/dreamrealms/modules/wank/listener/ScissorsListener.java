package cn.mcloli.dreamrealms.modules.wank.listener;

import cn.mcloli.dreamrealms.modules.wank.WankModule;
import cn.mcloli.dreamrealms.modules.wank.lang.WankMessages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.utils.Pair;

public class ScissorsListener implements Listener {

    private final WankModule module;

    public ScissorsListener(WankModule module) {
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!module.getModuleConfig().isScissorsEnabled()) {
            return;
        }

        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查是否手持剪刀
        if (item.getType() != Material.SHEARS) {
            return;
        }

        // 不能剪自己
        if (player.equals(target)) {
            return;
        }

        // 检查目标是否已经被剪过
        if (module.isCut(target.getUniqueId())) {
            WankMessages.scissors_already_cut.t(player, Pair.of("%target%", target.getName()));
            return;
        }

        // 剪掉目标
        module.cutPlayer(target.getUniqueId());

        // 消耗剪刀耐久
        item.setDurability((short) (item.getDurability() + 1));
        if (item.getDurability() >= item.getType().getMaxDurability()) {
            player.getInventory().setItemInMainHand(null);
        }

        // 通知双方
        WankMessages.scissors_cut_success.t(player, Pair.of("%target%", target.getName()));
        WankMessages.scissors_cut_victim.t(target, Pair.of("%player%", player.getName()));

        event.setCancelled(true);
    }
}
