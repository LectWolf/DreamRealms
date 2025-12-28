package cn.mcloli.dreamrealms.modules.giftpoints.lang;

import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "giftpoints.")
public enum GiftPointsMessages implements IHolderAccessor {

    module_disabled("&c模块未启用"),
    player_only("&c该命令只能由玩家执行"),
    no_permission("&c你没有权限执行此命令"),

    // 命令相关
    help__header("&7========== &b点券赠送帮助 &7=========="),
    help__gift("&e/gift &7- 打开赠送菜单"),
    help__gift_player("&e/gift <玩家> <wechat|alipay> <金额> &7- 直接赠送"),

    // 支付相关
    payment_not_connected("&c支付系统未连接，请稍后再试"),
    payment_processing("&c你已有一个支付正在处理中"),
    payment_invalid_type("&c无效的支付方式"),
    payment_invalid_amount("&c无效的金额"),
    payment_wechat_disabled("&c微信支付未启用"),
    payment_alipay_disabled("&c支付宝支付未启用"),
    payment_send("&a正在创建订单..."),
    payment_sent("&a订单创建成功！订单号: &e%order_id%\n&a金额: &e￥%money%\n&a赠送点券: &e%points%\n&a请在 &e%timeout% &a秒内完成支付"),
    payment_error("&c订单创建失败: %error%"),
    payment_success_sender("&a支付成功！已赠送 &e%points% &a点券给 &b%target%"),
    payment_success_receiver("&a玩家 &b%sender% &a赠送给你了 &e%points% &a点券！"),

    // 玩家选择
    player_not_found("&c找不到该玩家"),
    player_offline("&c该玩家不在线"),
    cannot_gift_self("&c不能赠送给自己"),

    // GUI 相关
    no_online_players("&c当前没有其他在线玩家");

    GiftPointsMessages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    GiftPointsMessages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<GiftPointsMessages> holder;

    @Override
    public LanguageEnumAutoHolder<GiftPointsMessages> holder() {
        return holder;
    }

    public static Holder register() {
        LanguageManager.inst().register(GiftPointsMessages.class, GiftPointsMessages::holder);
        return new Holder();
    }

    public static class Holder {
        private Holder() {}
    }
}
