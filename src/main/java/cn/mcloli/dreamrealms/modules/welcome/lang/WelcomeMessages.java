package cn.mcloli.dreamrealms.modules.welcome.lang;

import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "welcome.")
public enum WelcomeMessages implements IHolderAccessor {

    // 通用
    no_permission("&c你没有权限执行此命令"),
    module_disabled("&c欢迎模块已禁用"),

    // 帮助
    help__header("&6&l欢迎模块 &7- &f帮助"),
    help__status("&b/dr welcome status &7- &f查看状态"),
    help__reload("&b/dr welcome reload &7- &f重载配置"),
    help__delay("&b/dr welcome delay <分钟> &7- &f设置奖励延迟"),
    help__quit("&b/dr welcome quit <on|off> &7- &f新玩家退出是否发奖励"),

    // 状态
    status__header("&6&l欢迎模块状态"),
    status__reward_enabled("&7奖励: &f{enabled}"),
    status__reward_delay("&7奖励延迟: &f{delay} 分钟"),
    status__reward_on_quit("&7新玩家退出发奖励: &f{value}"),
    status__welcomer_balance("&7欢迎者奖励: &f{value}"),
    status__newplayer_balance("&7新玩家基础奖励: &f{value}"),
    status__active_sessions("&7活跃会话: &f{count}"),

    // 操作
    reload__success("&a配置已重载"),

    delay__usage("&c用法: &b/dr welcome delay <分钟>"),
    delay__invalid("&c无效的数值"),
    delay__success("&a奖励延迟已设置为: &f{delay} 分钟"),

    quit__usage("&c用法: &b/dr welcome quit <on|off>"),
    quit__invalid("&c无效的值，请使用 &bon &c或 &boff"),
    quit__on("&a已开启新玩家退出发奖励"),
    quit__off("&a已关闭新玩家退出发奖励"),

    // 欢迎相关
    already_welcomed("&c你已经欢迎过 &f{player} &c了"),
    welcome_success("&a你成功欢迎了 &f{player}"),
    not_new_player("&c该玩家不是新玩家"),

    // 奖励相关
    welcomer_reward("&a你因欢迎 &f{player} &a获得了 &e{balance} &a奖励"),
    newplayer_reward("&a有 &f{count} &a位玩家欢迎了你，你获得了 &e{balance} &a奖励"),

    ;

    WelcomeMessages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    WelcomeMessages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<WelcomeMessages> holder;

    @Override
    public LanguageEnumAutoHolder<WelcomeMessages> holder() {
        return holder;
    }

    /**
     * 注册语言枚举到 LanguageManager
     */
    public static Holder register() {
        LanguageManager.inst().register(WelcomeMessages.class, WelcomeMessages::holder);
        return new Holder();
    }

    public static class Holder {
        private Holder() {}
    }
}
