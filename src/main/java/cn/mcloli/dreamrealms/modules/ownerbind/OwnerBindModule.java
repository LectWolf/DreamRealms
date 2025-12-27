package cn.mcloli.dreamrealms.modules.ownerbind;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.command.CommandMain;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.ownerbind.api.OwnerBindEvent;
import cn.mcloli.dreamrealms.modules.ownerbind.api.OwnerBindMarkEvent;
import cn.mcloli.dreamrealms.modules.ownerbind.api.OwnerUnbindEvent;
import cn.mcloli.dreamrealms.modules.ownerbind.command.OwnerBindCommand;
import cn.mcloli.dreamrealms.modules.ownerbind.config.OwnerBindConfig;
import cn.mcloli.dreamrealms.modules.ownerbind.lang.OwnerBindMessages;
import cn.mcloli.dreamrealms.modules.ownerbind.listener.OwnerBindListener;
import cn.mcloli.dreamrealms.modules.ownerbind.listener.GlobalMarketPlusListener;
import cn.mcloli.dreamrealms.modules.ownerbind.listener.QuickShopListener;
import cn.mcloli.dreamrealms.modules.ownerbind.listener.ZAuctionHouseListener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AutoRegister
public class OwnerBindModule extends AbstractModule {

    private OwnerBindConfig config;
    private OwnerBindMessages.Holder lang;
    private OwnerBindCommand command;
    private OwnerBindListener listener;
    private GlobalMarketPlusListener globalMarketPlusListener;
    private QuickShopListener quickShopListener;
    private ZAuctionHouseListener zAuctionHouseListener;

    // NBT Keys
    private NamespacedKey ownerKey;        // 存储绑定玩家名
    private NamespacedKey bindableKey;     // 标记物品可绑定

    public OwnerBindModule(DreamRealms plugin) {
        super(plugin, "ownerbind");
        ownerKey = new NamespacedKey(plugin, "ownerbind_owner");
        bindableKey = new NamespacedKey(plugin, "ownerbind_bindable");
    }

    public static OwnerBindModule inst() {
        return instanceOf(OwnerBindModule.class);
    }

    @Override
    protected String getModuleDescription() {
        return "物主绑定模块 - 物品绑定后禁止转手";
    }

    public OwnerBindConfig getModuleConfig() {
        return config;
    }

    public NamespacedKey getOwnerKey() {
        return ownerKey;
    }

    public NamespacedKey getBindableKey() {
        return bindableKey;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            disableModule();
            return;
        }

        if (lang == null) {
            lang = OwnerBindMessages.register();
        }

        if (config == null) {
            config = new OwnerBindConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        // 注册命令
        if (command == null) {
            command = new OwnerBindCommand(this);
            CommandMain.inst().registerHandler(command);
        }

        enableModule();
        info("模块已加载");
    }

    private void enableModule() {
        if (listener == null) {
            listener = new OwnerBindListener(this);
            registerEvents(listener);
        }

        // GlobalMarketPlus Hook
        if (config.isHookGlobalMarketPlus() && Bukkit.getPluginManager().isPluginEnabled("GlobalMarketPlus")) {
            if (globalMarketPlusListener == null) {
                globalMarketPlusListener = new GlobalMarketPlusListener(this);
                registerEvents(globalMarketPlusListener);
                info("已挂钩 GlobalMarketPlus");
            }
        } else if (globalMarketPlusListener != null) {
            HandlerList.unregisterAll(globalMarketPlusListener);
            globalMarketPlusListener = null;
        }

        // ZAuctionHouse Hook
        if (config.isHookZAuctionHouse() && Bukkit.getPluginManager().isPluginEnabled("zAuctionHouse")) {
            if (zAuctionHouseListener == null) {
                zAuctionHouseListener = new ZAuctionHouseListener(this);
                registerEvents(zAuctionHouseListener);
                info("已挂钩 zAuctionHouse");
            }
        } else if (zAuctionHouseListener != null) {
            HandlerList.unregisterAll(zAuctionHouseListener);
            zAuctionHouseListener = null;
        }

        // QuickShop Hook
        if (config.isHookQuickShop() && Bukkit.getPluginManager().isPluginEnabled("QuickShop-Hikari")) {
            if (quickShopListener == null) {
                quickShopListener = new QuickShopListener(this);
                registerEvents(quickShopListener);
                info("已挂钩 QuickShop-Hikari");
            }
        } else if (quickShopListener != null) {
            HandlerList.unregisterAll(quickShopListener);
            quickShopListener = null;
        }
    }

    private void disableModule() {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
        if (globalMarketPlusListener != null) {
            HandlerList.unregisterAll(globalMarketPlusListener);
            globalMarketPlusListener = null;
        }
        if (zAuctionHouseListener != null) {
            HandlerList.unregisterAll(zAuctionHouseListener);
            zAuctionHouseListener = null;
        }
        if (quickShopListener != null) {
            HandlerList.unregisterAll(quickShopListener);
            quickShopListener = null;
        }
    }

    // ================ 物品状态检查 ================

    /**
     * 检查物品是否为空
     */
    public boolean isEmptyItem(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * 检查物品是否有任何绑定信息 (已绑定或可绑定)
     */
    public boolean hasAnyBindInfo(ItemStack item) {
        return hasBoundOwner(item) || isBindable(item);
    }

    /**
     * 检查物品是否已绑定玩家 (通过NBT)
     */
    public boolean hasBoundOwner(ItemStack item) {
        return getBoundOwner(item) != null;
    }

    /**
     * 获取绑定的玩家名
     */
    public String getBoundOwner(ItemStack item) {
        if (isEmptyItem(item) || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
    }

    /**
     * 检查物品是否可绑定 (通过NBT标记或Lore)
     */
    public boolean isBindable(ItemStack item) {
        if (isEmptyItem(item)) return false;
        
        // 已绑定的不算可绑定
        if (hasBoundOwner(item)) return false;

        // 检查NBT标记
        if (config.isNbtBindEnabled() && hasBindableNbt(item)) {
            return true;
        }

        // 检查Lore
        if (config.isLoreBindEnabled() && hasBindableLore(item)) {
            return true;
        }

        return false;
    }

    /**
     * 检查是否有可绑定NBT标记
     */
    public boolean hasBindableNbt(ItemStack item) {
        if (isEmptyItem(item) || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        var pdc = meta.getPersistentDataContainer();
        
        // 检查默认的 bindableKey
        if (Boolean.TRUE.equals(pdc.get(bindableKey, PersistentDataType.BOOLEAN))) {
            return true;
        }
        
        // 检查自定义 NBT 键
        for (String customKey : config.getNbtCustomKeys()) {
            if (customKey == null || customKey.isEmpty()) continue;
            try {
                String keyPart;
                String expectedValue = null;
                
                // 解析键值对格式 "namespace:key=value"
                if (customKey.contains("=")) {
                    int eqIndex = customKey.indexOf('=');
                    keyPart = customKey.substring(0, eqIndex);
                    expectedValue = customKey.substring(eqIndex + 1);
                } else {
                    keyPart = customKey;
                }
                
                NamespacedKey key;
                if (keyPart.contains(":")) {
                    String[] parts = keyPart.split(":", 2);
                    key = new NamespacedKey(parts[0], parts[1]);
                } else {
                    key = NamespacedKey.minecraft(keyPart);
                }
                
                // 如果只检查键是否存在
                if (expectedValue == null) {
                    if (pdc.has(key)) {
                        debug("检测到自定义 NBT 键: " + customKey);
                        return true;
                    }
                } else {
                    // 检查键值是否匹配
                    if (matchNbtValue(pdc, key, expectedValue)) {
                        debug("检测到自定义 NBT 键值匹配: " + customKey);
                        return true;
                    }
                }
            } catch (Exception e) {
                debug("无效的 NBT 键: " + customKey + " - " + e.getMessage());
            }
        }
        
        return false;
    }

    /**
     * 匹配 NBT 值 (支持字符串、整数、浮点数、布尔值)
     * 优化: 使用 has() 先检查类型再 get()，避免不必要的异常
     */
    private boolean matchNbtValue(org.bukkit.persistence.PersistentDataContainer pdc, NamespacedKey key, String expectedValue) {
        // 按常用程度排序，优先检查字符串和整数
        if (pdc.has(key, PersistentDataType.STRING)) {
            String strValue = pdc.get(key, PersistentDataType.STRING);
            if (strValue != null && strValue.equals(expectedValue)) {
                return true;
            }
        }
        
        if (pdc.has(key, PersistentDataType.INTEGER)) {
            Integer intValue = pdc.get(key, PersistentDataType.INTEGER);
            if (intValue != null && expectedValue.equals(String.valueOf(intValue))) {
                return true;
            }
        }
        
        if (pdc.has(key, PersistentDataType.BOOLEAN)) {
            Boolean boolValue = pdc.get(key, PersistentDataType.BOOLEAN);
            if (boolValue != null && expectedValue.equalsIgnoreCase(String.valueOf(boolValue))) {
                return true;
            }
        }
        
        if (pdc.has(key, PersistentDataType.LONG)) {
            Long longValue = pdc.get(key, PersistentDataType.LONG);
            if (longValue != null && expectedValue.equals(String.valueOf(longValue))) {
                return true;
            }
        }
        
        if (pdc.has(key, PersistentDataType.DOUBLE)) {
            Double doubleValue = pdc.get(key, PersistentDataType.DOUBLE);
            if (doubleValue != null && expectedValue.equals(String.valueOf(doubleValue))) {
                return true;
            }
        }
        
        if (pdc.has(key, PersistentDataType.FLOAT)) {
            Float floatValue = pdc.get(key, PersistentDataType.FLOAT);
            if (floatValue != null && expectedValue.equals(String.valueOf(floatValue))) {
                return true;
            }
        }
        
        if (pdc.has(key, PersistentDataType.BYTE)) {
            Byte byteValue = pdc.get(key, PersistentDataType.BYTE);
            if (byteValue != null && expectedValue.equals(String.valueOf(byteValue))) {
                return true;
            }
        }
        
        if (pdc.has(key, PersistentDataType.SHORT)) {
            Short shortValue = pdc.get(key, PersistentDataType.SHORT);
            if (shortValue != null && expectedValue.equals(String.valueOf(shortValue))) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查是否有可绑定Lore
     * 支持传统颜色代码和 MiniMessage 格式
     */
    public boolean hasBindableLore(ItemStack item) {
        if (isEmptyItem(item) || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return false;

        for (String bindLore : config.getBindableLores()) {
            // 将配置的 pattern 转为纯文本用于匹配
            String targetPlain = toPlainText(bindLore);
            
            for (String line : lore) {
                // 将 lore 行转为纯文本
                String linePlain = toPlainText(line);
                if (linePlain.contains(targetPlain)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 预编译正则表达式，避免重复编译
    private static final java.util.regex.Pattern COLOR_CODE_PATTERN = java.util.regex.Pattern.compile("§[0-9a-fk-orA-FK-OR]");
    private static final java.util.regex.Pattern HEX_COLOR_PATTERN = java.util.regex.Pattern.compile("§x(§[0-9a-fA-F]){6}");

    /**
     * 将文本转换为纯文本 (移除所有格式)
     * 支持传统颜色代码和 MiniMessage 格式
     */
    private String toPlainText(String text) {
        if (text == null || text.isEmpty()) return "";
        
        // 如果包含 MiniMessage 标签，尝试解析
        if (text.contains("<") && text.contains(">")) {
            try {
                var component = MiniMessage.miniMessage().deserialize(text);
                return PlainTextComponentSerializer.plainText().serialize(component);
            } catch (Exception ignored) {
                // 解析失败，使用传统方式
            }
        }
        
        // 移除所有颜色代码 (使用预编译的正则)
        String result = HEX_COLOR_PATTERN.matcher(text).replaceAll("");
        result = COLOR_CODE_PATTERN.matcher(result).replaceAll("");
        return result;
    }

    /**
     * 检查是否有已绑定Lore
     */
    public boolean hasBoundLore(ItemStack item) {
        if (isEmptyItem(item) || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return false;

        String boundPattern = toPlainText(config.getBoundLore().replace("%player%", ""));
        for (String line : lore) {
            if (toPlainText(line).contains(boundPattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查玩家是否是物品的主人
     */
    public boolean isOwner(Player player, ItemStack item) {
        String boundOwner = getBoundOwner(item);
        return boundOwner == null || boundOwner.equals(player.getName());
    }

    // ================ 物品操作 ================

    /**
     * 标记物品为可绑定 (添加NBT和Lore)
     */
    public OwnerBindResult markBindable(ItemStack item) {
        return markBindable(item, OwnerBindMarkEvent.MarkSource.OTHER);
    }

    /**
     * 标记物品为可绑定 (添加NBT和Lore)
     * @param item 物品
     * @param source 标记来源
     */
    public OwnerBindResult markBindable(ItemStack item, OwnerBindMarkEvent.MarkSource source) {
        if (isEmptyItem(item)) return OwnerBindResult.EMPTY_ITEM;
        if (hasBoundOwner(item)) return OwnerBindResult.ALREADY_BOUND;
        if (isBindable(item)) return OwnerBindResult.ALREADY_BOUND; // 已经是可绑定状态

        // 触发事件
        OwnerBindMarkEvent event = new OwnerBindMarkEvent(item, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            debug("标记可绑定被取消 (事件)");
            return OwnerBindResult.INVALID_ITEM;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        }
        if (meta == null) return OwnerBindResult.INVALID_ITEM;

        // 添加NBT标记
        meta.getPersistentDataContainer().set(bindableKey, PersistentDataType.BOOLEAN, true);

        // 添加Lore (使用配置的第一个可绑定Lore)
        List<String> bindableLores = config.getBindableLores();
        if (!bindableLores.isEmpty()) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.getLore())) : new ArrayList<>();
            lore.add(ColorHelper.parseColor(bindableLores.get(0)));
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        debug("物品已标记为可绑定");
        return OwnerBindResult.SUCCESS;
    }

    /**
     * 绑定物品给玩家
     */
    public OwnerBindResult bindToPlayer(ItemStack item, String playerName) {
        return bindToPlayer(item, playerName, OwnerBindEvent.BindSource.OTHER);
    }

    /**
     * 绑定物品给玩家
     * @param item 物品
     * @param playerName 玩家名
     * @param source 绑定来源
     */
    public OwnerBindResult bindToPlayer(ItemStack item, String playerName, OwnerBindEvent.BindSource source) {
        if (isEmptyItem(item)) return OwnerBindResult.EMPTY_ITEM;
        if (hasBoundOwner(item)) return OwnerBindResult.ALREADY_BOUND;

        // 触发事件
        OwnerBindEvent event = new OwnerBindEvent(item, playerName, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            debug("绑定被取消 (事件): " + playerName);
            return OwnerBindResult.INVALID_ITEM;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        }
        if (meta == null) return OwnerBindResult.INVALID_ITEM;

        // 设置NBT
        meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, playerName);
        // 移除可绑定标记
        meta.getPersistentDataContainer().remove(bindableKey);

        // 如果启用了 Lore 修改
        if (config.isLoreEnabled()) {
            // 更新Lore
            List<String> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.getLore())) : new ArrayList<>();
            
            // 移除可绑定Lore，添加已绑定Lore
            String boundLoreLine = ColorHelper.parseColor(config.getBoundLore().replace("%player%", playerName));
            boolean replaced = false;

            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                String linePlain = toPlainText(line);
                for (String bindLore : config.getBindableLores()) {
                    String bindLorePlain = toPlainText(bindLore);
                    if (linePlain.contains(bindLorePlain)) {
                        lore.set(i, boundLoreLine);
                        replaced = true;
                        break;
                    }
                }
                if (replaced) break;
            }

            if (!replaced) {
                lore.add(boundLoreLine);
            }

            meta.setLore(lore);
        }
        
        item.setItemMeta(meta);

        debug("物品已绑定给玩家: " + playerName);
        return OwnerBindResult.SUCCESS;
    }

    /**
     * 解除绑定
     */
    public OwnerBindResult unbind(ItemStack item) {
        return unbind(item, OwnerUnbindEvent.UnbindSource.OTHER);
    }

    /**
     * 解除绑定
     * @param item 物品
     * @param source 解绑来源
     */
    public OwnerBindResult unbind(ItemStack item, OwnerUnbindEvent.UnbindSource source) {
        if (isEmptyItem(item)) return OwnerBindResult.EMPTY_ITEM;
        if (!hasBoundOwner(item) && !isBindable(item)) return OwnerBindResult.NOT_BOUND;

        String previousOwner = getBoundOwner(item);

        // 触发事件
        OwnerUnbindEvent event = new OwnerUnbindEvent(item, previousOwner, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            debug("解绑被取消 (事件)");
            return OwnerBindResult.INVALID_ITEM;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return OwnerBindResult.INVALID_ITEM;

        // 移除NBT
        meta.getPersistentDataContainer().remove(ownerKey);
        meta.getPersistentDataContainer().remove(bindableKey);

        // 如果启用了 Lore 修改，移除绑定相关Lore
        if (config.isLoreEnabled() && meta.hasLore()) {
            List<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
            String boundPattern = toPlainText(config.getBoundLore().replace("%player%", ""));
            
            lore.removeIf(line -> {
                String linePlain = toPlainText(line);
                if (linePlain.contains(boundPattern)) return true;
                for (String bindLore : config.getBindableLores()) {
                    if (linePlain.contains(toPlainText(bindLore))) return true;
                }
                return false;
            });
            
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        debug("物品已解绑" + (previousOwner != null ? ", 原主人: " + previousOwner : ""));
        return OwnerBindResult.SUCCESS;
    }

    /**
     * 修复物品Lore (确保NBT和Lore一致)
     */
    public boolean repairItemLore(ItemStack item) {
        // 如果 Lore 功能关闭，不进行修复
        if (!config.isLoreEnabled() || !config.isRepairMode() || isEmptyItem(item) || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String boundOwner = getBoundOwner(item);
        boolean changed = false;

        // 如果有NBT绑定但没有对应Lore
        if (boundOwner != null && !hasBoundLore(item)) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.getLore())) : new ArrayList<>();
            
            // 移除可绑定Lore
            lore.removeIf(line -> {
                String linePlain = toPlainText(line);
                for (String bindLore : config.getBindableLores()) {
                    if (linePlain.contains(toPlainText(bindLore))) {
                        return true;
                    }
                }
                return false;
            });

            String boundLoreLine = ColorHelper.parseColor(config.getBoundLore().replace("%player%", boundOwner));
            lore.add(boundLoreLine);
            meta.setLore(lore);
            changed = true;
        }

        if (changed) {
            item.setItemMeta(meta);
            debug("修复了物品Lore");
        }
        return changed;
    }
}
