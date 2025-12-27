package cn.mcloli.dreamrealms.modules.ownerbind.listener;

import cn.mcloli.dreamrealms.modules.ownerbind.OwnerBindModule;
import cn.mcloli.dreamrealms.modules.ownerbind.lang.OwnerBindMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweetmail.IMail;
import top.mrxiaom.sweetmail.attachments.AttachmentItem;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OwnerBindListener implements Listener {

    private final OwnerBindModule module;
    private final Map<UUID, Boolean> playerInventoryOpenMap = new ConcurrentHashMap<>();

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
        if (module.isEmptyItem(item)) return true;

        // 修复Lore
        module.repairItemLore(item);

        // 自动绑定 (有 nobind 权限的玩家不会自动绑定)
        if (!hasNobindPermission(player) && module.isBindable(item)) {
            module.bindToPlayer(item, player.getName());
            module.debug("物品自动绑定给: " + player.getName());
        }

        // 检查所有权
        if (!hasBypassPermission(player) && !module.isOwner(player, item)) {
            return false;
        }

        return true;
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
            String senderName = config.getMailSenderName();
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

            module.debug("邮件发送状态: " + status);
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

        if (!processItem(player, currentItem)) {
            String boundOwner = module.getBoundOwner(currentItem);
            
            if (module.getModuleConfig().isHookSweetMail() && isPlayerInventory) {
                boolean mailSent = sendReturnMail(player, boundOwner, currentItem);
                if (mailSent) {
                    OwnerBindMessages.mail_send_success.tm(player, Pair.of("%owner%", boundOwner));
                    event.setCurrentItem(null);
                } else {
                    event.setCurrentItem(null);
                    player.getWorld().dropItemNaturally(player.getLocation(), currentItem);
                }
            } else if (isPlayerInventory) {
                event.setCurrentItem(null);
                player.getWorld().dropItemNaturally(player.getLocation(), currentItem);
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
            module.bindToPlayer(item, player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (module.isEmptyItem(item)) return;

        if (!processItem(player, item)) {
            event.setCancelled(true);
            String boundOwner = module.getBoundOwner(item);
            
            EquipmentSlot hand = event.getHand();
            if (hand == EquipmentSlot.HAND) {
                player.getInventory().setItemInMainHand(null);
            } else if (hand == EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(null);
            }
            
            handleNonOwnerAction(player, item, true);
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
            module.bindToPlayer(newItem, player.getName());
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
            player.getWorld().dropItemNaturally(player.getLocation(), newItem);
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
            module.bindToPlayer(item, player.getName());
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
}
