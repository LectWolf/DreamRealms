package cn.mcloli.dreamrealms.modules.itemmanager.database;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.database.AbstractModuleDatabase;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;
import cn.mcloli.dreamrealms.modules.itemmanager.data.ItemCategory;
import cn.mcloli.dreamrealms.modules.itemmanager.data.StoredItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 物品管理器数据库
 */
public class ItemManagerDatabase extends AbstractModuleDatabase {

    public ItemManagerDatabase(DreamRealms plugin, ItemManagerModule module) {
        super(plugin, module);
    }

    @Override
    protected void createTables(Connection conn) throws SQLException {
        // 分类表
        conn.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS %s (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(64) NOT NULL,
                icon VARCHAR(128),
                sort_order INT DEFAULT 0
            )
            """.formatted(table("categories")));

        // 物品表
        conn.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS %s (
                guid VARCHAR(36) PRIMARY KEY,
                identifier VARCHAR(64) UNIQUE,
                category_id VARCHAR(36),
                sort_order INT DEFAULT 0,
                serialized BOOLEAN DEFAULT FALSE,
                item_data MEDIUMTEXT NOT NULL
            )
            """.formatted(table("items")));

        module.info("数据表初始化完成");
    }

    // ==================== 分类操作 ====================

    /**
     * 获取所有分类
     */
    @NotNull
    public List<ItemCategory> getAllCategories() {
        return queryList(
                "SELECT * FROM " + table("categories") + " ORDER BY sort_order ASC",
                this::mapCategory
        );
    }

    /**
     * 获取分类
     */
    @Nullable
    public ItemCategory getCategory(UUID id) {
        return queryOne(
                "SELECT * FROM " + table("categories") + " WHERE id = ?",
                this::mapCategory,
                id.toString()
        );
    }

    /**
     * 保存分类
     */
    public boolean saveCategory(ItemCategory category) {
        // SQLite 兼容: 使用 INSERT OR REPLACE
        return executeUpdate("""
            INSERT OR REPLACE INTO %s (id, name, icon, sort_order) VALUES (?, ?, ?, ?)
            """.formatted(table("categories")),
                category.getId().toString(),
                category.getName(),
                category.getIcon(),
                category.getSortOrder()
        ) > 0;
    }

    /**
     * 删除分类
     */
    public boolean deleteCategory(UUID id) {
        // 先将该分类下的物品移到未分类
        executeUpdate(
                "UPDATE " + table("items") + " SET category_id = NULL WHERE category_id = ?",
                id.toString()
        );
        return executeUpdate(
                "DELETE FROM " + table("categories") + " WHERE id = ?",
                id.toString()
        ) > 0;
    }

    private ItemCategory mapCategory(ResultSet rs) {
        try {
            return new ItemCategory(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("name"),
                    rs.getString("icon"),
                    rs.getInt("sort_order")
            );
        } catch (SQLException e) {
            return null;
        }
    }

    // ==================== 物品操作 ====================

    /**
     * 获取所有物品
     */
    @NotNull
    public List<StoredItem> getAllItems() {
        return queryList(
                "SELECT * FROM " + table("items") + " ORDER BY sort_order ASC",
                this::mapItem
        );
    }

    /**
     * 获取分类下的物品
     */
    @NotNull
    public List<StoredItem> getItemsByCategory(@Nullable UUID categoryId) {
        if (categoryId == null) {
            return queryList(
                    "SELECT * FROM " + table("items") + " WHERE category_id IS NULL ORDER BY sort_order ASC",
                    this::mapItem
            );
        }
        return queryList(
                "SELECT * FROM " + table("items") + " WHERE category_id = ? ORDER BY sort_order ASC",
                this::mapItem,
                categoryId.toString()
        );
    }

    /**
     * 通过 GUID 获取物品
     */
    @Nullable
    public StoredItem getItemByGuid(UUID guid) {
        return queryOne(
                "SELECT * FROM " + table("items") + " WHERE guid = ?",
                this::mapItem,
                guid.toString()
        );
    }

    /**
     * 通过标识名获取物品
     */
    @Nullable
    public StoredItem getItemByIdentifier(String identifier) {
        return queryOne(
                "SELECT * FROM " + table("items") + " WHERE identifier = ?",
                this::mapItem,
                identifier
        );
    }

    /**
     * 通过标识名或 GUID 获取物品
     */
    @Nullable
    public StoredItem getItem(String identifierOrGuid) {
        // 先尝试作为标识名查找
        StoredItem item = getItemByIdentifier(identifierOrGuid);
        if (item != null) return item;

        // 再尝试作为 GUID 查找
        try {
            UUID guid = UUID.fromString(identifierOrGuid);
            return getItemByGuid(guid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 保存物品
     */
    public boolean saveItem(StoredItem item) {
        String itemData = serializeItemStack(item.getItemStack());
        if (itemData == null) return false;

        // SQLite 兼容: 使用 INSERT OR REPLACE
        return executeUpdate("""
            INSERT OR REPLACE INTO %s (guid, identifier, category_id, sort_order, serialized, item_data)
            VALUES (?, ?, ?, ?, ?, ?)
            """.formatted(table("items")),
                item.getGuid().toString(),
                item.getIdentifier(),
                item.getCategoryId() != null ? item.getCategoryId().toString() : null,
                item.getSortOrder(),
                item.isSerialized(),
                itemData
        ) > 0;
    }

    /**
     * 删除物品
     */
    public boolean deleteItem(UUID guid) {
        return executeUpdate(
                "DELETE FROM " + table("items") + " WHERE guid = ?",
                guid.toString()
        ) > 0;
    }

    private StoredItem mapItem(ResultSet rs) {
        try {
            String categoryIdStr = rs.getString("category_id");
            UUID categoryId = categoryIdStr != null ? UUID.fromString(categoryIdStr) : null;

            ItemStack itemStack = deserializeItemStack(rs.getString("item_data"));
            if (itemStack == null) return null;

            return new StoredItem(
                    UUID.fromString(rs.getString("guid")),
                    rs.getString("identifier"),
                    categoryId,
                    rs.getInt("sort_order"),
                    rs.getBoolean("serialized"),
                    itemStack
            );
        } catch (SQLException e) {
            return null;
        }
    }

    // ==================== 序列化工具 ====================

    @Nullable
    private String serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            module.warn("序列化物品失败: " + e.getMessage());
            return null;
        }
    }

    @Nullable
    private ItemStack deserializeItemStack(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            module.warn("反序列化物品失败: " + e.getMessage());
            return null;
        }
    }
}
