package cn.mcloli.dreamrealms.gui;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.func.AbstractPluginHolder;
import cn.mcloli.dreamrealms.hook.CraftEngineHook;
import cn.mcloli.dreamrealms.hook.PAPI;
import cn.mcloli.dreamrealms.utils.SkullUtil;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

import java.io.File;
import java.util.*;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;
import static top.mrxiaom.pluginbase.actions.ActionProviders.run;

/**
 * GUI 配置基类
 * @param <T> GUI 实例类型
 */
@SuppressWarnings("unused")
public abstract class AbstractMenuConfig<T extends IGui> extends AbstractPluginHolder {

    /**
     * 图标数据类
     */
    public static class Icon {
        public final String material;
        public final boolean glow;
        @Nullable
        public final String display;
        @Nullable
        public final Integer customModelData;
        public final List<String> lore;
        @Nullable
        public final String skullOwner;
        @Nullable
        public final String skullTexture;

        // 点击命令
        List<IAction> leftClick = null;
        List<IAction> rightClick = null;
        List<IAction> shiftLeftClick = null;
        List<IAction> shiftRightClick = null;
        List<IAction> dropClick = null;

        public Icon(String material, boolean glow, @Nullable String display,
                    @Nullable Integer customModelData, List<String> lore,
                    @Nullable String skullOwner, @Nullable String skullTexture) {
            this.material = material;
            this.glow = glow;
            this.display = display;
            this.customModelData = customModelData;
            this.lore = lore;
            this.skullOwner = skullOwner;
            this.skullTexture = skullTexture;
        }

        /**
         * 生成物品
         */
        @SafeVarargs
        public final ItemStack generateIcon(Player player, Pair<String, Object>... replacements) {
            ItemStack item = parseItem(material, skullOwner, skullTexture, player);
            if (item == null || item.getType() == Material.AIR) {
                return new ItemStack(Material.AIR);
            }

            // 应用显示名
            if (display != null) {
                String name = replace(display, replacements);
                name = setPlaceholders(player, name);
                ItemStackUtil.setItemDisplayName(item, name);
            }

            // 应用 Lore
            if (!lore.isEmpty()) {
                List<String> loreList = replace(lore, replacements);
                loreList = setPlaceholders(player, loreList);
                ItemStackUtil.setItemLore(item, loreList);
            }

            // 发光
            if (glow) {
                ItemStackUtil.setGlow(item);
            }

            // CustomModelData
            if (customModelData != null) {
                ItemStackUtil.setCustomModelData(item, customModelData);
            }

            return item;
        }

        /**
         * 处理点击
         */
        public void click(Player player, ClickType type) {
            List<IAction> commands = switch (type) {
                case LEFT -> leftClick;
                case RIGHT -> rightClick;
                case SHIFT_LEFT -> shiftLeftClick;
                case SHIFT_RIGHT -> shiftRightClick;
                case DROP -> dropClick;
                default -> null;
            };
            if (commands != null && !commands.isEmpty()) {
                run(DreamRealms.getInstance(), player, commands);
            }
        }

        /**
         * 从配置加载图标
         */
        public static Icon load(ConfigurationSection section, String key, boolean loadCommands) {
            String material = section.getString(key + ".material", "STONE");
            boolean glow = section.getBoolean(key + ".glow", false);
            String display = section.getString(key + ".display", null);
            Integer customModelData = section.contains(key + ".custom-model-data")
                    ? section.getInt(key + ".custom-model-data") : null;
            List<String> lore = section.getStringList(key + ".lore");
            String skullOwner = section.getString(key + ".skull-owner", null);
            String skullTexture = section.getString(key + ".skull-texture", null);

            Icon icon = new Icon(material, glow, display, customModelData, lore, skullOwner, skullTexture);

            if (loadCommands) {
                icon.leftClick = loadActions(section, key + ".left-click-commands");
                icon.rightClick = loadActions(section, key + ".right-click-commands");
                icon.shiftLeftClick = loadActions(section, key + ".shift-left-click-commands");
                icon.shiftRightClick = loadActions(section, key + ".shift-right-click-commands");
                icon.dropClick = loadActions(section, key + ".drop-commands");
            }
            return icon;
        }

        /**
         * 解析物品
         */
        @Nullable
        private static ItemStack parseItem(String material, @Nullable String skullOwner,
                                           @Nullable String skullTexture, @Nullable Player player) {
            // CraftEngine 物品
            if (material.startsWith("craftengine:")) {
                return CraftEngineHook.getItem(material);
            }

            // 标准物品
            Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(material);
            if (pair == null) {
                return new ItemStack(Material.PAPER);
            }
            ItemStack item = ItemStackUtil.legacy(pair);

            // 头颅处理
            if (SkullUtil.isSkull(item)) {
                if (skullOwner != null) {
                    String owner = player != null ? setPlaceholders(player, skullOwner) : skullOwner;
                    SkullUtil.setOwner(item, owner);
                } else if (skullTexture != null) {
                    SkullUtil.setTexture(item, skullTexture);
                }
            }

            return item;
        }

        @SafeVarargs
        private static String replace(String str, Pair<String, Object>... replacements) {
            for (Pair<String, Object> pair : replacements) {
                str = str.replace(pair.getKey(), String.valueOf(pair.getValue()));
            }
            return str;
        }

        @SafeVarargs
        private static List<String> replace(List<String> list, Pair<String, Object>... replacements) {
            List<String> result = new ArrayList<>();
            for (String str : list) {
                result.add(replace(str, replacements));
            }
            return result;
        }

        private static String setPlaceholders(@Nullable OfflinePlayer player, String str) {
            return PAPI.setPlaceholders(player, str);
        }

        private static List<String> setPlaceholders(@Nullable OfflinePlayer player, List<String> list) {
            return PAPI.setPlaceholders(player, list);
        }
    }

    // 配置文件
    protected final File configFile;
    protected final String resourcePath;
    protected YamlConfiguration config;

    // 界面配置
    protected String title;
    protected char[] inventory;
    protected final Map<String, Icon> otherIcons = new HashMap<>();

    // 可交互格子
    protected final Set<Character> interactiveChars = new HashSet<>();

    public AbstractMenuConfig(DreamRealms plugin, String resourcePath) {
        super(plugin);
        this.resourcePath = resourcePath;
        this.configFile = new File(plugin.getDataFolder(), resourcePath);
        this.register();
    }

    /**
     * 标记可交互格子
     */
    protected void markInteractive(char... chars) {
        for (char c : chars) {
            interactiveChars.add(c);
        }
    }

    /**
     * 检查是否可交互格子
     */
    public boolean isInteractiveSlot(int slot) {
        if (slot < 0 || slot >= inventory.length) return false;
        return interactiveChars.contains(inventory[slot]);
    }

    /**
     * 获取格子对应的字符
     */
    @Nullable
    public Character getSlotKey(int slot) {
        if (slot < 0 || slot >= inventory.length) return null;
        return inventory[slot];
    }

    /**
     * 获取字符在界面中第几次出现
     */
    public int getKeyIndex(char key, int slot) {
        int index = 0;
        for (int i = 0; i < slot && i < inventory.length; i++) {
            if (inventory[i] == key) index++;
        }
        return index;
    }

    /**
     * 创建物品栏
     */
    @SuppressWarnings("unchecked")
    public Inventory createInventory(T gui, Player player) {
        String titleStr = Icon.setPlaceholders(player, title);
        return plugin.createInventory(gui, inventory.length, titleStr);
    }

    /**
     * 应用图标到物品栏
     */
    public void applyIcons(T gui, Inventory inv, Player player) {
        for (int i = 0; i < inventory.length; i++) {
            applyIcon(gui, inv, player, i);
        }
    }

    /**
     * 应用单个图标
     */
    public void applyIcon(T gui, Inventory inv, Player player, int slot) {
        if (slot >= inventory.length) return;
        char c = inventory[slot];
        String key = String.valueOf(c);

        if (key.equals(" ") || key.equals("　")) {
            inv.setItem(slot, null);
            return;
        }

        int index = getKeyIndex(c, slot);

        // 尝试主图标
        ItemStack item = tryApplyMainIcon(gui, key, player, index);
        if (item != null) {
            inv.setItem(slot, item);
            return;
        }

        // 其他图标
        Icon icon = otherIcons.get(key);
        if (icon != null) {
            inv.setItem(slot, icon.generateIcon(player));
        } else {
            inv.setItem(slot, null);
        }
    }

    /**
     * 处理其他图标点击
     */
    public void handleOtherIconClick(Player player, ClickType click, char key) {
        Icon icon = otherIcons.get(String.valueOf(key));
        if (icon != null) {
            icon.click(player, click);
        }
    }

    /**
     * 子类实现：清空主图标
     */
    protected abstract void clearMainIcons();

    /**
     * 子类实现：加载主图标
     */
    protected abstract boolean loadMainIcon(ConfigurationSection section, String key, Icon icon);

    /**
     * 子类实现：生成主图标
     */
    @Nullable
    protected abstract ItemStack tryApplyMainIcon(T gui, String key, Player player, int iconIndex);

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!configFile.exists()) {
            plugin.saveResource(resourcePath, configFile);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        title = config.getString("title", "菜单");
        inventory = String.join("", config.getStringList("inventory")).toCharArray();

        // 加载主图标
        clearMainIcons();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                Icon icon = Icon.load(itemsSection, key, false);
                loadMainIcon(itemsSection, key, icon);
            }
        }

        // 加载其他图标
        otherIcons.clear();
        ConfigurationSection otherSection = config.getConfigurationSection("other-items");
        if (otherSection != null) {
            for (String key : otherSection.getKeys(false)) {
                Icon icon = Icon.load(otherSection, key, true);
                otherIcons.put(key, icon);
            }
        }
    }
}
