package cn.mcloli.dreamrealms.modules.itemmanager.lang;

import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

/**
 * 物品管理器语言枚举
 */
@Language(prefix = "itemmanager.")
public enum ItemManagerMessages implements IHolderAccessor {

    // 命令消息
    cmd__usage("&e用法: &b/dr itemmanager <add|give|get|delete|menu>"),
    cmd__add_success("&a成功添加物品: &f{identifier}"),
    cmd__add_fail_empty("&c请手持要添加的物品"),
    cmd__give_usage("&e用法: &b/dr itemmanager give <玩家> <标识名/GUID> [数量]"),
    cmd__give_success("&a已给予 &f{player} &ax{amount} &f{item}"),
    cmd__give_received("&a你收到了 x{amount} &f{item}"),
    cmd__give_fail_not_found("&c找不到物品: &f{identifier}"),
    cmd__give_fail_player("&c找不到玩家: &f{player}"),
    cmd__get_usage("&e用法: &b/dr itemmanager get <标识名/GUID> [数量]"),
    cmd__get_success("&a已获取 x{amount} &f{item}"),
    cmd__delete_usage("&e用法: &b/dr itemmanager delete <标识名/GUID>"),
    cmd__delete_success("&a已删除物品: &f{identifier}"),
    cmd__delete_fail_not_found("&c找不到物品: &f{identifier}"),

    // GUI 消息
    gui__category_edit("&e左键编辑"),
    gui__category_sort("&7Shift+左键前移 &7| &7Shift+右键后移"),
    gui__category_delete("&cCtrl+Q删除"),
    gui__item_edit("&e左键编辑"),
    gui__item_get("&a右键获取"),
    gui__item_delete("&cCtrl+Q删除"),
    gui__item_sort("&7Shift+左键前移 &7| &7Shift+右键后移"),

    // 编辑功能提示
    edit__name_wip("&e名称编辑功能开发中..."),
    edit__lore_wip("&eLore 编辑功能开发中..."),
    edit__enchant_wip("&e附魔编辑功能开发中..."),
    edit__attribute_wip("&e属性编辑功能开发中..."),
    edit__flag_wip("&eFlag 编辑功能开发中..."),
    edit__durability_wip("&e耐久编辑功能开发中..."),
    edit__current_name("<gray>当前名称: <white>{name}</white> <green><click:copy_to_clipboard:'{name}'><hover:show_text:'<gray>点击复制'>[复制]</hover></click> <yellow><click:suggest_command:'{name}'><hover:show_text:'<gray>点击输入到聊天框'>[输入]</hover></click>"),
    edit__name_success("&a物品名称已更新"),

    // Lore 编辑
    lore__current("<gray>第 {index} 行: <white>{content}</white> <green><click:copy_to_clipboard:'{content}'><hover:show_text:'<gray>点击复制'>[复制]</hover></click> <yellow><click:suggest_command:'{content}'><hover:show_text:'<gray>点击输入到聊天框'>[输入]</hover></click>"),
    lore__added("&a已添加新行"),
    lore__edited("&a已修改该行"),
    lore__deleted("&c已删除该行"),

    // 输入提示
    input__category_name("&e请输入分类名称:"),
    input__identifier("&e请输入物品标识名:"),
    input__category_icon("&e请输入分类图标 (材质名或 craftengine:xxx):"),
    input__item_name("&e请输入物品名称 (支持颜色代码):"),
    input__lore_add("&e请输入新的 Lore 行 (支持颜色代码):"),
    input__lore_edit("&e请输入新的内容 (支持颜色代码):"),

    // 操作反馈
    item__added("&a物品已添加到列表"),
    category__renamed("&a分类名称已更新"),
    category__icon_set("&a分类图标已设置"),

    // 聊天消息 (MiniMessage 格式)
    chat__copy_cmd("<aqua><click:copy_to_clipboard:{command}><hover:show_text:'<gray>点击复制'>点击复制获取命令</hover></click>"),
    chat__cmd_buttons("<gray>命令: <aqua>{command}</aqua> <green><click:copy_to_clipboard:{command}><hover:show_text:'<gray>点击复制到剪贴板'>[复制]</hover></click> <yellow><click:suggest_command:{command}><hover:show_text:'<gray>点击输入到聊天框'>[输入]</hover></click>"),

    ;

    ItemManagerMessages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    ItemManagerMessages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<ItemManagerMessages> holder;

    @Override
    public LanguageEnumAutoHolder<ItemManagerMessages> holder() {
        return holder;
    }

    /**
     * 注册语言枚举到 LanguageManager
     */
    public static Holder register() {
        LanguageManager.inst().register(ItemManagerMessages.class, ItemManagerMessages::holder);
        return new Holder();
    }

    public static class Holder {
        private Holder() {}
    }
}
