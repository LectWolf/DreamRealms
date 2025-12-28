package cn.mcloli.dreamrealms.modules.wank.listener;

import cn.mcloli.dreamrealms.modules.wank.WankModule;
import cn.mcloli.dreamrealms.modules.wank.lang.WankMessages;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScissorsListener implements Listener {

    private final WankModule module;
    // 掉落物 -> 原主人UUID
    private final Map<UUID, UUID> dropOwners = new ConcurrentHashMap<>();
    // 原主人UUID -> 可捡起时间
    private final Map<UUID, Long> ownerPickupTime = new ConcurrentHashMap<>();
    // 受害者被剪进度 (受害者UUID -> 当前被剪次数)
    private final Map<UUID, Integer> victimProgress = new ConcurrentHashMap<>();
    // 受害者需要的总剪刀次数 (受害者UUID -> 需要的次数)
    private final Map<UUID, Integer> victimRequiredCuts = new ConcurrentHashMap<>();
    // 剪刀冷却 (攻击者UUID -> 冷却结束时间)
    private final Map<UUID, Long> scissorsCooldown = new ConcurrentHashMap<>();
    // ActionBar 定时器取消器 (受害者UUID -> 取消器)
    private final Map<UUID, Runnable> actionBarTimers = new ConcurrentHashMap<>();
    // 受害者上次被剪时间 (受害者UUID -> 时间戳)
    private final Map<UUID, Long> lastCutTime = new ConcurrentHashMap<>();

    public ScissorsListener(WankModule module) {
        this.module = module;
    }
    
    /**
     * 恢复所有受害者的被剪进度 (每次减少1，但需要检查恢复间隔)
     */
    public void recoverAllProgress() {
        if (victimProgress.isEmpty()) return;
        
        long now = System.currentTimeMillis();
        int recoveryInterval = module.getModuleConfig().getScissorsRecoveryInterval() * 1000;
        
        var iterator = victimProgress.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            UUID victimUuid = entry.getKey();
            
            // 检查是否已经过了恢复间隔（从上次被剪开始计算）
            Long lastCut = lastCutTime.get(victimUuid);
            if (lastCut != null && now - lastCut < recoveryInterval) {
                // 还没到恢复时间，跳过
                continue;
            }
            
            int newProgress = entry.getValue() - 1;
            
            if (newProgress <= 0) {
                // 完全恢复
                iterator.remove();
                victimRequiredCuts.remove(victimUuid);
                lastCutTime.remove(victimUuid);
                stopActionBarTimer(victimUuid);
            } else {
                entry.setValue(newProgress);
                // 更新上次恢复时间为当前时间（下次恢复需要再等一个间隔）
                lastCutTime.put(victimUuid, now);
                // 更新 ActionBar
                Player victim = Bukkit.getPlayer(victimUuid);
                if (victim != null && victim.isOnline()) {
                    int required = victimRequiredCuts.getOrDefault(victimUuid, 1);
                    sendVictimActionBar(victim, newProgress, required);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!module.getModuleConfig().isScissorsEnabled()) {
            return;
        }

        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查是否手持剪刀
        if (item.getType() != Material.SHEARS) {
            return;
        }

        // 不能剪自己
        if (player.equals(target)) {
            return;
        }

        // 检查目标是否已经被剪过
        if (module.isCut(target.getUniqueId())) {
            sendActionBar(player, WankMessages.scissors_already_cut.str(Pair.of("%target%", target.getName())));
            return;
        }
        
        // 检查剪刀冷却 (静默)
        var config = module.getModuleConfig();
        Long cooldownEnd = scissorsCooldown.get(player.getUniqueId());
        if (cooldownEnd != null && System.currentTimeMillis() < cooldownEnd) {
            return;
        }

        event.setCancelled(true);
        
        // 设置冷却
        scissorsCooldown.put(player.getUniqueId(), System.currentTimeMillis() + config.getScissorsCooldown() * 1000L);
        
        // 计算需要的剪刀次数
        int requiredCuts = calculateRequiredCuts(target);
        
        if (requiredCuts > 1 && config.isPantProtectionEnabled()) {
            // 需要多次剪刀
            handleProgressCut(player, target, item, requiredCuts);
        } else {
            // 直接剪掉
            performCut(player, target, item);
        }
    }
    
    /**
     * 玩家死亡时清除被剪进度
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        victimProgress.remove(uuid);
        victimRequiredCuts.remove(uuid);
        lastCutTime.remove(uuid);
        stopActionBarTimer(uuid);
    }
    
    /**
     * 玩家退出时清除被剪进度
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        victimProgress.remove(uuid);
        victimRequiredCuts.remove(uuid);
        lastCutTime.remove(uuid);
        stopActionBarTimer(uuid);
    }
    
    /**
     * 发送 ActionBar 消息
     */
    private void sendActionBar(Player player, String message) {
        AdventureUtil.sendActionBar(player, ColorHelper.parseColor(message));
    }
    
    /**
     * 发送受害者 ActionBar 并启动持续显示定时器
     */
    private void sendVictimActionBar(Player victim, int current, int required) {
        String message = WankMessages.scissors_victim_progress.str(
                Pair.of("%current%", current),
                Pair.of("%required%", required)
        );
        sendActionBar(victim, message);
    }
    
    /**
     * 启动受害者 ActionBar 持续显示定时器
     */
    private void startActionBarTimer(Player victim, int current, int required) {
        UUID uuid = victim.getUniqueId();
        stopActionBarTimer(uuid);
        
        // 每 40 tick (2秒) 刷新一次 ActionBar
        Runnable canceller = Util.runTaskTimer(() -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                stopActionBarTimer(uuid);
                return;
            }
            Integer progress = victimProgress.get(uuid);
            Integer req = victimRequiredCuts.get(uuid);
            if (progress == null || req == null) {
                stopActionBarTimer(uuid);
                return;
            }
            sendVictimActionBar(p, progress, req);
        }, 40L, 40L);
        
        actionBarTimers.put(uuid, canceller);
    }
    
    /**
     * 停止受害者 ActionBar 定时器
     */
    private void stopActionBarTimer(UUID uuid) {
        Runnable canceller = actionBarTimers.remove(uuid);
        if (canceller != null) {
            canceller.run();
        }
    }

    /**
     * 计算需要的剪刀次数
     */
    private int calculateRequiredCuts(Player target) {
        var config = module.getModuleConfig();
        if (!config.isPantProtectionEnabled()) {
            return 1;
        }
        
        ItemStack pants = target.getInventory().getLeggings();
        if (Util.isAir(pants)) {
            return 1;
        }
        
        // 获取裤子材质对应的基础次数
        String materialName = pants.getType().name();
        int baseCuts = 1;
        
        for (Map.Entry<String, Integer> entry : config.getPantsCutsRequired().entrySet()) {
            if (materialName.contains(entry.getKey())) {
                baseCuts = entry.getValue();
                break;
            }
        }
        
        // 计算附魔加成
        int enchantBonus = 0;
        for (Map.Entry<String, Integer> entry : config.getEnchantmentBonus().entrySet()) {
            try {
                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(entry.getKey().toLowerCase()));
                if (enchant != null && pants.containsEnchantment(enchant)) {
                    int level = pants.getEnchantmentLevel(enchant);
                    enchantBonus += level * entry.getValue();
                }
            } catch (Exception ignored) {}
        }
        
        return baseCuts + enchantBonus;
    }

    /**
     * 处理进度剪刀
     */
    private void handleProgressCut(Player player, Player target, ItemStack scissors, int requiredCuts) {
        var config = module.getModuleConfig();
        UUID targetUuid = target.getUniqueId();
        
        // 获取或创建受害者进度
        int currentCuts = victimProgress.getOrDefault(targetUuid, 0) + 1;
        victimProgress.put(targetUuid, currentCuts);
        victimRequiredCuts.put(targetUuid, requiredCuts);
        // 记录被剪时间（重置恢复计时）
        lastCutTime.put(targetUuid, System.currentTimeMillis());
        
        // 受击效果
        if (config.isScissorsHitEffectEnabled()) {
            double damage = config.getScissorsHitDamage();
            if (damage > 0) {
                target.damage(damage, player);
            } else {
                // 只有受击效果不扣血 - 使用极小伤害触发受击动画
                target.damage(0.001, player);
            }
        }
        
        // 消耗剪刀耐久
        damageItem(scissors, config.getScissorsDurabilityCost());
        if (isDamaged(scissors)) {
            player.getInventory().setItemInMainHand(null);
        }
        
        // 消耗裤子耐久
        ItemStack pants = target.getInventory().getLeggings();
        if (Util.notAir(pants)) {
            damageItem(pants, config.getPantsDurabilityCost());
            if (isDamaged(pants)) {
                target.getInventory().setLeggings(null);
            } else {
                target.getInventory().setLeggings(pants);
            }
        }
        
        // 更新攻击者 ActionBar
        String attackerMsg = WankMessages.scissors_progress.str(
                Pair.of("%current%", currentCuts),
                Pair.of("%required%", requiredCuts),
                Pair.of("%target%", target.getName())
        );
        sendActionBar(player, attackerMsg);
        
        // 更新受害者 ActionBar 并启动持续显示
        sendVictimActionBar(target, currentCuts, requiredCuts);
        startActionBarTimer(target, currentCuts, requiredCuts);
        
        // 检查是否完成
        if (currentCuts >= requiredCuts) {
            // 清理进度
            victimProgress.remove(targetUuid);
            victimRequiredCuts.remove(targetUuid);
            stopActionBarTimer(targetUuid);
            
            // 执行剪刀
            performCut(player, target, scissors);
        }
    }

    /**
     * 执行剪刀操作
     */
    private void performCut(Player player, Player target, ItemStack scissors) {
        var config = module.getModuleConfig();
        
        // 剪掉目标
        module.cutPlayer(target.getUniqueId());

        // 消耗剪刀耐久 (如果没有裤子保护时)
        if (!config.isPantProtectionEnabled() || Util.isAir(target.getInventory().getLeggings())) {
            damageItem(scissors, config.getScissorsDurabilityCost());
            if (isDamaged(scissors)) {
                player.getInventory().setItemInMainHand(null);
            }
        }

        // 掉落物品
        if (config.isScissorsDropEnabled()) {
            dropCutItem(target);
        }
        
        // 剪掉伤害
        if (config.isScissorsCutDamageEnabled()) {
            double maxHealth = target.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            double damage = maxHealth * config.getScissorsCutDamagePercent();
            target.damage(damage, player);
        }

        // 通知双方 (聊天栏)
        WankMessages.scissors_cut_success.t(player, Pair.of("%target%", target.getName()));
        WankMessages.scissors_cut_victim.t(target, Pair.of("%player%", player.getName()));
    }

    /**
     * 扣除物品耐久
     */
    private void damageItem(ItemStack item, int damage) {
        if (item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(damageable.getDamage() + damage);
            item.setItemMeta(damageable);
        }
    }

    /**
     * 检查物品是否损坏
     */
    private boolean isDamaged(ItemStack item) {
        if (item.getItemMeta() instanceof Damageable damageable) {
            return damageable.getDamage() >= item.getType().getMaxDurability();
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Item itemEntity = event.getItem();
        UUID dropUuid = itemEntity.getUniqueId();
        UUID ownerUuid = dropOwners.get(dropUuid);

        if (ownerUuid == null) {
            return;
        }

        // 如果是原主人
        if (player.getUniqueId().equals(ownerUuid)) {
            Long pickupTime = ownerPickupTime.get(ownerUuid);
            if (pickupTime != null && System.currentTimeMillis() < pickupTime) {
                // 还不能捡起
                event.setCancelled(true);
                return;
            }
        }

        // 可以捡起，清理记录
        dropOwners.remove(dropUuid);
        ownerPickupTime.remove(ownerUuid);
    }


    private void dropCutItem(Player target) {
        var config = module.getModuleConfig();
        Material material = Material.matchMaterial(config.getScissorsDropMaterial());
        if (material == null) material = Material.PORKCHOP;

        ItemStack dropItem = new ItemStack(material);
        var meta = dropItem.getItemMeta();
        if (meta != null) {
            // 设置名称
            String name = config.getScissorsDropName()
                    .replace("%player%", target.getName());
            meta.setDisplayName(ColorHelper.parseColor(name));

            // 设置 lore
            List<String> lore = new ArrayList<>();
            for (String line : config.getScissorsDropLore()) {
                lore.add(ColorHelper.parseColor(line.replace("%player%", target.getName())));
            }
            meta.setLore(lore);
            
            // 设置 CustomModelData (可选)
            Integer cmd = config.getScissorsDropCustomModelData();
            if (cmd != null) {
                meta.setCustomModelData(cmd);
            }

            // 添加 NBT 标记
            if (config.isScissorsDropNbtEnabled()) {
                var container = meta.getPersistentDataContainer();
                var key = new NamespacedKey(Util.plugin(), config.getScissorsDropNbtKey());
                container.set(key, PersistentDataType.STRING, target.getUniqueId().toString());
            }

            dropItem.setItemMeta(meta);
        }

        // 掉落物品
        Item droppedItem = target.getWorld().dropItemNaturally(target.getLocation(), dropItem);

        // 记录原主人和捡起延迟
        int delaySeconds = config.getScissorsOwnerPickupDelay();
        dropOwners.put(droppedItem.getUniqueId(), target.getUniqueId());
        ownerPickupTime.put(target.getUniqueId(), System.currentTimeMillis() + delaySeconds * 1000L);
    }
}
