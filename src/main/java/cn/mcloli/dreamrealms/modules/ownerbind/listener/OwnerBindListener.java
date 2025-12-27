package cn.mcloli.dreamrealms.modules.ownerbind.listener;

import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import cn.mcloli.dreamrealms.modules.ownerbind.api.OwnerBindEvent;
import cn.mcloli.dreamrealms.modules.ownerbind.lang.OwnerBindMessages;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweetmail.IMail;
import top.mrxiaom.sweetmail.attachments.AttachmentItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OwnerBindListener implements Listener {

    private final OwnerBindModule module;
    private final Map<UUID, Boolean> playerInventoryOpenMap = new ConcurrentHashMap<>();
    private final Map<UUID, List<ItemStack>> deathKeepItems = new ConcurrentHashMap<>();

    public OwnerBindListener(OwnerBindModule module) {
        this.module = module;
    }

    // ========== 通用处理方法 ==========

    /**
     * 检查是否有绕过权限
     */
    private boolean hasBypassPermission(Player player) {
        return player.hasPermission("dreamrealms.ownerbind.bypass");
    }

    /**
     * 检查是否有免绑定权限 (手持可绑定物品不会自动绑定)
     */
    private boolean hasNobindPermission(Player player) {
        return player.hasPermission("dreamrealms.ownerbind.nobind");
    }

    /**
     * 处理物品：修复Lore + 自动绑定 + 检查所有权
     * @return true 如果玩家可以操作该物品
     */
    private boolean processItem(Player player, ItemStack item) {
        return processItem(player, item, OwnerBindEvent.BindSource.OTHER);
    }

    /**
     * 处理物品：修复Lore + 自动绑定 + 检查所有权
     * @param source 绑定来源
     * @return true 如果玩家可以操作该物品
     */
    private boolean processItem(Player player, ItemStack item, OwnerBindEvent.BindSource source) {
        if (module.isEmptyItem(item)) return true;

        // 修复Lore
        module.repairItemLore(item);

        // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
        if (!hasNobindPermission(player) && module.isBindable(item)) {
            module.bindToPlayer(item, player.getName(), source);
            module.debug("物品自动绑定给: " + player.getName());
        }

        // 检查所有权
        if (!hasBypassPermission(player) && !module.isOwner(player, item)) {
            return false;
        }

        return true;
    }

    /**
     * 处理非物主物品 (根据配置丢出或销毁)
     * @param player 玩家
     * @param item 物品
     * @param boundOwner 物主名
     */
    private void handleNonOwnerItem(Player player, ItemStack item, String boundOwner) {
        if (module.getModuleConfig().isNonOwnerDrop()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            module.debug("非物主物品已丢出: " + item.getType());
        } else {
            module.debug("非物主物品已销毁: " + item.getType());
        }
    }

    /**
     * 处理非持有者操作
     */
    private void handleNonOwnerAction(Player player, ItemStack item, boolean isPlayerInventory) {
        String boundOwner = module.getBoundOwner(item);
        if (boundOwner == null) return;

        boolean mailSent = false;

        // 尝试邮件归还
        if (module.getModuleConfig().isHookSweetMail() && isPlayerInventory) {
            mailSent = sendReturnMail(player, boundOwner, item);
            if (mailSent) {
                OwnerBindMessages.mail_send_success.tm(player, Pair.of("%owner%", boundOwner));
            } else {
                OwnerBindMessages.mail_send_failed.tm(player);
            }
        }

        // 提示玩家
        if (!mailSent) {
            OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
        }
    }

    /**
     * 发送归还邮件
     */
    private boolean sendReturnMail(Player attemptedPlayer, String boundOwner, ItemStack item) {
        if (!Bukkit.getPluginManager().isPluginEnabled("SweetMail")) {
            return false;
        }

        try {
            var config = module.getModuleConfig();
            // 根据配置决定发送者
            String senderName = config.isMailSenderPlayer() ? attemptedPlayer.getName() : config.getMailSenderName();
            String icon = config.getMailIcon();
            String title = config.getMailTitle();
            String content = String.join("\n", config.getMailContent())
                    .replace("%player%", attemptedPlayer.getName())
                    .replace("%owner%", boundOwner);
            int outdateDays = config.getMailOutdateDays();
            
            var mailBuilder = IMail.api().createMail(senderName)
                    .setIcon(icon)
                    .setSenderDisplay(senderName)
                    .setTitle(title)
                    .addContent(content)
                    .addAttachments(AttachmentItem.build(item.clone()))
                    .setReceiver(boundOwner);
            
            // 0 表示永不过期
            if (outdateDays > 0) {
                long outdateTime = System.currentTimeMillis() + (long) outdateDays * 24 * 60 * 60 * 1000;
                mailBuilder.setOutdate(outdateTime);
            }
            
            IMail.Status status = mailBuilder.send();

            module.debug("邮件发送状态: " + status + ", 发送者: " + senderName);
            return status.ok();
        } catch (Exception e) {
            module.warn("发送归还邮件失败: " + e.getMessage());
            return false;
        }
    }

    // ========== 事件监听 ==========

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        playerInventoryOpenMap.put(player.getUniqueId(), true);

        ItemStack currentItem = event.getCurrentItem();
        if (module.isEmptyItem(currentItem)) return;

        boolean isPlayerInventory = event.getClickedInventory() != null 
                && event.getClickedInventory().getType() == InventoryType.PLAYER;
        boolean isContainerInventory = event.getClickedInventory() != null 
                && event.getClickedInventory().getType() != InventoryType.PLAYER
                && event.getClickedInventory().getType() != InventoryType.CREATIVE;

        // 修复Lore
        module.repairItemLore(currentItem);

        // 从容器中拿取他人绑定物品的处理
        if (isContainerInventory && !hasBypassPermission(player) && !module.isOwner(player, currentItem)) {
            String boundOwner = module.getBoundOwner(currentItem);
            
            if (module.getModuleConfig().isAntiContainerPickup()) {
                // 阻止拿取
                OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
                event.setCancelled(true);
                return;
            }
            // 不阻止时，后续逻辑会处理（物品掉落）
        }

        if (!processItem(player, currentItem, OwnerBindEvent.BindSource.INVENTORY)) {
            String boundOwner = module.getBoundOwner(currentItem);
            
            if (module.getModuleConfig().isHookSweetMail() && isPlayerInventory) {
                boolean mailSent = sendReturnMail(player, boundOwner, currentItem);
                if (mailSent) {
                    OwnerBindMessages.mail_send_success.tm(player, Pair.of("%owner%", boundOwner));
                    event.setCurrentItem(null);
                } else {
                    event.setCurrentItem(null);
                    handleNonOwnerItem(player, currentItem, boundOwner);
                }
            } else if (isPlayerInventory) {
                event.setCurrentItem(null);
                handleNonOwnerItem(player, currentItem, boundOwner);
            }
            
            OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (module.isEmptyItem(item)) return;

        // 修复Lore
        module.repairItemLore(item);

        // 检查所有权
        if (!hasBypassPermission(player) && !module.isOwner(player, item)) {
            String boundOwner = module.getBoundOwner(item);

            if (module.getModuleConfig().isHookSweetMail()) {
                boolean mailSent = sendReturnMail(player, boundOwner, item);
                if (mailSent) {
                    OwnerBindMessages.mail_send_success.tm(player, Pair.of("%owner%", boundOwner));
                    event.getItem().remove();
                }
            }
            event.setCancelled(true);
            return;
        }

        // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
        if (!hasNobindPermission(player) && module.isBindable(item)) {
            module.bindToPlayer(item, player.getName(), OwnerBindEvent.BindSource.PICKUP);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // 即使物品为空也要检查右键动作（可能是空手右键装备槽）
        boolean isRightClick = event.getAction().name().contains("RIGHT");
        
        if (module.isEmptyItem(item)) {
            // 空手右键时不处理
            return;
        }

        // 检查是否是右键穿戴盔甲
        boolean isRightClickEquip = isRightClick && isArmorItem(item.getType());
        
        module.debug("交互物品: " + player.getName() + ", 物品: " + item.getType() + ", Action: " + event.getAction() + ", 右键穿戴: " + isRightClickEquip + ", 事件已取消: " + event.isCancelled());

        // 修复Lore
        module.repairItemLore(item);

        // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
        if (!hasNobindPermission(player) && module.isBindable(item) && !module.hasBoundOwner(item)) {
            module.bindToPlayer(item, player.getName(), OwnerBindEvent.BindSource.INTERACT);
            module.debug("交互时自动绑定给: " + player.getName());
        }

        // 检查所有权 - 手持物品
        if (!hasBypassPermission(player) && !module.isOwner(player, item)) {
            String boundOwner = module.getBoundOwner(item);
            module.debug("交互被阻止: 非物主, 物主: " + boundOwner);
            OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
            event.setCancelled(true);
            
            // 处理非物主的物品
            EquipmentSlot hand = event.getHand();
            if (hand == EquipmentSlot.HAND) {
                player.getInventory().setItemInMainHand(null);
            } else if (hand == EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(null);
            }
            handleNonOwnerItem(player, item, boundOwner);
            return;
        }

        // 右键穿戴盔甲时，检查身上已穿戴的盔甲是否是他人绑定的
        if (isRightClickEquip && !hasBypassPermission(player)) {
            ItemStack wornArmor = getWornArmorByType(player, item.getType());
            module.debug("检查已穿戴盔甲: " + (wornArmor != null ? wornArmor.getType() : "null") + ", 是否为空: " + module.isEmptyItem(wornArmor));
            if (!module.isEmptyItem(wornArmor)) {
                module.debug("已穿戴盔甲物主: " + module.getBoundOwner(wornArmor) + ", 是否为物主: " + module.isOwner(player, wornArmor));
                if (!module.isOwner(player, wornArmor)) {
                    String boundOwner = module.getBoundOwner(wornArmor);
                    module.debug("替换盔甲被阻止: 身上盔甲非物主, 物主: " + boundOwner);
                    OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * 根据物品类型获取玩家身上对应槽位的盔甲
     */
    private ItemStack getWornArmorByType(Player player, Material itemType) {
        String name = itemType.name();
        if (name.endsWith("_HELMET") || name.equals("TURTLE_HELMET")) {
            return player.getInventory().getHelmet();
        } else if (name.endsWith("_CHESTPLATE") || itemType == Material.ELYTRA) {
            return player.getInventory().getChestplate();
        } else if (name.endsWith("_LEGGINGS")) {
            return player.getInventory().getLeggings();
        } else if (name.endsWith("_BOOTS")) {
            return player.getInventory().getBoots();
        }
        return null;
    }

    /**
     * 检查物品是否是盔甲类型
     */
    private boolean isArmorItem(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") 
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || material == Material.ELYTRA || name.equals("TURTLE_HELMET");
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (module.isEmptyItem(item)) return;

        module.debug("消耗物品: " + player.getName() + ", 物品: " + item.getType());

        // 修复Lore
        module.repairItemLore(item);

        // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
        if (!hasNobindPermission(player) && module.isBindable(item) && !module.hasBoundOwner(item)) {
            module.bindToPlayer(item, player.getName(), OwnerBindEvent.BindSource.INTERACT);
            module.debug("消耗时自动绑定给: " + player.getName());
        }

        // 检查所有权
        if (!hasBypassPermission(player) && !module.isOwner(player, item)) {
            String boundOwner = module.getBoundOwner(item);
            module.debug("消耗被阻止: 非物主, 物主: " + boundOwner);
            OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // 检查是否是装备槽点击 (放入装备)
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack cursorItem = event.getCursor();
            if (!module.isEmptyItem(cursorItem)) {
                module.debug("装备物品(光标): " + player.getName() + ", 物品: " + cursorItem.getType() + ", 槽位: " + event.getSlot());

                // 修复Lore
                module.repairItemLore(cursorItem);

                // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
                if (!hasNobindPermission(player) && module.isBindable(cursorItem) && !module.hasBoundOwner(cursorItem)) {
                    module.bindToPlayer(cursorItem, player.getName(), OwnerBindEvent.BindSource.INVENTORY);
                    module.debug("装备时自动绑定给: " + player.getName());
                }

                // 检查所有权
                if (!hasBypassPermission(player) && !module.isOwner(player, cursorItem)) {
                    String boundOwner = module.getBoundOwner(cursorItem);
                    module.debug("装备被阻止(光标): 非物主, 物主: " + boundOwner);
                    OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
        // 检查 Shift+点击装备 (从背包快速装备)
        if (event.isShiftClick() && event.getClickedInventory() != null 
                && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            ItemStack clickedItem = event.getCurrentItem();
            if (!module.isEmptyItem(clickedItem) && isArmorItem(clickedItem.getType())) {
                module.debug("Shift装备物品: " + player.getName() + ", 物品: " + clickedItem.getType());

                // 修复Lore
                module.repairItemLore(clickedItem);

                // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
                if (!hasNobindPermission(player) && module.isBindable(clickedItem) && !module.hasBoundOwner(clickedItem)) {
                    module.bindToPlayer(clickedItem, player.getName(), OwnerBindEvent.BindSource.INVENTORY);
                    module.debug("Shift装备时自动绑定给: " + player.getName());
                }

                // 检查所有权
                if (!hasBypassPermission(player) && !module.isOwner(player, clickedItem)) {
                    String boundOwner = module.getBoundOwner(clickedItem);
                    module.debug("Shift装备被阻止: 非物主, 物主: " + boundOwner);
                    OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (module.isEmptyItem(newItem)) return;

        // 修复Lore
        module.repairItemLore(newItem);

        // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
        if (!hasNobindPermission(player) && module.isBindable(newItem) && !module.hasBoundOwner(newItem)) {
            module.bindToPlayer(newItem, player.getName(), OwnerBindEvent.BindSource.HOLD);
        }

        // 检查所有权
        if (!hasBypassPermission(player) && !module.isOwner(player, newItem)) {
            String boundOwner = module.getBoundOwner(newItem);

            if (module.getModuleConfig().isHookSweetMail()) {
                boolean mailSent = sendReturnMail(player, boundOwner, newItem);
                if (mailSent) {
                    OwnerBindMessages.mail_send_success.tm(player, Pair.of("%owner%", boundOwner));
                    player.getInventory().setItem(event.getNewSlot(), null);
                    return;
                }
            }
            
            player.getInventory().setItem(event.getNewSlot(), null);
            handleNonOwnerItem(player, newItem, boundOwner);
            OwnerBindMessages.not_owner.tm(player, Pair.of("%owner%", boundOwner));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        if (module.isEmptyItem(item)) return;

        // 修复Lore
        module.repairItemLore(item);

        // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
        if (!hasNobindPermission(player) && module.isBindable(item) && !module.hasBoundOwner(item)) {
            module.bindToPlayer(item, player.getName(), OwnerBindEvent.BindSource.HOLD);
        }

        // 检查所有权
        if (!hasBypassPermission(player) && !module.isOwner(player, item)) {
            String boundOwner = module.getBoundOwner(item);

            if (module.getModuleConfig().isHookSweetMail()) {
                boolean mailSent = sendReturnMail(player, boundOwner, item);
                if (mailSent) {
                    OwnerBindMessages.mail_send_success.tm(player, Pair.of("%owner%", boundOwner));
                    event.getItemDrop().remove();
                    return;
                }
            }
        }

        // 阻止Q键丢出 (bypass 权限可以绕过)
        if (module.getModuleConfig().isAntiDrop() 
                && !hasBypassPermission(player)
                && module.hasBoundOwner(item) 
                && !playerInventoryOpenMap.getOrDefault(player.getUniqueId(), false)) {
            OwnerBindMessages.anti_drop_tip.tm(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            playerInventoryOpenMap.put(player.getUniqueId(), false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String worldName = player.getWorld().getName();
        String action = module.getModuleConfig().getDeathDropAction(worldName);
        
        module.debug("玩家死亡: " + player.getName() + ", 世界: " + worldName + ", 处理方式: " + action);
        
        // DEFAULT 不处理
        if ("DEFAULT".equals(action)) {
            return;
        }
        
        List<ItemStack> drops = event.getDrops();
        List<ItemStack> boundItems = new ArrayList<>();
        
        // 找出所有绑定物品
        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (!module.isEmptyItem(item) && module.hasBoundOwner(item) && module.isOwner(player, item)) {
                boundItems.add(item.clone());
                iterator.remove();
                module.debug("移除掉落物: " + item.getType());
            }
        }
        
        if (boundItems.isEmpty()) {
            return;
        }
        
        switch (action) {
            case "KEEP" -> {
                // 保留在背包中 - 存储物品，重生时归还
                deathKeepItems.put(player.getUniqueId(), boundItems);
                module.debug("保留绑定物品数量: " + boundItems.size());
            }
            case "DROP" -> {
                // 掉落到地上 - 重新添加到掉落列表
                drops.addAll(boundItems);
                module.debug("掉落绑定物品数量: " + boundItems.size());
            }
            case "DESTROY" -> {
                // 销毁物品 - 已从掉落列表移除，不做任何处理
                module.debug("销毁绑定物品数量: " + boundItems.size());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        restoreDeathKeepItems(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 处理玩家死亡后退出再登录的情况
        Player player = event.getPlayer();
        restoreDeathKeepItems(player);
    }

    /**
     * 归还死亡保留的绑定物品
     */
    private void restoreDeathKeepItems(Player player) {
        List<ItemStack> items = deathKeepItems.remove(player.getUniqueId());
        if (items != null && !items.isEmpty()) {
            // 延迟1tick归还物品，确保玩家已完全重生/加入
            Util.runLater(() -> {
                for (ItemStack item : items) {
                    Util.giveItem(player, item);
                }
                module.debug("归还绑定物品数量: " + items.size() + ", 玩家: " + player.getName());
            }, 1L);
        }
    }
}
