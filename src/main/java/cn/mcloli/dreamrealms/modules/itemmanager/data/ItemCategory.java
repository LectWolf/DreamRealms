package cn.mcloli.dreamrealms.modules.itemmanager.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 物品分类
 */
public class ItemCategory {

    private final UUID id;
    private String name;
    @Nullable
    private String icon;
    private int sortOrder;

    public ItemCategory(@NotNull UUID id, @NotNull String name, @Nullable String icon, int sortOrder) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.sortOrder = sortOrder;
    }

    /**
     * 创建新分类
     */
    public static ItemCategory create(@NotNull String name) {
        return new ItemCategory(UUID.randomUUID(), name, null, 0);
    }

    @NotNull
    public UUID getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Nullable
    public String getIcon() {
        return icon;
    }

    public void setIcon(@Nullable String icon) {
        this.icon = icon;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
