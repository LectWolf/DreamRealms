package cn.mcloli.dreamrealms.modules.timesync.lang;

import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "timesync.")
public enum TimeSyncMessages implements IHolderAccessor {

    // 通用
    no_permission("&c你没有权限执行此命令"),
    module_disabled("&c时间同步模块已禁用"),
    module_not_loaded("&c时间同步模块未加载"),

    // 帮助
    help__header("&6&l时间同步 &7- &f帮助"),
    help__status("&b/dr timesync status &7- &f查看状态"),
    help__toggle("&b/dr timesync toggle &7- &f重启同步任务"),
    help__timezone("&b/dr timesync timezone <时区> &7- &f设置时区"),
    help__mode("&b/dr timesync mode <whitelist|blacklist> &7- &f切换模式"),
    help__world("&b/dr timesync world <add|remove> <世界> &7- &f管理世界"),
    help__sleep("&b/dr timesync sleep <on|off> &7- &f禁止睡觉开关"),

    // 状态
    status__header("&6&l时间同步状态"),
    status__timezone("&7时区: &f{timezone}"),
    status__mode("&7模式: &f{mode}"),
    status__worlds("&7世界: &f{worlds}"),
    status__sleep("&7禁止睡觉: &f{sleep}"),

    // 操作
    toggle__success("&a已重启时间同步任务"),
    toggle__disabled("&c模块已在主配置中禁用，请修改 config.yml"),

    timezone__usage("&c用法: &b/dr timesync timezone <时区>"),
    timezone__invalid("&c无效的时区: &f{timezone}"),
    timezone__success("&a时区已设置为: &f{timezone}"),

    mode__usage("&c用法: &b/dr timesync mode <whitelist|blacklist>"),
    mode__invalid("&c无效的模式，请使用 &bwhitelist &c或 &bblacklist"),
    mode__whitelist("&a已切换为白名单模式"),
    mode__blacklist("&a已切换为黑名单模式"),

    world__usage("&c用法: &b/dr timesync world <add|remove> <世界名>"),
    world__invalid_action("&c无效的操作，请使用 &badd &c或 &bremove"),
    world__not_exist("&e警告: 世界 &f{world} &e当前不存在"),
    world__add_success("&a已添加世界: &f{world}"),
    world__add_exists("&c世界已在列表中: &f{world}"),
    world__remove_success("&a已移除世界: &f{world}"),
    world__remove_not_found("&c世界不在列表中: &f{world}"),

    sleep__usage("&c用法: &b/dr timesync sleep <on|off>"),
    sleep__invalid("&c无效的值，请使用 &bon &c或 &boff"),
    sleep__on("&a已开启禁止睡觉"),
    sleep__off("&a已关闭禁止睡觉"),

    ;

    TimeSyncMessages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    TimeSyncMessages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<TimeSyncMessages> holder;

    @Override
    public LanguageEnumAutoHolder<TimeSyncMessages> holder() {
        return holder;
    }

    /**
     * 注册语言枚举到 LanguageManager
     */
    public static Holder register() {
        LanguageManager.inst().register(TimeSyncMessages.class, TimeSyncMessages::holder);
        return new Holder();
    }

    public static class Holder {
        private Holder() {}
    }
}
