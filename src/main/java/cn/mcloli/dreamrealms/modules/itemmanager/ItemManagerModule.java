package cn.mcloli.dreamrealms.modules.itemmanager;

import cn.mcloli.dreamrealms.DreamRealms;
import cn.mcloli.dreamrealms.command.CommandMain;
import cn.mcloli.dreamrealms.command.ModuleCommandManager;
import cn.mcloli.dreamrealms.func.AbstractModule;
import cn.mcloli.dreamrealms.modules.itemmanager.command.ItemManagerCommand;
import cn.mcloli.dreamrealms.modules.itemmanager.command.ItemManagerCommands;
import cn.mcloli.dreamrealms.modules.itemmanager.config.ItemManagerConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.database.ItemManagerDatabase;
import cn.mcloli.dreamrealms.modules.itemmanager.lang.ItemManagerMessages;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.AttributeDetailMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.AttributeEditMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.AttributeSelectMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.CategoryMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.EnchantEditMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.EnchantSelectMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.FlagEditMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.FoodEditMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.FoodEffectDetailMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.FoodEffectEditMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.FoodEffectSelectMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.ItemEditMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.ItemListMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.ItemPropertiesMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.LoreEditMenuConfig;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;

@AutoRegister
public class ItemManagerModule extends AbstractModule {

    private ItemManagerConfig config;
    private ItemManagerDatabase database;
    private ItemManagerCommand command;
    private ModuleCommandManager commandManager;
    private ItemManagerMessages.Holder lang;

    // 菜单配置
    private CategoryMenuConfig categoryMenuConfig;
    private ItemListMenuConfig itemListMenuConfig;
    private ItemEditMenuConfig itemEditMenuConfig;
    private LoreEditMenuConfig loreEditMenuConfig;
    private EnchantEditMenuConfig enchantEditMenuConfig;
    private EnchantSelectMenuConfig enchantSelectMenuConfig;
    private AttributeEditMenuConfig attributeEditMenuConfig;
    private AttributeDetailMenuConfig attributeDetailMenuConfig;
    private AttributeSelectMenuConfig attributeSelectMenuConfig;
    private FlagEditMenuConfig flagEditMenuConfig;
    private ItemPropertiesMenuConfig itemPropertiesMenuConfig;
    private FoodEditMenuConfig foodEditMenuConfig;
    private FoodEffectEditMenuConfig foodEffectEditMenuConfig;
    private FoodEffectSelectMenuConfig foodEffectSelectMenuConfig;
    private FoodEffectDetailMenuConfig foodEffectDetailMenuConfig;

    public ItemManagerModule(DreamRealms plugin) {
        super(plugin, "itemmanager");
    }

    public static ItemManagerModule inst() {
        return instanceOf(ItemManagerModule.class);
    }

    @Override
    protected String getModuleDescription() {
        return "物品管理器 - 存储、分类、编辑和分发物品";
    }

    public ItemManagerConfig getModuleConfig() {
        return config;
    }

    public ItemManagerDatabase getDatabase() {
        return database;
    }

    public CategoryMenuConfig getCategoryMenuConfig() {
        return categoryMenuConfig;
    }

    public ItemListMenuConfig getItemListMenuConfig() {
        return itemListMenuConfig;
    }

    public ItemEditMenuConfig getItemEditMenuConfig() {
        return itemEditMenuConfig;
    }

    public LoreEditMenuConfig getLoreEditMenuConfig() {
        return loreEditMenuConfig;
    }

    public EnchantEditMenuConfig getEnchantEditMenuConfig() {
        return enchantEditMenuConfig;
    }

    public EnchantSelectMenuConfig getEnchantSelectMenuConfig() {
        return enchantSelectMenuConfig;
    }

    public AttributeEditMenuConfig getAttributeEditMenuConfig() {
        return attributeEditMenuConfig;
    }

    public AttributeDetailMenuConfig getAttributeDetailMenuConfig() {
        return attributeDetailMenuConfig;
    }

    public AttributeSelectMenuConfig getAttributeSelectMenuConfig() {
        return attributeSelectMenuConfig;
    }

    public FlagEditMenuConfig getFlagEditMenuConfig() {
        return flagEditMenuConfig;
    }

    public ItemPropertiesMenuConfig getItemPropertiesMenuConfig() {
        return itemPropertiesMenuConfig;
    }

    public FoodEditMenuConfig getFoodEditMenuConfig() {
        return foodEditMenuConfig;
    }

    public FoodEffectEditMenuConfig getFoodEffectEditMenuConfig() {
        return foodEffectEditMenuConfig;
    }

    public FoodEffectSelectMenuConfig getFoodEffectSelectMenuConfig() {
        return foodEffectSelectMenuConfig;
    }

    public FoodEffectDetailMenuConfig getFoodEffectDetailMenuConfig() {
        return foodEffectDetailMenuConfig;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!checkModuleEnabled(cfg)) {
            info("模块已禁用");
            return;
        }

        // 注册语言
        if (lang == null) {
            lang = ItemManagerMessages.register();
        }

        // 初始化配置
        if (config == null) {
            config = new ItemManagerConfig(plugin, this);
        }
        config.reload();
        setDebug(config.isDebug());

        // 初始化数据库
        if (database == null) {
            database = new ItemManagerDatabase(plugin, this);
            plugin.options.registerDatabase(database);
        }

        // 初始化菜单配置
        if (categoryMenuConfig == null) {
            categoryMenuConfig = new CategoryMenuConfig(plugin, this);
        }
        categoryMenuConfig.reloadConfig(cfg);

        if (itemListMenuConfig == null) {
            itemListMenuConfig = new ItemListMenuConfig(plugin, this);
        }
        itemListMenuConfig.reloadConfig(cfg);

        if (itemEditMenuConfig == null) {
            itemEditMenuConfig = new ItemEditMenuConfig(plugin, this);
        }
        itemEditMenuConfig.reloadConfig(cfg);

        if (loreEditMenuConfig == null) {
            loreEditMenuConfig = new LoreEditMenuConfig(plugin, this);
        }
        loreEditMenuConfig.reloadConfig(cfg);

        if (enchantEditMenuConfig == null) {
            enchantEditMenuConfig = new EnchantEditMenuConfig(plugin, this);
        }
        enchantEditMenuConfig.reloadConfig(cfg);

        if (enchantSelectMenuConfig == null) {
            enchantSelectMenuConfig = new EnchantSelectMenuConfig(plugin, this);
        }
        enchantSelectMenuConfig.reloadConfig(cfg);

        if (attributeEditMenuConfig == null) {
            attributeEditMenuConfig = new AttributeEditMenuConfig(plugin, this);
        }
        attributeEditMenuConfig.reloadConfig(cfg);

        if (attributeDetailMenuConfig == null) {
            attributeDetailMenuConfig = new AttributeDetailMenuConfig(plugin, this);
        }
        attributeDetailMenuConfig.reloadConfig(cfg);

        if (attributeSelectMenuConfig == null) {
            attributeSelectMenuConfig = new AttributeSelectMenuConfig(plugin, this);
        }
        attributeSelectMenuConfig.reloadConfig(cfg);

        if (flagEditMenuConfig == null) {
            flagEditMenuConfig = new FlagEditMenuConfig(plugin, this);
        }
        flagEditMenuConfig.reloadConfig(cfg);

        if (itemPropertiesMenuConfig == null) {
            itemPropertiesMenuConfig = new ItemPropertiesMenuConfig(plugin, this);
        }
        itemPropertiesMenuConfig.reloadConfig(cfg);

        if (foodEditMenuConfig == null) {
            foodEditMenuConfig = new FoodEditMenuConfig(plugin, this);
        }
        foodEditMenuConfig.reloadConfig(cfg);

        if (foodEffectEditMenuConfig == null) {
            foodEffectEditMenuConfig = new FoodEffectEditMenuConfig(plugin, this);
        }
        foodEffectEditMenuConfig.reloadConfig(cfg);

        if (foodEffectSelectMenuConfig == null) {
            foodEffectSelectMenuConfig = new FoodEffectSelectMenuConfig(plugin, this);
        }
        foodEffectSelectMenuConfig.reloadConfig(cfg);

        if (foodEffectDetailMenuConfig == null) {
            foodEffectDetailMenuConfig = new FoodEffectDetailMenuConfig(plugin, this);
        }
        foodEffectDetailMenuConfig.reloadConfig(cfg);

        // 注册命令到 /dr itemmanager
        if (command == null) {
            command = new ItemManagerCommand(this);
            CommandMain.inst().registerHandler(command);
        }

        // 注册独立命令 /itemmanager 和 /im (动态注册，无需 plugin.yml)
        // 每次重载都重新注册命令，确保命令处理器指向当前模块实例
        commandManager = ItemManagerCommands.create(this);
        // 先注销可能存在的旧命令
        cn.mcloli.dreamrealms.utils.CommandRegister.unregister("itemmanager");
        cn.mcloli.dreamrealms.utils.CommandRegister.register(
                "itemmanager",
                new String[]{"im"},
                "物品管理器",
                commandManager
        );

        info("模块已加载");
    }
}
