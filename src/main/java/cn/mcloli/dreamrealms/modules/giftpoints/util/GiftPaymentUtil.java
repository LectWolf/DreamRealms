package cn.mcloli.dreamrealms.modules.giftpoints.util;

import cn.mcloli.dreamrealms.modules.giftpoints.GiftPointsModule;
import cn.mcloli.dreamrealms.modules.giftpoints.lang.GiftPointsMessages;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.qrcode.QRCode;
import top.mrxiaom.qrcode.enums.ErrorCorrectionLevel;
import top.mrxiaom.sweet.checkout.PluginCommon;
import top.mrxiaom.sweet.checkout.func.PaymentAPI;
import top.mrxiaom.sweet.checkout.func.PaymentsAndQRCodeManager;
import top.mrxiaom.sweet.checkout.map.MapQRCode;
import top.mrxiaom.sweet.checkout.packets.plugin.PacketPluginRequestOrder;

import java.time.LocalDateTime;
import java.util.UUID;

public class GiftPaymentUtil {

    /**
     * 发起赠送支付
     */
    public static void startPayment(GiftPointsModule module, Player sender, Player target, String paymentType, double amount) {
        // 检查支付系统连接
        if (!PaymentAPI.inst().isConnected()) {
            GiftPointsMessages.payment_not_connected.t(sender);
            return;
        }

        PaymentsAndQRCodeManager manager = PaymentsAndQRCodeManager.inst();

        // 检查是否已有支付在处理
        if (manager.isProcess(sender)) {
            GiftPointsMessages.payment_processing.t(sender);
            return;
        }

        int points = module.getModuleConfig().calculatePoints(amount);
        String moneyStr = String.format("%.2f", amount);
        String productName = "赠送点券给 " + target.getName();

        // 保存目标玩家 UUID，以便支付成功后使用
        UUID targetUUID = target.getUniqueId();
        String targetName = target.getName();

        // 标记正在处理
        manager.putProcess(sender, "giftpoints:" + targetName + ":" + moneyStr);

        GiftPointsMessages.payment_send.t(sender);

        // 发送支付请求
        PaymentAPI.inst().send(new PacketPluginRequestOrder(
                sender.getName(), paymentType, productName, moneyStr, false
        ), resp -> {
            String error = resp.getError();
            if (!error.isEmpty()) {
                module.warn("玩家 " + sender.getName() + " 赠送点券给 " + targetName + " 失败: " + error);
                GiftPointsMessages.payment_error.t(sender, Pair.of("%error%", error));
                manager.remove(sender);
                return;
            }

            // 订单创建成功
            String orderId = resp.getOrderId();
            String realMoney = resp.getMoney();
            int timeout = module.getModuleConfig().getPaymentTimeout();
            long outdateTime = System.currentTimeMillis() + (timeout * 1000L) + 500L;

            module.debug("玩家 " + sender.getName() + " 赠送点券给 " + targetName + " 订单创建成功: " + orderId);

            GiftPointsMessages.payment_sent.t(sender,
                    Pair.of("%order_id%", orderId),
                    Pair.of("%money%", realMoney),
                    Pair.of("%points%", points),
                    Pair.of("%timeout%", timeout));

            // 生成二维码并显示
            QRCode qrCode = QRCode.create(resp.getPaymentUrl(), ErrorCorrectionLevel.M);
            MapQRCode mapSource = new MapQRCode(qrCode);
            manager.requireScan(sender, mapSource, orderId, outdateTime, money -> {
                // 支付成功，记录交易日志到 SweetCheckout
                logToSweetCheckout(sender, paymentType, realMoney, "giftpoints:" + targetName + ":" + points);
                // 给予目标玩家点券
                onPaymentSuccess(module, sender.getName(), targetUUID, targetName, points);
            });
        });
    }

    /**
     * 记录交易日志到 SweetCheckout
     */
    private static void logToSweetCheckout(Player player, String type, String money, String reason) {
        try {
            PluginCommon sweetCheckout = PluginCommon.getInstance();
            if (sweetCheckout != null && sweetCheckout.getTradeDatabase() != null) {
                sweetCheckout.getTradeDatabase().log(player, LocalDateTime.now(), type, money, reason);
            }
        } catch (Exception e) {
            // 忽略日志记录失败
        }
    }

    /**
     * 支付成功回调
     */
    private static void onPaymentSuccess(GiftPointsModule module, String senderName, UUID targetUUID, String targetName, int points) {
        // 获取 PlayerPoints API
        PlayerPoints playerPoints = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (playerPoints == null) {
            module.warn("PlayerPoints 插件未找到，无法赠送点券");
            return;
        }

        PlayerPointsAPI api = playerPoints.getAPI();

        // 给目标玩家点券 (使用 UUID，即使玩家离线也能给)
        api.give(targetUUID, points);

        module.info("玩家 " + senderName + " 赠送了 " + points + " 点券给 " + targetName);

        // 通知发送者（如果在线）
        Player sender = Bukkit.getPlayerExact(senderName);
        if (sender != null && sender.isOnline()) {
            GiftPointsMessages.payment_success_sender.t(sender,
                    Pair.of("%points%", points),
                    Pair.of("%target%", targetName));
        }

        // 通知接收者（如果在线）
        Player onlineTarget = Bukkit.getPlayer(targetUUID);
        if (onlineTarget != null && onlineTarget.isOnline()) {
            GiftPointsMessages.payment_success_receiver.t(onlineTarget,
                    Pair.of("%points%", points),
                    Pair.of("%sender%", senderName));
        }
    }
}
