package cn.mcloli.dreamrealms.modules.giftpoints.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.giftpoints.GiftPointsModule;
import cn.mcloli.dreamrealms.modules.giftpoints.util.GiftPaymentUtil;
import cn.mcloli.dreamrealms.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class AmountSelectGui extends AbstractInteractiveGui<AmountSelectMenuConfig> {

    private final Player targetPlayer;
    private final String paymentType;

    public AmountSelectGui(Player player, AmountSelectMenuConfig config, Player targetPlayer, String paymentType) {
        super(player, config);
        this.targetPlayer = targetPlayer;
        this.paymentType = paymentType;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public String getPaymentType() {
        return paymentType;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = config.createInventory(this, player);
        refreshInventory();
        return inventory;
    }

    private void refreshInventory() {
        GiftPointsModule module = GiftPointsModule.inst();
        List<Double> amounts = module.getModuleConfig().getAmountOptions();
        int pointsScale = module.getModuleConfig().getPointsScale();

        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            switch (key) {
                case 'I' -> {
                    int index = config.getKeyIndex(key, i);
                    inventory.setItem(i, getAmountItem(index, amounts, pointsScale));
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private ItemStack getAmountItem(int index, List<Double> amounts, int pointsScale) {
        if (index >= amounts.size()) {
            return null;
        }

        double amount = amounts.get(index);
        int points = (int) Math.round(amount * pointsScale);

        ItemStack item = ItemBuilder.parseItem("GOLD_INGOT");
        String amountStr = amount == Math.floor(amount) ? String.valueOf((int) amount) : String.format("%.2f", amount);
        ItemBuilder.setDisplayName(item, ColorHelper.parseColor("&e￥" + amountStr));

        List<String> lore = new ArrayList<>();
        lore.add(ColorHelper.parseColor("&7充值 &e￥" + amountStr));
        lore.add(ColorHelper.parseColor("&7赠送 &b" + points + " &7点券给 &e" + targetPlayer.getName()));
        lore.add("");
        lore.add(ColorHelper.parseColor("&a点击确认支付"));
        ItemBuilder.setLore(item, lore);

        return item;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        GiftPointsModule module = GiftPointsModule.inst();

        switch (key) {
            case 'I' -> {
                List<Double> amounts = module.getModuleConfig().getAmountOptions();
                if (index < amounts.size()) {
                    double amount = amounts.get(index);
                    player.closeInventory();
                    GiftPaymentUtil.startPayment(module, player, targetPlayer, paymentType, amount);
                }
            }
            case 'B' -> {
                // 返回
                new PaymentSelectGui(player, module.getPaymentSelectMenuConfig(), targetPlayer).open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }
}
