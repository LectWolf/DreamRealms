package cn.mcloli.dreamrealms.database;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.database.IDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 模块数据库基类
 * 封装常用数据库操作，简化模块数据库开发
 */
public abstract class AbstractModuleDatabase implements IDatabase {

    protected final DreamRealms plugin;
    protected final AbstractModule module;
    protected String tablePrefix;

    public AbstractModuleDatabase(DreamRealms plugin, AbstractModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    /**
     * 获取表名 (自动添加前缀)
     */
    protected String table(String name) {
        return tablePrefix + name;
    }

    /**
     * 获取数据库连接
     */
    @Nullable
    protected Connection getConnection() {
        return plugin.getConnection();
    }

    /**
     * 子类实现：创建数据表
     */
    protected abstract void createTables(Connection conn) throws SQLException;

    @Override
    public void reload(Connection conn, String tablePrefix) throws SQLException {
        this.tablePrefix = tablePrefix + module.getModuleId() + "_";
        createTables(conn);
    }

    // ==================== 工具方法 ====================

    /**
     * 执行更新语句 (INSERT/UPDATE/DELETE)
     */
    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection()) {
            if (conn == null) return 0;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, params);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    /**
     * 执行批量更新
     */
    protected int[] executeBatch(String sql, List<Object[]> paramsList) {
        try (Connection conn = getConnection()) {
            if (conn == null) return new int[0];
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Object[] params : paramsList) {
                    setParameters(ps, params);
                    ps.addBatch();
                }
                return ps.executeBatch();
            }
        } catch (SQLException e) {
            handleException(e);
            return new int[0];
        }
    }

    /**
     * 查询单个结果
     */
    @Nullable
    protected <T> T queryOne(String sql, Function<ResultSet, T> mapper, Object... params) {
        try (Connection conn = getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapper.apply(rs);
                    }
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }
        return null;
    }

    /**
     * 查询列表结果
     */
    @NotNull
    protected <T> List<T> queryList(String sql, Function<ResultSet, T> mapper, Object... params) {
        List<T> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            if (conn == null) return list;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        T item = mapper.apply(rs);
                        if (item != null) {
                            list.add(item);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }
        return list;
    }

    /**
     * 查询数量
     */
    protected int queryCount(String sql, Object... params) {
        Integer count = queryOne(sql, rs -> {
            try {
                return rs.getInt(1);
            } catch (SQLException e) {
                return 0;
            }
        }, params);
        return count != null ? count : 0;
    }

    /**
     * 检查记录是否存在
     */
    protected boolean exists(String sql, Object... params) {
        return queryCount(sql, params) > 0;
    }

    /**
     * 设置 PreparedStatement 参数
     */
    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param == null) {
                ps.setNull(i + 1, Types.NULL);
            } else if (param instanceof String) {
                ps.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                ps.setInt(i + 1, (Integer) param);
            } else if (param instanceof Long) {
                ps.setLong(i + 1, (Long) param);
            } else if (param instanceof Double) {
                ps.setDouble(i + 1, (Double) param);
            } else if (param instanceof Boolean) {
                ps.setBoolean(i + 1, (Boolean) param);
            } else if (param instanceof byte[]) {
                ps.setBytes(i + 1, (byte[]) param);
            } else if (param instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) param);
            } else {
                ps.setObject(i + 1, param);
            }
        }
    }

    /**
     * 处理异常
     */
    protected void handleException(SQLException e) {
        module.warn("数据库操作异常: " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * 日志
     */
    protected void info(String message) {
        module.info(message);
    }

    protected void warn(String message) {
        module.warn(message);
    }
}
