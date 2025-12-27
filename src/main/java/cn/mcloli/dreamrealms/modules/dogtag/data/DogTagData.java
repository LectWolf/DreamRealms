package cn.mcloli.dreamrealms.modules.dogtag.data;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 狗牌配置数据
 */
public class DogTagData {

    private final String id;
    private final String permission;
    private final int priority;
    private final String material;
    @Nullable
    private final Integer customModelData;
    private final String displayName;
    private final List<String> lore;

    public DogTagData(String id, String permission, int priority, String material,
                      @Nullable Integer customModelData, String displayName, List<String> lore) {
        this.id = id;
        this.permission = permission;
        this.priority = priority;
        this.material = material;
        this.customModelData = customModelData;
        this.displayName = displayName;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public String getPermission() {
        return permission;
    }

    public int getPriority() {
        return priority;
    }

    public String getMaterial() {
        return material;
    }

    @Nullable
    public Integer getCustomModelData() {
        return customModelData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    /**
     * 从配置加载
     */
    public static DogTagData load(String id, ConfigurationSection section) {
        String permission = section.getString("permission", "dreamrealms.dogtag." + id);
        int priority = section.getInt("priority", 0);
        String material = section.getString("material", "NAME_TAG");
        Integer customModelData = section.contains("custom-model-data")
                ? section.getInt("custom-model-data") : null;
        String displayName = section.getString("display", "&c{victim} 的狗牌");
        List<String> lore = section.getStringList("lore");

        return new DogTagData(id, permission, priority, material, customModelData, displayName, lore);
    }
}
