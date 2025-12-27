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

    // 附魔编辑
    enchant__added("&a已添加附魔"),
    enchant__removed("&c已移除附魔"),
    enchant__level_increased("&a附魔等级已增加"),
    enchant__level_decreased("&c附魔等级已减少"),

    // 属性编辑
    attribute__added("&a已添加属性"),
    attribute__removed("&c已移除属性"),
    attribute__modified("&a属性数值已修改"),

    // Flag 编辑
    flag__added("&a已启用 Flag"),
    flag__removed("&c已禁用 Flag"),

    // 物品属性编辑
    properties__not_damageable("&c此物品没有耐久属性"),
    properties__damage_set("&a当前耐久已设置"),
    properties__max_damage_set("&a最大耐久已设置"),
    properties__max_damage_reset("&a最大耐久已重置为默认"),
    properties__max_stack_set("&a最大堆叠已设置"),
    properties__max_stack_reset("&a最大堆叠已重置为默认"),
    properties__unbreakable_toggled("&a无法破坏状态已切换"),
    properties__hide_tooltip_toggled("&a隐藏提示状态已切换"),
    properties__glint_toggled("&a附魔光效状态已切换"),
    properties__fire_resistant_toggled("&a防火状态已切换"),
    properties__custom_model_data_set("&a自定义模型数据已设置"),
    properties__custom_model_data_cleared("&a自定义模型数据已清除"),
    properties__item_name_set("&a物品名称已设置"),
    properties__item_name_cleared("&a物品名称已清除"),
    properties__item_model_set("&a物品模型已设置"),
    properties__item_model_cleared("&a物品模型已清除"),
    properties__enchantable_set("&a附魔可用性已设置"),
    properties__enchantable_cleared("&a附魔可用性已清除"),
    properties__rarity_toggled("&a稀有度已切换"),
    properties__rarity_cleared("&a稀有度已清除"),
    properties__wip("&7此功能开发中..."),
    properties__food_nutrition_set("&a营养值已设置"),
    properties__food_saturation_set("&a饱和度已设置"),
    properties__food_eat_time_set("&a食用时间已设置"),
    properties__food_can_always_eat_toggled("&a可以一直吃状态已切换"),
    properties__food_removed("&a食物组件已移除"),
    properties__food_converts_to_set("&a使用后转换物品已设置为: &f{item}"),
    properties__food_converts_to_cleared("&a使用后转换物品已清除"),
    properties__food_converts_to_hint("&e请手持物品后点击设置"),
    properties__food_effect_added("&a已添加食用效果"),
    properties__food_effect_removed("&c已移除食用效果"),
    properties__food_effect_modified("&a食用效果已修改"),
    properties__food_effect_add_failed("&c添加食用效果失败"),
    properties__material_changed("&a物品材质已更改为: &f{material}"),
    properties__material_hint("&e请手持物品后点击，或空手点击输入材质名"),
    properties__material_invalid("&c无效的材质名: &f{material}"),
    properties__need_paper("&c此功能需要 Paper 服务端"),
    properties__tool_speed_set("&a默认挖掘速度已设置"),
    properties__tool_damage_set("&a每方块耐久消耗已设置"),
    properties__tool_removed("&a工具组件已移除"),
    properties__tool_rule_removed("&c已移除挖掘规则"),
    properties__tool_rule_add_hint("&e添加规则功能开发中，请使用命令或NBT编辑器"),

    // 输入提示
    input__category_name("&e请输入分类名称:"),
    input__identifier("&e请输入物品标识名:"),
    input__category_icon("&e请输入分类图标 (材质名或 craftengine:xxx):"),
    input__item_name("&e请输入物品名称 (支持颜色代码):"),
    input__lore_add("&e请输入新的 Lore 行 (支持颜色代码):"),
    input__lore_edit("&e请输入新的内容 (支持颜色代码):"),
    input__attribute_value("&e请输入属性数值:"),
    input__damage_value("&e请输入当前损耗值:"),
    input__max_damage_value("&e请输入最大耐久值:"),
    input__max_stack_value("&e请输入最大堆叠数量 (1-99):"),
    input__mining_speed("&e请输入挖掘速度:"),
    input__invalid_number("&c无效的数字"),
    input__custom_model_data("&e请输入自定义模型数据 (整数):"),
    input__item_name_value("&e请输入物品名称 (支持颜色代码):"),
    input__item_model("&e请输入物品模型 (格式: namespace:key):"),
    input__enchantable("&e请输入附魔可用性数值 (正整数):"),
    input__nutrition("&e请输入营养值 (整数):"),
    input__saturation("&e请输入饱和度 (小数):"),
    input__eat_time("&e请输入食用时间 (秒):"),
    input__effect_duration("&e请输入效果持续时间 (秒):"),
    input__effect_amplifier("&e请输入效果等级 (1-255):"),
    input__effect_probability("&e请输入触发概率 (0-100):"),
    input__material("&e请输入材质名 (如 DIAMOND_SWORD):"),
    input__tool_speed("&e请输入默认挖掘速度 (小数):"),
    input__tool_damage("&e请输入每方块耐久消耗 (整数):"),

    // 操作反馈
    item__added("&a物品已添加到列表"),
    category__renamed("&a分类名称已更新"),
    category__icon_set("&a分类图标已设置"),
    category__not_empty("&c无法删除: 分类中还有 &f{count} &c个物品"),

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
