package cn.mcloli.dreamrealms.modules.debug.command;

import cn.mcloli.dreamrealms.command.ISubCommandHandler;
import cn.mcloli.dreamrealms.modules.debug.DebugModule;
import cn.mcloli.dreamrealms.utils.Util;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.*;

public class DebugCommand implements ISubCommandHandler {

    private final DebugModule module;
    private Gson gson;
    private Gson gsonCompact;

    private static final List<String> SUB_COMMANDS = Lists.newArrayList("item", "entity", "block");

    public DebugCommand(DebugModule module) {
        this.module = module;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.gsonCompact = new Gson();
    }

    private Gson getGson() {
        return module.getModuleConfig().isPrettyPrint() ? gson : gsonCompact;
    }

    @Override
    public @NotNull String getName() {
        return "debug";
    }

    @Override
    public @NotNull String[] getAliases() {
        return new String[]{"dbg"};
    }

    @Override
    public @NotNull String getDescription() {
        return "调试工具";
    }

    @Override
    public @Nullable String getPermission() {
        return "dreamrealms.debug";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorHelper.parseColor("&c该命令只能由玩家执行"));
            return true;
        }

        if (args.length == 0) {
            return showHelp(player);
        }

        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "item" -> handleItem(player);
            case "entity" -> handleEntity(player);
            case "block" -> handleBlock(player);
            default -> showHelp(player);
        };
    }

    private boolean handleItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (Util.isAir(item)) {
            player.sendMessage(ColorHelper.parseColor("&c请手持物品"));
            return true;
        }

        JsonObject json = serializeItem(item);
        String jsonStr = getGson().toJson(json);

        sendJsonOutput(player, "物品", jsonStr);

        module.debug("玩家 " + player.getName() + " 查询物品: " + item.getType());
        return true;
    }

    private boolean handleEntity(Player player) {
        int distance = module.getModuleConfig().getRayTraceDistance();
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                distance,
                entity -> entity != player && entity instanceof LivingEntity
        );

        if (result == null || result.getHitEntity() == null) {
            player.sendMessage(ColorHelper.parseColor("&c未找到指向的生物 (距离: " + distance + " 格)"));
            return true;
        }

        Entity entity = result.getHitEntity();
        JsonObject json = serializeEntity(entity);
        String jsonStr = getGson().toJson(json);

        sendJsonOutput(player, "实体", jsonStr);

        module.debug("玩家 " + player.getName() + " 查询实体: " + entity.getType());
        return true;
    }

    private boolean handleBlock(Player player) {
        int distance = module.getModuleConfig().getRayTraceDistance();
        RayTraceResult result = player.rayTraceBlocks(distance, FluidCollisionMode.NEVER);

        if (result == null || result.getHitBlock() == null) {
            player.sendMessage(ColorHelper.parseColor("&c未找到指向的方块 (距离: " + distance + " 格)"));
            return true;
        }

        Block block = result.getHitBlock();
        JsonObject json = serializeBlock(block);
        String jsonStr = getGson().toJson(json);

        sendJsonOutput(player, "方块", jsonStr);

        module.debug("玩家 " + player.getName() + " 查询方块: " + block.getType());
        return true;
    }

    /**
     * 发送 JSON 输出并提供复制功能
     */
    private void sendJsonOutput(Player player, String type, String jsonStr) {
        // 压缩 JSON 用于复制 (单行)
        String compactJson = gsonCompact.toJson(gsonCompact.fromJson(jsonStr, JsonObject.class));
        // 转义单引号用于 MiniMessage
        String escapedJson = compactJson.replace("'", "\\'");

        player.sendMessage(ColorHelper.parseColor("&a===== " + type + "序列化信息 ====="));
        player.sendMessage(jsonStr);
        player.sendMessage(ColorHelper.parseColor("&a========================"));

        // 发送可点击的复制按钮
        String copyMessage = "<green><click:copy_to_clipboard:'" + escapedJson + "'><hover:show_text:'<gray>点击复制到剪贴板'>[复制JSON]</hover></click></green>";
        AdventureUtil.sendMessage(player, copyMessage);
    }

    private JsonObject serializeItem(ItemStack item) {
        JsonObject json = new JsonObject();
        json.addProperty("type", item.getType().name());
        json.addProperty("amount", item.getAmount());

        if (item.hasItemMeta()) {
            var meta = item.getItemMeta();
            JsonObject metaJson = new JsonObject();

            if (meta.hasDisplayName()) {
                metaJson.addProperty("displayName", meta.getDisplayName());
            }
            if (meta.hasLore()) {
                metaJson.add("lore", gson.toJsonTree(meta.getLore()));
            }
            if (meta.hasCustomModelData()) {
                metaJson.addProperty("customModelData", meta.getCustomModelData());
            }
            if (meta.hasEnchants()) {
                JsonObject enchants = new JsonObject();
                meta.getEnchants().forEach((ench, level) ->
                        enchants.addProperty(ench.getKey().getKey(), level));
                metaJson.add("enchants", enchants);
            }
            if (!meta.getItemFlags().isEmpty()) {
                metaJson.add("flags", gson.toJsonTree(
                        meta.getItemFlags().stream().map(Enum::name).toList()));
            }
            if (meta.isUnbreakable()) {
                metaJson.addProperty("unbreakable", true);
            }

            json.add("meta", metaJson);
        }

        // 添加 Bukkit 原生序列化
        json.add("bukkit", gson.toJsonTree(item.serialize()));

        return json;
    }

    private JsonObject serializeEntity(Entity entity) {
        JsonObject json = new JsonObject();
        json.addProperty("type", entity.getType().name());
        json.addProperty("uuid", entity.getUniqueId().toString());
        json.addProperty("customName", entity.getCustomName());

        Location loc = entity.getLocation();
        JsonObject locJson = new JsonObject();
        locJson.addProperty("world", loc.getWorld().getName());
        locJson.addProperty("x", loc.getX());
        locJson.addProperty("y", loc.getY());
        locJson.addProperty("z", loc.getZ());
        locJson.addProperty("yaw", loc.getYaw());
        locJson.addProperty("pitch", loc.getPitch());
        json.add("location", locJson);

        if (entity instanceof LivingEntity living) {
            JsonObject livingJson = new JsonObject();
            livingJson.addProperty("health", living.getHealth());
            livingJson.addProperty("maxHealth", living.getMaxHealth());
            livingJson.addProperty("ai", living.hasAI());
            json.add("living", livingJson);
        }

        return json;
    }

    private JsonObject serializeBlock(Block block) {
        JsonObject json = new JsonObject();
        json.addProperty("type", block.getType().name());

        Location loc = block.getLocation();
        JsonObject locJson = new JsonObject();
        locJson.addProperty("world", loc.getWorld().getName());
        locJson.addProperty("x", loc.getBlockX());
        locJson.addProperty("y", loc.getBlockY());
        locJson.addProperty("z", loc.getBlockZ());
        json.add("location", locJson);

        // 方块数据
        json.addProperty("blockData", block.getBlockData().getAsString());

        // 如果是容器等特殊方块，添加额外信息
        var state = block.getState();
        json.addProperty("stateType", state.getClass().getSimpleName());

        return json;
    }

    private boolean showHelp(Player player) {
        player.sendMessage(ColorHelper.parseColor("&6===== Debug 命令帮助 ====="));
        player.sendMessage(ColorHelper.parseColor("&b/dr debug item &7- 获取手持物品序列化信息"));
        player.sendMessage(ColorHelper.parseColor("&b/dr debug entity &7- 获取指向生物序列化信息"));
        player.sendMessage(ColorHelper.parseColor("&b/dr debug block &7- 获取指向方块序列化信息"));
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(SUB_COMMANDS, args[0]);
        }
        return Collections.emptyList();
    }

    private List<String> startsWith(Collection<String> list, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }
}
