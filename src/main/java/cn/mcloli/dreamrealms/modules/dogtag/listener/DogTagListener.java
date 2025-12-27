package cn.mcloli.dreamrealms.modules.dogtag.listener;

import cn.mcloli.dreamrealms.hook.PAPI;
import cn.mcloli.dreamrealms.modules.dogtag.DogTagModule;
import cn.mcloli.dreamrealms.modules.dogtag.config.DogTagConfig;
import cn.mcloli.dreamrealms.modules.dogtag.data.DogTagData;
import cn.mcloli.dreamrealms.utils.EntityNameUtil;
import cn.mcloli.dreamrealms.utils.ItemBuilder;
import cn.mcloli.dreamrealms.utils.ItemNameUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DogTagListener implements Listener {

    private final DogTagModule module;

    public DogTagListener(DogTagModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        DogTagConfig config = module.getModuleConfig();

        // 检查是否仅 PVP 掉落
        if (config.isPvpOnly() && killer == null) {
            module.debug("玩家 " + victim.getName() + " 非 PVP 死亡，不掉落狗牌");
            return;
        }

        // 检查是否有绕过权限
        for (String perm : config.getBypassPermissions()) {
            if (victim.hasPermission(perm)) {
                module.debug("玩家 " + victim.getName() + " 拥有绕过权限 " + perm + "，不掉落狗牌");
                return;
            }
        }

        // 查找匹配的狗牌配置 (按优先级)
        DogTagData matchedTag = null;
        for (DogTagData tag : config.getSortedDogTags()) {
            String perm = tag.getPermission();
            // permission 为空表示不需要权限
            if (perm == null || perm.isEmpty() || victim.hasPermission(perm)) {
                matchedTag = tag;
                break;
            }
        }

        if (matchedTag == null) {
            module.debug("玩家 " + victim.getName() + " 没有匹配的狗牌配置");
            return;
        }

        // 创建狗牌物品
        ItemStack dogTag = createDogTag(matchedTag, victim, killer, config.getDateFormat());
        if (dogTag == null) {
            module.warn("无法创建狗牌物品: " + matchedTag.getId());
            return;
        }

        // 在死亡位置生成掉落物 (兼容 keepInventory)
        Location dropLoc = victim.getLocation().add(0, 0.5, 0);
        Item droppedItem = victim.getWorld().dropItem(dropLoc, dogTag);
        // 不给初速度，让物品原地掉落
        droppedItem.setVelocity(new Vector(0, 0, 0));
        module.debug("玩家 " + victim.getName() + " 死亡，掉落狗牌: " + matchedTag.getId());
    }

    /**
     * 创建狗牌物品
     */
    @Nullable
    private ItemStack createDogTag(DogTagData data, Player victim, @Nullable Player killer, String dateFormat) {
        // 使用 ItemBuilder 解析材质 (支持 CraftEngine)
        ItemStack item = ItemBuilder.parseItem(data.getMaterial());
        if (item.getType() == Material.AIR) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        // 准备变量
        Location loc = victim.getLocation();
        DogTagConfig config = module.getModuleConfig();
        String killerName = getKillerName(victim, killer, config);
        String killerLevel = killer != null ? String.valueOf(killer.getLevel()) : "0";
        String weapon = getWeaponName(killer, config);
        String deathTime = new SimpleDateFormat(dateFormat).format(new Date());

        // 替换变量
        String displayName = replaceVariables(data.getDisplayName(), victim, killer,
                killerName, killerLevel, weapon, deathTime, loc);
        displayName = ColorHelper.parseColor(displayName);
        meta.setDisplayName(displayName);

        // 处理 Lore
        List<String> lore = new ArrayList<>();
        for (String line : data.getLore()) {
            String replaced = replaceVariables(line, victim, killer,
                    killerName, killerLevel, weapon, deathTime, loc);
            replaced = ColorHelper.parseColor(replaced);
            lore.add(replaced);
        }
        meta.setLore(lore);

        // CustomModelData
        if (data.getCustomModelData() != null) {
            meta.setCustomModelData(data.getCustomModelData());
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 替换变量
     */
    private String replaceVariables(String text, Player victim, @Nullable Player killer,
                                    String killerName, String killerLevel, String weapon,
                                    String deathTime, Location loc) {
        text = text.replace("{victim}", victim.getName())
                .replace("{killer}", killerName)
                .replace("{killer_level}", killerLevel)
                .replace("{weapon}", weapon)
                .replace("{death_time}", deathTime)
                .replace("{world}", loc.getWorld() != null ? loc.getWorld().getName() : "unknown")
                .replace("{x}", String.valueOf(loc.getBlockX()))
                .replace("{y}", String.valueOf(loc.getBlockY()))
                .replace("{z}", String.valueOf(loc.getBlockZ()));

        // PAPI 变量 - 使用死者作为上下文
        text = PAPI.setPlaceholders(victim, text);

        return text;
    }

    /**
     * 获取击杀者名称
     */
    private String getKillerName(Player victim, @Nullable Player killer, DogTagConfig config) {
        // 如果是玩家击杀
        if (killer != null) {
            return killer.getName();
        }
        
        // 尝试获取最后伤害来源实体
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent entityDamageEvent) {
            Entity damager = entityDamageEvent.getDamager();
            return EntityNameUtil.getEntityName(damager);
        }
        
        // 回退到配置的未知击杀者名称
        return config.getUnknownKillerName();
    }

    /**
     * 获取武器名称
     */
    private String getWeaponName(@Nullable Player killer, DogTagConfig config) {
        if (killer == null) {
            return config.getBareHandName();
        }
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.AIR) {
            return config.getBareHandName();
        }
        return ItemNameUtil.getItemName(weapon);
    }
}
