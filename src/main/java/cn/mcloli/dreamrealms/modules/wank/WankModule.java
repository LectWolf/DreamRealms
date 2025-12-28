package cn.mcloli.dreamrealms.modules.wank;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.wank.command.WankCommand;
import cn.mcloli.dreamrealms.modules.wank.config.WankConfig;
import cn.mcloli.dreamrealms.modules.wank.lang.WankMessages;
import cn.mcloli.dreamrealms.modules.wank.listener.BedEnterListener;
import cn.mcloli.dreamrealms.modules.wank.listener.ScissorsListener;
import cn.mcloli.dreamrealms.utils.CommandRegister;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;
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
        final int totalTicks = duration * 5;

        Util.runTaskTimer(() -> {
            if (!player.isOnline()) {
                processingPlayers.remove(player.getUniqueId());
                return;
            }

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_COD_FLOP, 0.3f, 1.0f);
            player.swingMainHand();

            counter[0]++;
            if (counter[0] >= totalTicks) {
                finishWank(player, originalSpeed, duration, maxTimes);
            }
        }, 0L, 4L);
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

        Util.runTaskTimer(() -> {
            if (!player.isOnline()) {
                return;
            }

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_EAT, 0.8f, 1.0f);
            player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.LLAMA_SPIT);

            counter[0]++;
            if (counter[0] >= totalTicks) {
                afterShoot(player, wankDuration, maxTimes);
            }
        }, 0L, 2L);
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
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(PotionType.WATER);
            meta.setCustomModelData(config.getCustomModelData());
            meta.setColor(Color.WHITE);

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
        return potion;
    }

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

        // 注册命令
        command = new WankCommand(this);
        CommandRegister.unregister("wank");
        CommandRegister.register(
                "wank",
                new String[]{},
                "导管命令",
                command
        );

        info("模块已加载");
    }
}
