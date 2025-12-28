package cn.mcloli.dreamrealms.modules.giftpoints.command;

import cn.mcloli.dreamrealms.modules.giftpoints.GiftPointsModule;
import cn.mcloli.dreamrealms.modules.giftpoints.lang.GiftPointsMessages;
import cn.mcloli.dreamrealms.modules.giftpoints.menu.PlayerSelectGui;
import cn.mcloli.dreamrealms.modules.giftpoints.util.GiftPaymentUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GiftCommand implements CommandExecutor, TabCompleter {

    private final GiftPointsModule module;

    public GiftCommand(GiftPointsModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return GiftPointsMessages.player_only.t(sender);
        }

        if (!player.hasPermission("dreamrealms.giftpoints")) {
            return GiftPointsMessages.no_permission.t(sender);
        }

        if (!module.isModuleEnabled()) {
            return GiftPointsMessages.module_disabled.t(sender);
        }

        // /gift - 打开玩家选择菜单
        if (args.length == 0) {
            new PlayerSelectGui(player, module.getPlayerSelectMenuConfig()).open();
            return true;
        }

        // /gift <玩家> <wechat|alipay> <金额> - 直接赠送
        if (args.length == 3) {
            String targetName = args[0];
            String paymentType = args[1].toLowerCase();
            String amountStr = args[2];

            // 检查目标玩家
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                return GiftPointsMessages.player_not_found.t(player);
            }

            if (target.equals(player)) {
                return GiftPointsMessages.cannot_gift_self.t(player);
            }

            // 检查支付方式
            if (!paymentType.equals("wechat") && !paymentType.equals("alipay")) {
                return GiftPointsMessages.payment_invalid_type.t(player);
            }

            if (paymentType.equals("wechat") && !module.getModuleConfig().isEnableWechat()) {
                return GiftPointsMessages.payment_wechat_disabled.t(player);
            }

            if (paymentType.equals("alipay") && !module.getModuleConfig().isEnableAlipay()) {
                return GiftPointsMessages.payment_alipay_disabled.t(player);
            }

            // 检查金额
            Double amount = Util.parseDouble(amountStr).orElse(null);
            if (amount == null || amount < 0.01) {
                return GiftPointsMessages.payment_invalid_amount.t(player);
            }

            // 发起支付
            GiftPaymentUtil.startPayment(module, player, target, paymentType, amount);
            return true;
        }

        // 显示帮助
        GiftPointsMessages.help__header.t(player);
        GiftPointsMessages.help__gift.t(player);
        GiftPointsMessages.help__gift_player.t(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            // 补全在线玩家名（排除自己）
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player)) {
                    names.add(p.getName());
                }
            }
            return startsWith(names, args[0]);
        }

        if (args.length == 2) {
            // 补全支付方式
            List<String> types = new ArrayList<>();
            if (module.getModuleConfig().isEnableWechat()) {
                types.add("wechat");
            }
            if (module.getModuleConfig().isEnableAlipay()) {
                types.add("alipay");
            }
            return startsWith(types, args[1]);
        }

        if (args.length == 3) {
            // 补全金额选项
            List<String> amounts = new ArrayList<>();
            for (Double amount : module.getModuleConfig().getAmountOptions()) {
                amounts.add(String.valueOf(amount.intValue()));
            }
            return startsWith(amounts, args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> startsWith(List<String> list, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }
}
