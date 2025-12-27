package cn.mcloli.dreamrealms.modules.dogtag.config;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.modules.dogtag.DogTagModule;
import cn.mcloli.dreamrealms.modules.dogtag.data.DogTagData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class DogTagConfig {

    private final DreamRealms plugin;
    private final DogTagModule module;
    private final File configFile;
    private YamlConfiguration config;

    private boolean debug;
    private boolean pvpOnly;
    private String dateFormat;
    private List<String> bypassPermissions;
    private final Map<String, DogTagData> dogTags = new LinkedHashMap<>();
    private final List<DogTagData> sortedDogTags = new ArrayList<>();

    public DogTagConfig(DreamRealms plugin, DogTagModule module) {
        this.plugin = plugin;
        this.module = module;
        this.configFile = module.getModuleConfigFile("settings.yml");
    }

    public void reload() {
        if (!configFile.exists()) {
            module.saveModuleResource("settings.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        debug = config.getBoolean("debug", false);
        pvpOnly = config.getBoolean("pvp-only", true);
        dateFormat = config.getString("date-format", "yyyy-MM-dd HH:mm:ss");
        bypassPermissions = config.getStringList("bypass-permissions");

        // 加载狗牌配置
        dogTags.clear();
        sortedDogTags.clear();
        ConfigurationSection tagsSection = config.getConfigurationSection("dogtags");
        if (tagsSection != null) {
            for (String key : tagsSection.getKeys(false)) {
                ConfigurationSection tagSection = tagsSection.getConfigurationSection(key);
                if (tagSection != null) {
                    DogTagData data = DogTagData.load(key, tagSection);
                    dogTags.put(key, data);
                    sortedDogTags.add(data);
                }
            }
        }

        // 按优先级排序 (高优先级在前)
        sortedDogTags.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isPvpOnly() {
        return pvpOnly;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public List<String> getBypassPermissions() {
        return bypassPermissions;
    }

    public Map<String, DogTagData> getDogTags() {
        return dogTags;
    }

    /**
     * 获取按优先级排序的狗牌列表
     */
    public List<DogTagData> getSortedDogTags() {
        return sortedDogTags;
    }
}
