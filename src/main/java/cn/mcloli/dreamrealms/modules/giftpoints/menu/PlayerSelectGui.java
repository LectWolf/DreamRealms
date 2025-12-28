package cn.mcloli.dreamrealms.modules.giftpoints.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.giftpoints.GiftPointsModule;
import cn.mcloli.dreamrealms.utils.ItemBuilder;
import cn.mcloli.dreamrealms.utils.SkullUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class PlayerSelectGui extends AbstractInteractiveGui<PlayerSelectMenuConfig> {

    private final List<Player> onlinePlayers;
    private int page = 0;
    private int slotsPerPage = 0;

    public PlayerSelectGui(Player player, PlayerSelectMenuConfig config) {
        super(player, config);
        this.onlinePlayers = getOtherOnlinePlayers(player);
    }

    private List<Player> getOtherOnlinePlayers(Player exclude) {
        List<Player> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(exclude)) {
                players.add(p);
            }
        }
        return players;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = config.createInventory(this, player);
        slotsPerPage = countSlots('I');
        refreshInventory();
        return inventory;
    }

    private int countSlots(char key) {
        int count = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            Character k = config.getSlotKey(i);
            if (k != null && k == key) count++;
        }
        return count;
    }

    private void refreshInventory() {
        int totalPages = Math.max(1, (int) Math.ceil((double) onlinePlayers.size() / slotsPerPage));

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'I' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    inventory.setItem(i, getPlayerHead(index));
                }
                case '<' -> {
                    if (page > 0) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        // 使用 other-items 中的空图标，通过 applyIcon 处理
                        config.applyOtherIcon(inventory, player, i, "prev-empty");
                    }
                }
                case '>' -> {
                    if (page < totalPages - 1) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        config.applyOtherIcon(inventory, player, i, "next-empty");
                    }
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private ItemStack getPlayerHead(int index) {
        if (index >= onlinePlayers.size()) {
            return null;
        }

        Player target = onlinePlayers.get(index);
        ItemStack head = SkullUtil.createPlayerSkull(target.getName());
        ItemBuilder.setDisplayName(head, ColorHelper.parseColor("&f[ &c! &f] &e" + target.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7点击为其充值点券"));
        ItemBuilder.setLore(head, lore);
        return head;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        GiftPointsModule module = GiftPointsModule.inst();
        int totalPages = Math.max(1, (int) Math.ceil((double) onlinePlayers.size() / slotsPerPage));

        switch (key) {
            case 'I' -> {
                int playerIndex = index + page * slotsPerPage;
                if (playerIndex < onlinePlayers.size()) {
                    Player target = onlinePlayers.get(playerIndex);
                    new PaymentSelectGui(player, module.getPaymentSelectMenuConfig(), target).open();
                }
            }
            case '<' -> {
                if (page > 0) {
                    page--;
                    refreshInventory();
                }
            }
            case '>' -> {
                if (page < totalPages - 1) {
                    page++;
                    refreshInventory();
                }
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }
}
