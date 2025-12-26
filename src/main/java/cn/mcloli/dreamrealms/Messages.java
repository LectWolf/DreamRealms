package cn.mcloli.dreamrealms;

import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

/**
 * 主消息枚举
 */
@Language(prefix = "")
public enum Messages implements IHolderAccessor {

    // 通用消息
    no_permission("&c你没有权限执行此命令"),
    reload_success("&a配置文件已重载"),

    // 帮助信息
    help__header("&6&lDreamRealms &7- &f帮助"),
    help__reload("&b/dr reload &7- &f重载配置"),

    ;

    Messages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    Messages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<Messages> holder;

    @Override
    public LanguageEnumAutoHolder<Messages> holder() {
        return holder;
    }
}
