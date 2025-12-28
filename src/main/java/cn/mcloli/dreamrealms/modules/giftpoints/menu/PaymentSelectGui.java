package cn.mcloli.dreamrealms.modules.giftpoints.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.giftpoints.GiftPointsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PaymentSelectGui extends AbstractInteractiveGui<PaymentSelectMenuConfig> {

    private final Player targetPlayer;

    public PaymentSelectGui(Player player, PaymentSelectMenuConfig config, Player targetPlayer) {
        super(player, config);
        this.targetPlayer = targetPlayer;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        this.inventory = config.createInventory(this, player);
        config.applyIcons(this, inventory, player);
        return inventory;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        GiftPointsModule module = GiftPointsModule.inst();

        switch (key) {
            case 'W' -> {
                // 微信支付
                if (module.getModuleConfig().isEnableWechat()) {
                    new AmountSelectGui(player, module.getAmountSelectMenuConfig(), targetPlayer, "wechat").open();
                }
            }
            case 'A' -> {
                // 支付宝
                if (module.getModuleConfig().isEnableAlipay()) {
                    new AmountSelectGui(player, module.getAmountSelectMenuConfig(), targetPlayer, "alipay").open();
                }
            }
            case 'B' -> {
                // 返回
                new PlayerSelectGui(player, module.getPlayerSelectMenuConfig()).open();
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }
}
