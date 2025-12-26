package cn.mcloli.dreamrealms.modules.welcome.data;

import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 欢迎会话数据
 * 记录一个新玩家的欢迎状态
 */
public class WelcomeSession {

    private final UUID newPlayerUuid;
    private final String newPlayerName;
    private final long createTime;

    // 已欢迎的玩家UUID集合
    private final Set<UUID> welcomers = new HashSet<>();

    // 奖励任务
    private BukkitTask rewardTask;

    // 新玩家是否在线
    private boolean newPlayerOnline = true;

    public WelcomeSession(UUID newPlayerUuid, String newPlayerName) {
        this.newPlayerUuid = newPlayerUuid;
        this.newPlayerName = newPlayerName;
        this.createTime = System.currentTimeMillis();
    }

    public UUID getNewPlayerUuid() {
        return newPlayerUuid;
    }

    public String getNewPlayerName() {
        return newPlayerName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public Set<UUID> getWelcomers() {
        return welcomers;
    }

    public int getWelcomerCount() {
        return welcomers.size();
    }

    public boolean hasWelcomed(UUID playerUuid) {
        return welcomers.contains(playerUuid);
    }

    public void addWelcomer(UUID playerUuid) {
        welcomers.add(playerUuid);
    }

    public BukkitTask getRewardTask() {
        return rewardTask;
    }

    public void setRewardTask(BukkitTask rewardTask) {
        this.rewardTask = rewardTask;
    }

    public void cancelRewardTask() {
        if (rewardTask != null) {
            rewardTask.cancel();
            rewardTask = null;
        }
    }

    public boolean isNewPlayerOnline() {
        return newPlayerOnline;
    }

    public void setNewPlayerOnline(boolean online) {
        this.newPlayerOnline = online;
    }
}
