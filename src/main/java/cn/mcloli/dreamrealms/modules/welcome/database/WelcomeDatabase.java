package cn.mcloli.dreamrealms.modules.welcome.database;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.database.AbstractModuleDatabase;
import cn.mcloli.dreamrealms.modules.welcome.WelcomeModule;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * 欢迎模块数据库
 * 记录新玩家首次进服信息
 */
public class WelcomeDatabase extends AbstractModuleDatabase {

    public WelcomeDatabase(DreamRealms plugin, WelcomeModule module) {
        super(plugin, module);
    }

    @Override
    protected void createTables(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + table("players") + " (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "name VARCHAR(32) NOT NULL, " +
                "first_join TIMESTAMP NOT NULL, " +
                "welcome_count INT DEFAULT 0" +
                ")";
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
        module.info("数据表已初始化");
    }

    /**
     * 检查玩家是否为新玩家（首次进服）
     */
    public boolean isNewPlayer(UUID uuid) {
        String sql = "SELECT 1 FROM " + table("players") + " WHERE uuid = ?";
        return !exists(sql, uuid.toString());
    }

    /**
     * 记录新玩家
     */
    public void recordNewPlayer(UUID uuid, String name) {
        String sql = "INSERT INTO " + table("players") + " (uuid, name, first_join, welcome_count) VALUES (?, ?, ?, 0)";
        executeUpdate(sql, uuid.toString(), name, new Timestamp(System.currentTimeMillis()));
        module.debug("记录新玩家: " + name + " (" + uuid + ")");
    }

    /**
     * 更新欢迎人数
     */
    public void updateWelcomeCount(UUID uuid, int count) {
        String sql = "UPDATE " + table("players") + " SET welcome_count = ? WHERE uuid = ?";
        executeUpdate(sql, count, uuid.toString());
        module.debug("更新欢迎人数: " + uuid + " -> " + count);
    }

    /**
     * 获取欢迎人数
     */
    public int getWelcomeCount(UUID uuid) {
        String sql = "SELECT welcome_count FROM " + table("players") + " WHERE uuid = ?";
        Integer count = queryOne(sql, rs -> {
            try {
                return rs.getInt("welcome_count");
            } catch (SQLException e) {
                return 0;
            }
        }, uuid.toString());
        return count != null ? count : 0;
    }
}
