package cn.mcloli.dreamrealms.modules.wank.lang;

import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "wank.")
public enum WankMessages implements IHolderAccessor {

    player_only("&c该命令只能由玩家执行"),
    module_disabled("&c模块未启用"),

    prompt("<click:run_command:/wank><hover:show_text:'<#fd6262>点击,开导!'><#766aba>夜深人静,要不要来一发?"),
    cooldown("&c手冲虽好,但不要贪冲哦!"),
    recovering("<#766aba>你的牛牛正在疗养中..."),
    cut_disabled("<#766aba>你的牛牛被剪掉了,无法导管..."),
    start("<#766aba>开导!"),
    finish("<#766aba>你射精了!你这次坚持了 &f%duration% <#766aba>秒"),
    explode("&c你一天手冲超过了 %max_times% 次! 你的牛牛无法忍受,爆炸了!"),
    explode_broadcast("%player%手冲太多爆炸了"),

    scissors_cut_success("&a你剪掉了 &b%target% &a的牛牛!"),
    scissors_cut_victim("&c你的牛牛被 &b%player% &c剪掉了! 今天无法导管了..."),
    scissors_already_cut("&c%target% 的牛牛已经被剪掉了"),
    scissors_progress("&c你正在攻击 &b%target% &c的牛牛 &7| &f%current%/%required%"),
    scissors_victim_progress("&c你的牛牛正在被攻击 &7| &f%current%/%required%"),

    admin_reset("&a已重置所有玩家的导管次数"),
    ;

    WankMessages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    WankMessages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<WankMessages> holder;

    @Override
    public LanguageEnumAutoHolder<WankMessages> holder() {
        return holder;
    }

    public static Holder register() {
        LanguageManager.inst().register(WankMessages.class, WankMessages::holder);
        return new Holder();
    }

    public static class Holder {
        private Holder() {}
    }
}
