package cn.mcloli.dreamrealms.modules.cosmeticmenu.menu;

import cn.mcloli.dreamrealms.gui.AbstractInteractiveGui;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.CosmeticMenuModule;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.lang.CosmeticMenuMessages;
import cn.mcloli.dreamrealms.modules.cosmeticmenu.util.CosmeticUtil;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * 时装列表 GUI - 显示玩家拥有的时装，底部有分类切换
 */
public class CosmeticListGui extends AbstractInteractiveGui<CosmeticListMenuConfig> {

    private final CosmeticMenuModule module;
    private CosmeticSlot currentSlot;
    private List<Cosmetic> cosmetics;
    private int page = 0;
    private int slotsPerPage = 0;

    // 槽位顺序: A=头部, B=胸部, C=腿部, D=脚部, E=背包, F=副手, G=气球
    private static final String[] SLOT_NAMES = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "BACKPACK", "OFFHAND", "BALLOON"};

    public CosmeticListGui(Player player, CosmeticListMenuConfig config) {
        this(player, config, CosmeticSlot.valueOf("HELMET"));
    }

    public CosmeticListGui(Player player, CosmeticListMenuConfig config, CosmeticSlot slot) {
        super(player, config);
        this.module = CosmeticMenuModule.inst();
        this.currentSlot = slot;
    }

    @Override
    @NotNull
    public Inventory newInventory() {
        // 获取玩家拥有的该槽位时装
        cosmetics = CosmeticUtil.getPlayerCosmeticsForSlot(player, currentSlot);

        // 创建 inventory
        this.inventory = config.createInventory(this, player);

        // 计算每页槽位数
        slotsPerPage = countSlots('I');

        refreshInventory();
        return inventory;
    }

    private int countSlots(char key) {
        int count = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            Character k = config.getSlotKey(i);
            if (k != null && k == key) count++;
        }
        return count;
    }

    private void refreshInventory() {
        for (int i = 0; i < inventory.getSize(); i++) {
            Character key = config.getSlotKey(i);
            if (key == null) continue;

            String keyStr = String.valueOf(key);
            if (keyStr.equals(" ") || keyStr.equals("　")) {
                inventory.setItem(i, null);
                continue;
            }

            switch (key) {
                case 'I' -> {
                    int index = config.getKeyIndex(key, i) + page * slotsPerPage;
                    inventory.setItem(i, getCosmeticItem(index));
                }
                case 'P' -> {
                    // 卸下当前分类时装 - 根据是否有装备显示不同图标
                    if (CosmeticUtil.hasEquippedInSlot(player, currentSlot)) {
                        inventory.setItem(i, config.getUnequipIcon() != null ? config.getUnequipIcon().generateIcon(player) : null);
                    } else {
                        inventory.setItem(i, config.getUnequipEmptyIcon(player));
                    }
                }
                case '<' -> {
                    if (page > 0) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        inventory.setItem(i, config.getEmptyPrevIcon(player));
                    }
                }
                case '>' -> {
                    if (hasNextPage()) {
                        config.applyIcon(this, inventory, player, i);
                    } else {
                        inventory.setItem(i, config.getEmptyNextIcon(player));
                    }
                }
                case 'A', 'B', 'C', 'D', 'E', 'F', 'G' -> {
                    // 分类按钮
                    inventory.setItem(i, getCategoryIcon(key));
                }
                default -> config.applyIcon(this, inventory, player, i);
            }
        }
    }

    private boolean hasNextPage() {
        return (page + 1) * slotsPerPage < cosmetics.size();
    }

    private ItemStack getCosmeticItem(int index) {
        if (index >= cosmetics.size()) {
            return null;
        }

        Cosmetic cosmetic = cosmetics.get(index);
        
        // 获取时装的物品图标
        ItemStack item = CosmeticUtil.getCosmeticDisplayItem(cosmetic);
        if (item == null) return null;

        // 检查是否已装备
        boolean equipped = CosmeticUtil.isEquipped(player, cosmetic);

        // 如果已装备，添加附魔效果
        if (equipped) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }

        // 设置 Lore
        List<String> lore = new ArrayList<>();
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            lore.addAll(item.getItemMeta().getLore());
        }
        lore.add("");
        if (equipped) {
            lore.add(ColorHelper.parseColor(CosmeticMenuMessages.gui__cosmetic_equipped.str()));
            lore.add(ColorHelper.parseColor(CosmeticMenuMessages.gui__cosmetic_click_unequip.str()));
        } else {
            lore.add(ColorHelper.parseColor(CosmeticMenuMessages.gui__cosmetic_click_equip.str()));
        }
        ItemStackUtil.setItemLore(item, lore);

        return item;
    }

    private ItemStack getCategoryIcon(char key) {
        int slotIndex = key - 'A';
        if (slotIndex < 0 || slotIndex >= SLOT_NAMES.length) return null;

        String slotName = SLOT_NAMES[slotIndex];
        CosmeticSlot slot = CosmeticSlot.valueOf(slotName);
        boolean isCurrentSlot = slot == currentSlot;

        // 获取该分类的时装数量
        int count = CosmeticUtil.getPlayerCosmeticsForSlot(player, slot).size();
        // 获取总时装数量
        int total = CosmeticUtil.getTotalCosmeticsCount(player);

        // 获取对应的图标，根据是否为当前分类选择不同图标
        ItemStack icon = switch (key) {
            case 'A' -> {
                var iconConfig = isCurrentSlot ? config.getHelmetSelectedIcon() : config.getHelmetIcon();
                yield iconConfig != null ? iconConfig.generateIcon(player,
                        Pair.of("%count%", String.valueOf(count)),
                        Pair.of("%total%", String.valueOf(total))) : null;
            }
            case 'B' -> {
                var iconConfig = isCurrentSlot ? config.getChestplateSelectedIcon() : config.getChestplateIcon();
                yield iconConfig != null ? iconConfig.generateIcon(player,
                        Pair.of("%count%", String.valueOf(count)),
                        Pair.of("%total%", String.valueOf(total))) : null;
            }
            case 'C' -> {
                var iconConfig = isCurrentSlot ? config.getLeggingsSelectedIcon() : config.getLeggingsIcon();
                yield iconConfig != null ? iconConfig.generateIcon(player,
                        Pair.of("%count%", String.valueOf(count)),
                        Pair.of("%total%", String.valueOf(total))) : null;
            }
            case 'D' -> {
                var iconConfig = isCurrentSlot ? config.getBootsSelectedIcon() : config.getBootsIcon();
                yield iconConfig != null ? iconConfig.generateIcon(player,
                        Pair.of("%count%", String.valueOf(count)),
                        Pair.of("%total%", String.valueOf(total))) : null;
            }
            case 'E' -> {
                var iconConfig = isCurrentSlot ? config.getBackpackSelectedIcon() : config.getBackpackIcon();
                yield iconConfig != null ? iconConfig.generateIcon(player,
                        Pair.of("%count%", String.valueOf(count)),
                        Pair.of("%total%", String.valueOf(total))) : null;
            }
            case 'F' -> {
                var iconConfig = isCurrentSlot ? config.getOffhandSelectedIcon() : config.getOffhandIcon();
                yield iconConfig != null ? iconConfig.generateIcon(player,
                        Pair.of("%count%", String.valueOf(count)),
                        Pair.of("%total%", String.valueOf(total))) : null;
            }
            case 'G' -> {
                var iconConfig = isCurrentSlot ? config.getBalloonSelectedIcon() : config.getBalloonIcon();
                yield iconConfig != null ? iconConfig.generateIcon(player,
                        Pair.of("%count%", String.valueOf(count)),
                        Pair.of("%total%", String.valueOf(total))) : null;
            }
            default -> null;
        };

        return icon;
    }

    @Override
    protected void handleClick(ClickType click, char key, int index, ItemStack currentItem, InventoryClickEvent event) {
        module.debug("CosmeticListGui click: key=" + key + ", index=" + index + ", click=" + click);

        switch (key) {
            case 'I' -> handleCosmeticClick(click, index + page * slotsPerPage);
            case 'P' -> handleUnequip();
            case '<' -> {
                if (page > 0) {
                    page--;
                    refreshInventory();
                }
            }
            case '>' -> {
                if (hasNextPage()) {
                    page++;
                    refreshInventory();
                }
            }
            case 'A', 'B', 'C', 'D', 'E', 'F', 'G' -> {
                // 切换分类
                int slotIndex = key - 'A';
                if (slotIndex >= 0 && slotIndex < SLOT_NAMES.length) {
                    switchSlot(SLOT_NAMES[slotIndex]);
                }
            }
            default -> config.handleOtherIconClick(player, click, key);
        }
    }

    private void handleCosmeticClick(ClickType click, int index) {
        if (index >= cosmetics.size()) return;

        Cosmetic cosmetic = cosmetics.get(index);
        CosmeticUser user = HMCCosmeticsAPI.getUser(player.getUniqueId());
        if (user == null) return;

        if (click == ClickType.LEFT) {
            if (CosmeticUtil.isEquipped(player, cosmetic)) {
                // 已装备，卸下
                HMCCosmeticsAPI.unequipCosmetic(user, currentSlot);
                CosmeticMenuMessages.action__unequipped.t(player);
            } else {
                // 未装备，装备
                HMCCosmeticsAPI.equipCosmetic(user, cosmetic);
                CosmeticMenuMessages.action__equipped.t(player, Pair.of("%name%", CosmeticUtil.getCosmeticName(cosmetic)));
            }
            refreshInventory();
        }
    }

    private void handleUnequip() {
        CosmeticUser user = HMCCosmeticsAPI.getUser(player.getUniqueId());
        if (user == null) return;

        HMCCosmeticsAPI.unequipCosmetic(user, currentSlot);
        CosmeticMenuMessages.action__unequipped.t(player);
        refreshInventory();
    }

    private void switchSlot(String slotName) {
        CosmeticSlot newSlot = CosmeticSlot.valueOf(slotName);
        if (newSlot == currentSlot) return;

        currentSlot = newSlot;
        page = 0;
        cosmetics = CosmeticUtil.getPlayerCosmeticsForSlot(player, currentSlot);
        refreshInventory();
    }
}
