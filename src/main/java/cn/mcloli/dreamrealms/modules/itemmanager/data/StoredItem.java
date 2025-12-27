package cn.mcloli.dreamrealms.modules.itemmanager.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 存储的物品数据
 */
public class StoredItem {

    private final UUID guid;
    @Nullable
    private String identifier;
    @Nullable
    private UUID categoryId;
    private int sortOrder;
    private boolean serialized;
    private ItemStack itemStack;

    public StoredItem(@NotNull UUID guid, @Nullable String identifier, @Nullable UUID categoryId,
                      int sortOrder, boolean serialized, @NotNull ItemStack itemStack) {
        this.guid = guid;
        this.identifier = identifier;
        this.categoryId = categoryId;
        this.sortOrder = sortOrder;
        this.serialized = serialized;
        this.itemStack = itemStack;
    }

    /**
     * 创建新物品
     */
    public static StoredItem create(@NotNull ItemStack itemStack) {
        return new StoredItem(UUID.randomUUID(), null, null, 0, false, itemStack.clone());
    }

    @NotNull
    public UUID getGuid() {
        return guid;
    }

    @Nullable
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(@Nullable String identifier) {
        this.identifier = identifier;
    }

    @Nullable
    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@Nullable UUID categoryId) {
        this.categoryId = categoryId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isSerialized() {
        return serialized;
    }

    public void setSerialized(boolean serialized) {
        this.serialized = serialized;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * 获取显示用的标识 (优先标识名，否则完整 GUID)
     */
    @NotNull
    public String getDisplayIdentifier() {
        return identifier != null ? identifier : guid.toString();
    }

    /**
     * 获取命令用的标识 (优先标识名，否则完整 GUID)
     */
    @NotNull
    public String getCommandIdentifier() {
        return identifier != null ? identifier : guid.toString();
    }
}
