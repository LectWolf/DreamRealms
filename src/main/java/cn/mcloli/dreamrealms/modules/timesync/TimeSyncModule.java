package cn.mcloli.dreamrealms.modules.timesync;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.command.CommandMain;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.timesync.command.TimeSyncCommand;
import cn.mcloli.dreamrealms.modules.timesync.config.TimeSyncConfig;
import cn.mcloli.dreamrealms.modules.timesync.lang.TimeSyncMessages;
import cn.mcloli.dreamrealms.modules.timesync.listener.TimeSyncListener;
import cn.mcloli.dreamrealms.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.Calendar;

@AutoRegister
public class TimeSyncModule extends AbstractModule {

    private TimeSyncConfig config;
    private TimeSyncListener listener;
    private TimeSyncCommand command;
    private Runnable syncTaskCanceller;
    private TimeSyncMessages.Holder lang;

    public TimeSyncModule(DreamRealms plugin) {
        super(plugin, "timesync");
    }
    
    @Override
    protected String getModuleDescription() {
        return "时间同步模块 - 同步游戏时间与现实时间";
    }

    public static TimeSyncModule inst() {
        return instanceOf(TimeSyncModule.class);
    }

    public TimeSyncConfig getModuleConfig() {
        return config;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            stopSyncTask();
            return;
        }

        // 首次加载时注册语言
        if (lang == null) {
            lang = TimeSyncMessages.register();
        }

        if (config == null) {
            config = new TimeSyncConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        if (listener == null) {
            listener = new TimeSyncListener(this);
            registerEvents(listener);
        }

        if (command == null) {
            command = new TimeSyncCommand(this);
            CommandMain.inst().registerHandler(command);
        }

        startSyncTask();
        info("模块已加载");
    }

    public void startSyncTask() {
        stopSyncTask();
        syncTaskCanceller = Util.runTaskTimer(this::syncTime, 0L, config.getSyncInterval());
        debug("同步任务已启动, 间隔: " + config.getSyncInterval() + " ticks");
    }

    public void stopSyncTask() {
        if (syncTaskCanceller != null) {
            syncTaskCanceller.run();
            syncTaskCanceller = null;
            debug("同步任务已停止");
        }
    }

    private void syncTime() {
        Calendar now = Calendar.getInstance(config.getTimeZone());
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        long mcTime = convertToMinecraftTime(hour, minute);

        for (World world : Bukkit.getWorlds()) {
            if (shouldSync(world)) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setTime(mcTime);
                debug("同步世界 " + world.getName() + " 时间: " + hour + ":" + minute + " -> " + mcTime);
            }
        }
    }

    public boolean shouldSync(World world) {
        String name = world.getName();
        if (config.isWhitelistMode()) {
            return config.getWorlds().contains(name);
        } else {
            return !config.getWorlds().contains(name);
        }
    }

    public void restoreDaylightCycle() {
        for (World world : Bukkit.getWorlds()) {
            if (shouldSync(world)) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }
        }
    }

    private long convertToMinecraftTime(int hour, int minute) {
        double totalMinutes = hour * 60 + minute;
        double mcTime = totalMinutes / 1440.0 * 24000.0;
        return (long) ((mcTime + 18000.0) % 24000.0);
    }
}
