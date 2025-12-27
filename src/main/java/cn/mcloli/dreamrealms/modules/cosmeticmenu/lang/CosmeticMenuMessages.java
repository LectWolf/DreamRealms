package cn.mcloli.dreamrealms.modules.cosmeticmenu.lang;

import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "cosmeticmenu.")
public enum CosmeticMenuMessages implements IHolderAccessor {

    // GUI 消息
    gui__slot_helmet("&e头部时装"),
    gui__slot_chestplate("&e胸部时装"),
    gui__slot_leggings("&e腿部时装"),
    gui__slot_boots("&e脚部时装"),
    gui__slot_backpack("&e背包时装"),
    gui__slot_offhand("&e副手时装"),
    gui__slot_balloon("&e气球时装"),
    
    gui__cosmetic_click_equip("&7左键 &f装备"),
    gui__cosmetic_click_unequip("&7左键 &f卸下"),
    gui__cosmetic_equipped("&a已装备"),
    gui__cosmetic_count("&7拥有数量: &f%count%"),
    
    // 操作消息
    action__equipped("&a已装备时装: &f%name%"),
    action__unequipped("&a已卸下时装"),
    ;

    CosmeticMenuMessages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    CosmeticMenuMessages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<CosmeticMenuMessages> holder;

    @Override
    public LanguageEnumAutoHolder<CosmeticMenuMessages> holder() {
        return holder;
    }

    public static Holder register() {
        LanguageManager.inst().register(CosmeticMenuMessages.class, CosmeticMenuMessages::holder);
        return new Holder();
    }

    public static class Holder {
        private Holder() {}
    }
}
