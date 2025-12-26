package cn.mcloli.dreamrealms.lang;

import cn.mcloli.dreamrealms.DreamRealms;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 物品语言管理器
 * 从 Minecraft 官方资源获取物品/方块的本地化名称
 */
public class ItemLanguage {

    private static final Gson GSON = new Gson();
    private static final String ASSETS_URL = "https://resources.download.minecraft.net/";
    private static final String VERSIONS_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private static ItemLanguage instance;

    private final DreamRealms plugin;
    private final Map<String, String> langMap = new HashMap<>();
    private String currentLang = "zh_cn";
    private boolean loaded = false;

    public ItemLanguage(DreamRealms plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static ItemLanguage getInstance() {
        return instance;
    }

    /**
     * 加载语言文件
     * @param lang 语言代码 (如 zh_cn, en_us)
     */
    public void load(String lang) {
        this.currentLang = lang.toLowerCase();
        File langFile = new File(plugin.getDataFolder(), "lang/" + currentLang + ".json");

        // 如果文件不存在，尝试下载
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            plugin.info("正在下载语言文件: " + currentLang + "...");
            if (!downloadLangFile(langFile)) {
                plugin.warn("无法下载语言文件，将使用英文名称");
                return;
            }
        }

        // 加载语言文件
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(langFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> map = GSON.fromJson(reader, type);
            if (map != null) {
                langMap.clear();
                langMap.putAll(map);
                loaded = true;
                plugin.info("已加载语言文件: " + currentLang + " (" + langMap.size() + " 条)");
            }
        } catch (Exception e) {
            plugin.warn("加载语言文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取物品的本地化名称
     */
    @NotNull
    public String getItemName(@NotNull ItemStack item) {
        // 优先使用自定义名称
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }

        // 使用语言文件
        return getItemName(item.getType());
    }

    /**
     * 获取材质的本地化名称
     */
    @NotNull
    public String getItemName(@NotNull Material material) {
        if (!loaded) {
            return formatMaterialName(material);
        }

        // 尝试物品键
        String itemKey = "item.minecraft." + material.name().toLowerCase();
        String name = langMap.get(itemKey);
        if (name != null) return name;

        // 尝试方块键
        String blockKey = "block.minecraft." + material.name().toLowerCase();
        name = langMap.get(blockKey);
        if (name != null) return name;

        // 回退到格式化名称
        return formatMaterialName(material);
    }

    /**
     * 获取翻译键的值
     */
    @Nullable
    public String get(String key) {
        return langMap.get(key);
    }

    /**
     * 获取翻译键的值，带默认值
     */
    @NotNull
    public String get(String key, String defaultValue) {
        return langMap.getOrDefault(key, defaultValue);
    }

    /**
     * 格式化材质名称 (DIAMOND_SWORD -> Diamond Sword)
     */
    @NotNull
    private String formatMaterialName(@NotNull Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getCurrentLang() {
        return currentLang;
    }


    /**
     * 下载语言文件
     */
    private boolean downloadLangFile(File destination) {
        try {
            // 获取版本清单
            String mcVersion = getMcVersion();
            plugin.info("检测到 MC 版本: " + mcVersion);

            // 获取版本信息
            Map<String, Object> versionManifest = downloadJson(VERSIONS_URL);
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, String>> versions = 
                (java.util.List<Map<String, String>>) versionManifest.get("versions");

            String versionUrl = null;
            for (Map<String, String> v : versions) {
                if (mcVersion.equals(v.get("id"))) {
                    versionUrl = v.get("url");
                    break;
                }
            }

            if (versionUrl == null) {
                plugin.warn("找不到版本: " + mcVersion);
                return false;
            }

            // 获取资源索引
            Map<String, Object> versionInfo = downloadJson(versionUrl);
            @SuppressWarnings("unchecked")
            Map<String, String> assetIndex = (Map<String, String>) versionInfo.get("assetIndex");
            String assetUrl = assetIndex.get("url");

            // 获取语言文件哈希
            Map<String, Object> assets = downloadJson(assetUrl);
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> objects = 
                (Map<String, Map<String, String>>) assets.get("objects");

            String langPath = "minecraft/lang/" + currentLang + ".json";
            Map<String, String> langAsset = objects.get(langPath);

            if (langAsset == null) {
                plugin.warn("找不到语言文件: " + langPath);
                return false;
            }

            String hash = langAsset.get("hash");
            String downloadUrl = ASSETS_URL + hash.substring(0, 2) + "/" + hash;

            // 下载文件
            plugin.info("下载: " + downloadUrl);
            downloadFile(downloadUrl, destination);
            plugin.info("语言文件下载完成");
            return true;

        } catch (Exception e) {
            plugin.warn("下载语言文件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取当前 MC 版本
     */
    private String getMcVersion() {
        String version = org.bukkit.Bukkit.getBukkitVersion();
        // 格式: 1.21.8-R0.1-SNAPSHOT -> 1.21.8
        int idx = version.indexOf('-');
        return idx > 0 ? version.substring(0, idx) : version;
    }

    /**
     * 下载 JSON
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> downloadJson(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "DreamRealms/1.0");

        try (InputStreamReader reader = new InputStreamReader(
                conn.getInputStream(), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, Map.class);
        }
    }

    /**
     * 下载文件
     */
    private void downloadFile(String url, File destination) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "DreamRealms/1.0");

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
