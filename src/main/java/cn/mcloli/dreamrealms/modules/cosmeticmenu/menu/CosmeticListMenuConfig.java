package cn.mcloli.dreamrealms.modules.cosmeticmenu.menu;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.gui.AbstractMenuConfig;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.CosmeticMenuModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 时装列表菜单配置
 */
public class CosmeticListMenuConfig extends AbstractMenuConfig<CosmeticListGui> {

    private Icon cosmeticIcon;
    private Icon cosmeticEquippedIcon;
    private Icon unequipIcon;
    private Icon unequipEmptyIcon;
    private Icon prevIcon;
    private Icon nextIcon;
    private Icon emptyPrevIcon;
    private Icon emptyNextIcon;
    
    // 分类图标
    private Icon helmetIcon;
    private Icon helmetSelectedIcon;
    private Icon chestplateIcon;
    private Icon chestplateSelectedIcon;
    private Icon leggingsIcon;
    private Icon leggingsSelectedIcon;
    private Icon bootsIcon;
    private Icon bootsSelectedIcon;
    private Icon backpackIcon;
    private Icon backpackSelectedIcon;
    private Icon offhandIcon;
    private Icon offhandSelectedIcon;
    private Icon balloonIcon;
    private Icon balloonSelectedIcon;

    public CosmeticListMenuConfig(DreamRealms plugin, CosmeticMenuModule module) {
        super(plugin, module.getModuleMenuPath() + "/cosmetic_list.yml");
        markInteractive('I'); // 时装物品槽位
    }

    @Override
    protected void clearMainIcons() {
        cosmeticIcon = null;
        cosmeticEquippedIcon = null;
        unequipIcon = null;
        unequipEmptyIcon = null;
        prevIcon = null;
        nextIcon = null;
        emptyPrevIcon = null;
        emptyNextIcon = null;
        helmetIcon = null;
        helmetSelectedIcon = null;
        chestplateIcon = null;
        chestplateSelectedIcon = null;
        leggingsIcon = null;
        leggingsSelectedIcon = null;
        bootsIcon = null;
        bootsSelectedIcon = null;
        backpackIcon = null;
        backpackSelectedIcon = null;
        offhandIcon = null;
        offhandSelectedIcon = null;
        balloonIcon = null;
        balloonSelectedIcon = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon icon) {
        switch (key) {
            case "I" -> { cosmeticIcon = icon; return true; }
            case "I_equipped" -> { cosmeticEquippedIcon = icon; return true; }
            case "P" -> { unequipIcon = icon; return true; }
            case "P_empty" -> { unequipEmptyIcon = icon; return true; }
            case "<" -> { prevIcon = icon; return true; }
            case ">" -> { nextIcon = icon; return true; }
            case "<_empty" -> { emptyPrevIcon = icon; return true; }
            case ">_empty" -> { emptyNextIcon = icon; return true; }
            case "A" -> { helmetIcon = icon; return true; }
            case "A_selected" -> { helmetSelectedIcon = icon; return true; }
            case "B" -> { chestplateIcon = icon; return true; }
            case "B_selected" -> { chestplateSelectedIcon = icon; return true; }
            case "C" -> { leggingsIcon = icon; return true; }
            case "C_selected" -> { leggingsSelectedIcon = icon; return true; }
            case "D" -> { bootsIcon = icon; return true; }
            case "D_selected" -> { bootsSelectedIcon = icon; return true; }
            case "E" -> { backpackIcon = icon; return true; }
            case "E_selected" -> { backpackSelectedIcon = icon; return true; }
            case "F" -> { offhandIcon = icon; return true; }
            case "F_selected" -> { offhandSelectedIcon = icon; return true; }
            case "G" -> { balloonIcon = icon; return true; }
            case "G_selected" -> { balloonSelectedIcon = icon; return true; }
        }
        return false;
    }

    @Override
    protected ItemStack tryApplyMainIcon(CosmeticListGui gui, String key, Player player, int iconIndex) {
        return switch (key) {
            case "P" -> unequipIcon != null ? unequipIcon.generateIcon(player) : null;
            case "<" -> prevIcon != null ? prevIcon.generateIcon(player) : null;
            case ">" -> nextIcon != null ? nextIcon.generateIcon(player) : null;
            case "A" -> helmetIcon != null ? helmetIcon.generateIcon(player) : null;
            case "B" -> chestplateIcon != null ? chestplateIcon.generateIcon(player) : null;
            case "C" -> leggingsIcon != null ? leggingsIcon.generateIcon(player) : null;
            case "D" -> bootsIcon != null ? bootsIcon.generateIcon(player) : null;
            case "E" -> backpackIcon != null ? backpackIcon.generateIcon(player) : null;
            case "F" -> offhandIcon != null ? offhandIcon.generateIcon(player) : null;
            default -> null;
        };
    }

    public Icon getCosmeticIcon() {
        return cosmeticIcon;
    }

    public Icon getCosmeticEquippedIcon() {
        return cosmeticEquippedIcon;
    }

    public Icon getUnequipIcon() {
        return unequipIcon;
    }

    public ItemStack getUnequipEmptyIcon(Player player) {
        return unequipEmptyIcon != null ? unequipEmptyIcon.generateIcon(player) : null;
    }

    public ItemStack getEmptyPrevIcon(Player player) {
        return emptyPrevIcon != null ? emptyPrevIcon.generateIcon(player) : null;
    }

    public ItemStack getEmptyNextIcon(Player player) {
        return emptyNextIcon != null ? emptyNextIcon.generateIcon(player) : null;
    }

    public Icon getHelmetIcon() { return helmetIcon; }
    public Icon getHelmetSelectedIcon() { return helmetSelectedIcon; }
    public Icon getChestplateIcon() { return chestplateIcon; }
    public Icon getChestplateSelectedIcon() { return chestplateSelectedIcon; }
    public Icon getLeggingsIcon() { return leggingsIcon; }
    public Icon getLeggingsSelectedIcon() { return leggingsSelectedIcon; }
    public Icon getBootsIcon() { return bootsIcon; }
    public Icon getBootsSelectedIcon() { return bootsSelectedIcon; }
    public Icon getBackpackIcon() { return backpackIcon; }
    public Icon getBackpackSelectedIcon() { return backpackSelectedIcon; }
    public Icon getOffhandIcon() { return offhandIcon; }
    public Icon getOffhandSelectedIcon() { return offhandSelectedIcon; }
    public Icon getBalloonIcon() { return balloonIcon; }
    public Icon getBalloonSelectedIcon() { return balloonSelectedIcon; }
}
