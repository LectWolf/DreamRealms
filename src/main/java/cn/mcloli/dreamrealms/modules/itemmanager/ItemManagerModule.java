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
import cn.mcloli.dreamrealms.modules.itemmanager.menu.CategoryMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.ItemEditMenuConfig;
import cn.mcloli.dreamrealms.modules.itemmanager.menu.ItemListMenuConfig;
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

        // 注册命令到 /dr itemmanager
        if (command == null) {
            command = new ItemManagerCommand(this);
            CommandMain.inst().registerHandler(command);
        }

        // 注册独立命令 /itemmanager 和 /im (动态注册，无需 plugin.yml)
        if (commandManager == null) {
            commandManager = ItemManagerCommands.create(this);
            cn.mcloli.dreamrealms.utils.CommandRegister.register(
                    "itemmanager",
                    new String[]{"im"},
                    "物品管理器",
                    commandManager
            );
        }

        info("模块已加载");
    }
}
