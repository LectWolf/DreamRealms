package cn.mcloli.dreamrealms.modules.ownerbind.lang;

import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "ownerbind.")
public enum OwnerBindMessages implements IHolderAccessor {

    // 通用
    no_permission("&c你没有权限执行此命令"),
    module_disabled("&c物主绑定模块已禁用"),
    player_only("&c该命令只能由玩家执行"),
    reload_success("&a配置已重载"),

    // 帮助
    help__header("&6&l物主绑定 &7- &f帮助"),
    help__mark("&b/dr ob mark &7- &f标记手持物品为可绑定"),
    help__bind("&b/dr ob bind [玩家] &7- &f绑定手持物品"),
    help__unbind("&b/dr ob unbind &7- &f解除手持物品绑定"),
    help__info("&b/dr ob info &7- &f查看手持物品绑定信息"),
    help__reload("&b/dr ob reload &7- &f重载配置"),

    // 操作
    hand_empty("&c请手持需要操作的物品"),
    mark_success("&a物品已标记为可绑定"),
    mark_failed("&c标记失败"),
    already_marked("&c物品已经是可绑定状态"),
    bind_success("&a物品已绑定给 &e%player%"),
    bind_failed("&c绑定失败"),
    already_bound("&c物品已绑定给 &e%owner%"),
    unbind_success("&a已解除物品绑定"),
    unbind_failed("&c解绑失败"),
    not_bound("&c该物品未绑定"),

    // 查询
    info__header("&6&l物品绑定信息"),
    info__bound_owner("&7绑定玩家: &e%owner%"),
    info__bindable("&7状态: &a可绑定"),
    info__bindable_nbt("&7  - NBT 标记"),
    info__bindable_lore("&7  - Lore 标记"),
    info__not_bindable("&7状态: &c不可绑定"),

    // 绑定相关
    not_owner("&c这个物品属于 &e%owner%&c，你无法使用"),
    anti_drop_tip("&c绑定物品无法丢弃"),
    anti_market_tip("&c绑定物品无法上架市场"),
    
    // 邮件归还
    mail_send_success("&a物品已通过邮件归还给 &e%owner%"),
    mail_send_failed("&c邮件发送失败，物品已丢弃"),

    ;

    OwnerBindMessages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    OwnerBindMessages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<OwnerBindMessages> holder;

    @Override
    public LanguageEnumAutoHolder<OwnerBindMessages> holder() {
        return holder;
    }

    public static Holder register() {
        LanguageManager.inst().register(OwnerBindMessages.class, OwnerBindMessages::holder);
        return new Holder();
    }

    public static class Holder {
        private Holder() {}
    }
}
