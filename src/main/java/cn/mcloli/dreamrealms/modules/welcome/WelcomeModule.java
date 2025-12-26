package cn.mcloli.dreamrealms.modules.welcome;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.command.CommandMain;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.welcome.command.WelcomeCommand;
import cn.mcloli.dreamrealms.modules.welcome.config.WelcomeConfig;
import cn.mcloli.dreamrealms.modules.welcome.data.WelcomeSession;
import cn.mcloli.dreamrealms.modules.welcome.database.WelcomeDatabase;
import cn.mcloli.dreamrealms.modules.welcome.lang.WelcomeMessages;
import cn.mcloli.dreamrealms.modules.welcome.listener.WelcomeListener;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@AutoRegister
public class WelcomeModule extends AbstractModule {

    private WelcomeConfig config;
    private WelcomeListener listener;
    private WelcomeCommand command;
    private WelcomeDatabase database;
    private WelcomeMessages.Holder lang;

    // 当前活跃的欢迎会话 (新玩家UUID -> 会话)
    private final Map<UUID, WelcomeSession> activeSessions = new ConcurrentHashMap<>();

    public WelcomeModule(DreamRealms plugin) {
        super(plugin, "welcome");
    }

    public static WelcomeModule inst() {
        return instanceOf(WelcomeModule.class);
    }

    public WelcomeConfig getModuleConfig() {
        return config;
    }

    public WelcomeDatabase getDatabase() {
        return database;
    }

    public WelcomeListener getListener() {
        return listener;
    }

    public Map<UUID, WelcomeSession> getActiveSessions() {
        return activeSessions;
    }

    @Nullable
    public WelcomeSession getSession(UUID newPlayerUuid) {
        return activeSessions.get(newPlayerUuid);
    }

    public void createSession(Player newPlayer) {
        WelcomeSession session = new WelcomeSession(newPlayer.getUniqueId(), newPlayer.getName());
        activeSessions.put(newPlayer.getUniqueId(), session);
        debug("创建欢迎会话: " + newPlayer.getName());
    }

    public void removeSession(UUID uuid) {
        WelcomeSession session = activeSessions.remove(uuid);
        if (session != null) {
            session.cancelRewardTask();
            debug("移除欢迎会话: " + session.getNewPlayerName());
        }
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            // 清理所有会话
            for (WelcomeSession session : activeSessions.values()) {
                session.cancelRewardTask();
            }
            activeSessions.clear();
            return;
        }

        // 首次加载时注册语言
        if (lang == null) {
            lang = WelcomeMessages.register();
        }

        // 首次加载时注册数据库
        if (database == null) {
            database = new WelcomeDatabase(plugin, this);
        }

        if (config == null) {
            config = new WelcomeConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        if (listener == null) {
            listener = new WelcomeListener(this);
            registerEvents(listener);
        }

        if (command == null) {
            command = new WelcomeCommand(this);
            CommandMain.inst().registerHandler(command);
        }

        info("模块已加载");
    }
}
