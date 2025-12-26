package cn.mcloli.dreamrealms.gui;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractPluginHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.function.BiConsumer;

/**
 * GUI 管理器
 */
@AutoRegister
public class GuiManager extends AbstractPluginHolder implements Listener {

    private BiConsumer<Player, IGui> disableAction = (player, gui) -> {
        try {
            player.sendTitle("§e请等等", "§f管理员正在更新插件", 10, 30, 10);
        } catch (Throwable ignored) {
        }
    };
    private boolean disabled = false;

    public GuiManager(DreamRealms plugin) {
        super(plugin, true);
        registerEvents(this);
    }

    /**
     * 打开 GUI
     */
    public void openGui(IGui gui) {
        if (disabled) return;
        Player player = gui.getPlayer();
        if (player == null) return;

        Inventory inv = gui.newInventory();
        if (inv != null) {
            InventoryHolder holder = getHolder(inv);
            if (holder == gui) {
                player.openInventory(inv);
            } else {
                player.closeInventory();
                warn("试图为玩家 " + player.getName() + " 打开界面 " + gui.getClass().getName() + " 时，界面未设置 InventoryHolder 为自身实例");
            }
        } else if (!gui.allowNullInventory()) {
            warn("试图为玩家 " + player.getName() + " 打开界面 " + gui.getClass().getName() + " 时，程序返回了 null");
        }
    }

    @Override
    public void onDisable() {
        disabled = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            IGui gui = getGuiHolder(view.getTopInventory());
            if (gui != null) {
                gui.onClose(view);
                player.closeInventory();
                if (disableAction != null) {
                    disableAction.accept(player, gui);
                }
            }
        }
    }

    /**
     * 设置插件卸载时的处理
     */
    public void setDisableAction(@Nullable BiConsumer<Player, IGui> action) {
        this.disableAction = action;
    }

    /**
     * 获取玩家当前打开的 GUI
     */
    @Nullable
    public IGui getOpeningGui(Player player) {
        if (disabled) return null;
        return getGuiHolder(player.getOpenInventory().getTopInventory());
    }

    @Nullable
    private IGui getGuiHolder(Inventory inv) {
        InventoryHolder holder = getHolder(inv);
        if (holder instanceof IGui) {
            return (IGui) holder;
        }
        return null;
    }

    @Nullable
    private InventoryHolder getHolder(Inventory inv) {
        try {
            return inv.getHolder();
        } catch (Throwable t) {
            return null;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (disabled) return;
        Player player = e.getPlayer();
        InventoryView view = player.getOpenInventory();
        IGui gui = getGuiHolder(view.getTopInventory());
        if (gui != null) {
            gui.onClose(view);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (disabled || !(event.getWhoClicked() instanceof Player)) return;
        InventoryView view = event.getView();
        IGui gui = getGuiHolder(view.getTopInventory());
        if (gui != null) {
            gui.onClick(
                    event.getAction(), event.getClick(),
                    event.getSlotType(), event.getRawSlot(),
                    event.getCurrentItem(), event.getCursor(),
                    view, event
            );
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (disabled || !(event.getWhoClicked() instanceof Player)) return;
        InventoryView view = event.getView();
        IGui gui = getGuiHolder(view.getTopInventory());
        if (gui != null) {
            gui.onDrag(view, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (disabled || !(event.getPlayer() instanceof Player)) return;
        InventoryView view = event.getView();
        IGui gui = getGuiHolder(view.getTopInventory());
        if (gui != null) {
            gui.onClose(view);
        }
    }

    public static GuiManager inst() {
        return instanceOf(GuiManager.class);
    }
}
