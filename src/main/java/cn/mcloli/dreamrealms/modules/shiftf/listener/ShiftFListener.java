package cn.mcloli.dreamrealms.modules.shiftf.listener;

import cn.mcloli.dreamrealms.modules.shiftf.ShiftFModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ShiftFListener implements Listener {

    private final ShiftFModule module;

    public ShiftFListener(ShiftFModule module) {
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }

        String command = module.getModuleConfig().getCommand();
        if (command == null || command.isEmpty()) {
            return;
        }

        module.debug("玩家 " + player.getName() + " 触发 Shift+F 快捷键");

        // 触发命令预处理事件，允许其他插件拦截
        PlayerCommandPreprocessEvent callEvent = new PlayerCommandPreprocessEvent(player, "/" + command);
        Bukkit.getPluginManager().callEvent(callEvent);

        if (!callEvent.isCancelled() && !callEvent.getMessage().isEmpty() && callEvent.getMessage().startsWith("/")) {
            event.setCancelled(true);
            Bukkit.dispatchCommand(player, callEvent.getMessage().substring(1));
        }
    }
}
