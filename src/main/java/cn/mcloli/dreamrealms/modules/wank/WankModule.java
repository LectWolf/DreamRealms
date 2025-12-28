package cn.mcloli.dreamrealms.modules.wank;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.wank.command.WankCommand;
import cn.mcloli.dreamrealms.modules.wank.config.WankConfig;
import cn.mcloli.dreamrealms.modules.wank.lang.WankMessages;
import cn.mcloli.dreamrealms.modules.wank.listener.BedEnterListener;
import cn.mcloli.dreamrealms.modules.wank.listener.ScissorsListener;
import cn.mcloli.dreamrealms.modules.wank.listener.SpitDamageListener;
import cn.mcloli.dreamrealms.utils.CommandRegister;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@AutoRegister
public class WankModule extends AbstractModule {

    private WankConfig config;
    private WankMessages.Holder lang;
    private WankCommand command;
    private BedEnterListener bedListener;
    private ScissorsListener scissorsListener;
    private SpitDamageListener spitDamageListener;

    // 玩家今日导管次数
    private final Map<UUID, Integer> wankTimes = new HashMap<>();
    // 玩家今日最大导管次数
    private final Map<UUID, Integer> maxWankTimes = new HashMap<>();
    // 正在处理中的玩家
    private final Set<UUID> processingPlayers = ConcurrentHashMap.newKeySet();
    // 冷却时间
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    // 被剪掉的玩家 (UUID -> 恢复时间戳)
    private final Map<UUID, Long> cutPlayers = new ConcurrentHashMap<>();

    public WankModule(DreamRealms plugin) {
        super(plugin, "wank");
    }

    public static WankModule inst() {
        return instanceOf(WankModule.class);
    }

    @Override
    protected String getModuleDescription() {
        return "导管模块 - 夜间睡觉时的娱乐功能";
    }

    public WankConfig getModuleConfig() {
        return config;
    }

    public int getWankTimes(UUID uuid) {
        return wankTimes.getOrDefault(uuid, 0);
    }

    public void addWankTimes(UUID uuid) {
        wankTimes.put(uuid, getWankTimes(uuid) + 1);
    }

    public int getMaxWankTimes(UUID uuid) {
        if (!maxWankTimes.containsKey(uuid)) {
            int max = config.getRandomMaxTimes();
            maxWankTimes.put(uuid, max);
        }
        return maxWankTimes.get(uuid);
    }

    public void resetAllWankTimes() {
        wankTimes.clear();
        maxWankTimes.clear();
        cutPlayers.clear();
        info("已重置所有玩家的导管次数");
    }

    /**
     * 检查玩家是否被剪掉
     */
    public boolean isCut(UUID uuid) {
        Long recoverTime = cutPlayers.get(uuid);
        if (recoverTime == null) {
            return false;
        }
        if (System.currentTimeMillis() >= recoverTime) {
            cutPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    /**
     * 剪掉玩家
     */
    public void cutPlayer(UUID uuid) {
        long disableDuration = config.getScissorsDisableDuration() * 50L; // ticks to ms
        cutPlayers.put(uuid, System.currentTimeMillis() + disableDuration);
    }

    /**
     * 开始导管
     */
    public void startWank(Player player) {
        UUID uuid = player.getUniqueId();

        // 检查是否被剪掉
        if (isCut(uuid)) {
            WankMessages.cut_disabled.tm(player);
            return;
        }

        // 检查冷却
        long now = System.currentTimeMillis();
        Long lastUse = cooldowns.get(uuid);
        int cooldownSeconds = config.getCooldown();
        if (lastUse != null && now - lastUse < cooldownSeconds * 1000L) {
            WankMessages.cooldown.t(player);
            return;
        }

        // 检查是否正在处理中
        if (processingPlayers.contains(uuid)) {
            return;
        }

        // 检查是否已达到最大次数
        int times = getWankTimes(uuid);
        int maxTimes = getMaxWankTimes(uuid);
        if (times >= maxTimes) {
            WankMessages.recovering.tm(player);
            return;
        }

        // 设置冷却和处理状态
        cooldowns.put(uuid, now);
        processingPlayers.add(uuid);

        // 如果在床上，让玩家起床
        if (player.isSleeping()) {
            player.damage(0.001);
        }

        WankMessages.start.tm(player);

        // 保存并降低移动速度
        float originalSpeed = player.getWalkSpeed();
        player.setWalkSpeed(0.05f);

        // 随机导管时间
        int duration = config.getRandomWankDuration();

        // 开始导管动画
        startWankAnimation(player, duration, originalSpeed, maxTimes);
    }

    private void startWankAnimation(Player player, int duration, float originalSpeed, int maxTimes) {
        final int[] counter = {0};
        int interval = config.getHandSwingInterval();
        final int totalTicks = duration * (20 / interval); // 根据间隔计算总次数
        final Runnable[] cancelTask = {null};

        cancelTask[0] = Util.runAtEntityTimer(player, p -> {
            if (!p.isOnline()) {
                processingPlayers.remove(p.getUniqueId());
                if (cancelTask[0] != null) cancelTask[0].run();
                return;
            }

            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_COD_FLOP, config.getSoundVolume(), 1.0f);
            p.swingMainHand();

            counter[0]++;
            if (counter[0] >= totalTicks) {
                if (cancelTask[0] != null) cancelTask[0].run();
                finishWank(p, originalSpeed, duration, maxTimes);
            }
        }, 0L, interval);
    }

    private void finishWank(Player player, float originalSpeed, int duration, int maxTimes) {
        processingPlayers.remove(player.getUniqueId());

        if (!player.isOnline()) {
            return;
        }

        player.setWalkSpeed(originalSpeed);
        WankMessages.finish.tm(player, Pair.of("%duration%", duration));

        int shootDuration = config.getRandomShootDuration();
        startShootAnimation(player, shootDuration, duration, maxTimes);
    }

    private void startShootAnimation(Player player, int shootDuration, int wankDuration, int maxTimes) {
        final int[] counter = {0};
        final int totalTicks = shootDuration * 10;
        final Runnable[] cancelTask = {null};

        cancelTask[0] = Util.runAtEntityTimer(player, p -> {
            if (!p.isOnline()) {
                if (cancelTask[0] != null) cancelTask[0].run();
                return;
            }

            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CAT_EAT, 0.8f, 1.0f);
            
            // 计算发射方向 - 优先射向目标实体，否则射向前方10格
            double speed = config.getRandomShootSpeed();
            org.bukkit.Location spawnLoc = p.getLocation().clone().add(0, 0.8, 0);
            org.bukkit.util.Vector direction = calculateShootDirection(p, spawnLoc);
            
            // 使用 spawn 生成羊驼口水
            org.bukkit.entity.LlamaSpit spit = p.getWorld().spawn(spawnLoc, org.bukkit.entity.LlamaSpit.class, s -> {
                s.setShooter(p);
                s.setVelocity(direction.clone().multiply(speed));
            });
            
            // 手动生成喷射粒子效果
            org.bukkit.util.Vector particleDir = direction.clone().multiply(0.3);
            for (int i = 0; i < 5; i++) {
                p.getWorld().spawnParticle(
                        org.bukkit.Particle.SPIT,
                        spawnLoc.clone().add(particleDir.clone().multiply(i * 0.2)),
                        1, 0.05, 0.05, 0.05, 0.01
                );
            }

            counter[0]++;
            if (counter[0] >= totalTicks) {
                if (cancelTask[0] != null) cancelTask[0].run();
                afterShoot(p, wankDuration, maxTimes);
            }
        }, 0L, 2L);
    }
    
    /**
     * 计算射击方向 - 优先射向目标实体，否则射向前方10格
     */
    private org.bukkit.util.Vector calculateShootDirection(Player player, org.bukkit.Location spawnLoc) {
        // 尝试射线追踪获取目标实体
        org.bukkit.util.RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                10,
                entity -> entity != player && entity instanceof org.bukkit.entity.LivingEntity
        );
        
        org.bukkit.Location targetLoc;
        if (result != null && result.getHitEntity() != null) {
            // 射向目标实体
            targetLoc = result.getHitEntity().getLocation().add(0, 1, 0);
        } else {
            // 射向前方10格
            targetLoc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(10));
        }
        
        // 计算从发射位置到目标的方向
        return targetLoc.toVector().subtract(spawnLoc.toVector()).normalize();
    }

    private void afterShoot(Player player, int wankDuration, int maxTimes) {
        addWankTimes(player.getUniqueId());

        // 检查手中是否为玻璃瓶
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.GLASS_BOTTLE) {
            ItemStack milk = createMilkPotion(player, wankDuration);
            handItem.setAmount(handItem.getAmount() - 1);
            Util.giveItem(player, milk);
        }

        // 检查是否爆炸
        int newTimes = getWankTimes(player.getUniqueId());
        if (newTimes >= maxTimes) {
            WankMessages.explode.tm(player, Pair.of("%max_times%", maxTimes));
            player.getWorld().createExplosion(player.getLocation(), 10f, false, false);

            String broadcastMsg = WankMessages.explode_broadcast.str(Pair.of("%player%", player.getName()));
            Bukkit.broadcastMessage(ColorHelper.parseColor(broadcastMsg));
            player.setHealth(0);
        }
    }

    private ItemStack createMilkPotion(Player player, int duration) {
        Material material = Material.matchMaterial(config.getMilkMaterial());
        if (material == null) material = Material.POTION;
        
        ItemStack potion = new ItemStack(material);
        var meta = potion.getItemMeta();
        if (meta != null) {
            // 如果是药水类型，设置药水属性
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setBasePotionType(PotionType.WATER);
                // 解析颜色
                String colorStr = config.getMilkColor();
                if (colorStr != null && !colorStr.isEmpty()) {
                    potionMeta.setColor(parseHexColor(colorStr));
                } else {
                    potionMeta.setColor(Color.WHITE);
                }
            }
            
            // 设置 CustomModelData (可选)
            Integer cmd = config.getCustomModelData();
            if (cmd != null) {
                meta.setCustomModelData(cmd);
            }

            // 从配置读取名称
            String name = config.getMilkName()
                    .replace("%player%", player.getName());
            meta.setDisplayName(ColorHelper.parseColor(name));

            // 从配置读取 lore
            List<String> lore = new ArrayList<>();
            for (String line : config.getMilkLore()) {
                String replaced = line
                        .replace("%player%", player.getName())
                        .replace("%duration%", String.valueOf(duration));
                lore.add(ColorHelper.parseColor(replaced));
            }
            meta.setLore(lore);

            potion.setItemMeta(meta);
        }
        
        // 添加 NBT 标记
        if (config.isMilkNbtEnabled()) {
            potion = addNbtTag(potion, config.getMilkNbtKey(), player.getUniqueId().toString());
        }
        
        return potion;
    }
    
    /**
     * 添加 NBT 标记到物品
     */
    private ItemStack addNbtTag(ItemStack item, String key, String value) {
        var meta = item.getItemMeta();
        if (meta != null) {
            var container = meta.getPersistentDataContainer();
            var namespacedKey = new org.bukkit.NamespacedKey(plugin, key);
            container.set(namespacedKey, org.bukkit.persistence.PersistentDataType.STRING, value);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 解析十六进制颜色
     */
    private Color parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) return Color.WHITE;
        hex = hex.replace("#", "");
        try {
            int rgb = Integer.parseInt(hex, 16);
            return Color.fromRGB(rgb);
        } catch (NumberFormatException e) {
            return Color.WHITE;
        }
    }

    // 重置定时器取消器
    private Runnable resetTimerCanceller;
    // 恢复定时器取消器
    private Runnable recoveryTimerCanceller;
    // 上次重置的游戏日
    private long lastResetDay = -1;

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            return;
        }

        // 首次加载时注册语言
        if (lang == null) {
            lang = WankMessages.register();
        }

        // 初始化配置
        if (config == null) {
            config = new WankConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        // 注册监听器
        if (bedListener == null) {
            bedListener = new BedEnterListener(this);
            Bukkit.getPluginManager().registerEvents(bedListener, plugin);
        }
        if (scissorsListener == null) {
            scissorsListener = new ScissorsListener(this);
            Bukkit.getPluginManager().registerEvents(scissorsListener, plugin);
        }
        if (spitDamageListener == null) {
            spitDamageListener = new SpitDamageListener(this);
            Bukkit.getPluginManager().registerEvents(spitDamageListener, plugin);
        }

        // 注册命令
        command = new WankCommand(this);
        CommandRegister.unregister("wank");
        CommandRegister.register(
                "wank",
                new String[]{},
                "导管命令",
                command
        );
        
        // 启动重置定时器
        startResetTimer();
        
        // 启动恢复定时器
        startRecoveryTimer();

        info("模块已加载");
    }
    
    /**
     * 启动游戏时间重置定时器
     */
    private void startResetTimer() {
        // 取消旧定时器
        if (resetTimerCanceller != null) {
            resetTimerCanceller.run();
        }
        
        // 每 100 tick (5秒) 检查一次游戏时间
        resetTimerCanceller = Util.runTaskTimer(() -> {
            var world = Bukkit.getWorld(config.getResetWorld());
            if (world == null) return;
            
            // 解析重置时间
            String timeStr = config.getResetTime();
            String[] parts = timeStr.split(":");
            if (parts.length != 2) return;
            
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            // 转换为游戏 tick (0 = 6:00, 6000 = 12:00, 12000 = 18:00, 18000 = 0:00)
            // 游戏时间公式: (hour - 6) * 1000 + minute * 1000 / 60
            int targetTick = ((hour - 6 + 24) % 24) * 1000 + minute * 1000 / 60;
            
            long worldTime = world.getTime();
            long currentDay = world.getFullTime() / 24000;
            
            // 检查是否到达重置时间且今天还没重置
            if (worldTime >= targetTick && worldTime < targetTick + 200 && currentDay != lastResetDay) {
                lastResetDay = currentDay;
                resetAllWankTimes();
            }
        }, 100L, 100L);
    }
    
    /**
     * 启动被剪恢复定时器
     */
    private void startRecoveryTimer() {
        // 取消旧定时器
        if (recoveryTimerCanceller != null) {
            recoveryTimerCanceller.run();
        }
        
        int intervalSeconds = config.getScissorsRecoveryInterval();
        
        // 每隔 interval 秒恢复一次剪刀进度
        recoveryTimerCanceller = Util.runTaskTimer(() -> {
            if (scissorsListener != null) {
                scissorsListener.recoverAllProgress();
            }
        }, 20L * intervalSeconds, 20L * intervalSeconds);
    }
}
