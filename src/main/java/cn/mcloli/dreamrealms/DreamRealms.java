package cn.mcloli.dreamrealms;

import cn.mcloli.dreamrealms.hook.CraftEngineHook;
import cn.mcloli.dreamrealms.hook.ItemsAdderHook;
import cn.mcloli.dreamrealms.hook.PAPI;
import cn.mcloli.dreamrealms.lang.ItemLanguage;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.economy.EnumEconomy;
import top.mrxiaom.pluginbase.economy.IEconomy;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class DreamRealms extends BukkitPlugin {

    private ItemLanguage itemLanguage;

    public static DreamRealms getInstance() {
        return (DreamRealms) BukkitPlugin.getInstance();
    }

    public DreamRealms() throws Exception {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(true)
                .economy(EnumEconomy.VAULT)
                .scanIgnore("cn.mcloli.dreamrealms.libs")
        );
        this.scheduler = new FoliaLibScheduler(this);

        info("正在检查依赖库状态");
        File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                ? new File("libraries")
                : new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

        resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);

        List<URL> libraries = resolver.doResolve();
        info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            this.classLoader.addURL(library);
        }
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
    }

    @NotNull
    public IEconomy getEconomy() {
        return options.economy();
    }

    public ItemLanguage getItemLanguage() {
        return itemLanguage;
    }

    @Override
    protected void beforeEnable() {
        // 初始化 Hook
        CraftEngineHook.init();
        ItemsAdderHook.init();
        PAPI.init();

        // 初始化物品语言
        itemLanguage = new ItemLanguage(this);

        // 注册语言系统
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Messages.class, Messages::holder);

        options.registerDatabase(
                // 在这里添加数据库 (如果需要的话)
        );
    }

    @Override
    protected void beforeReloadConfig(FileConfiguration config) {
        // 加载物品语言
        String lang = config.getString("item-language", "zh_cn");
        if (itemLanguage != null) {
            itemLanguage.load(lang);
        }
    }

    @Override
    protected void afterEnable() {
        getLogger().info("DreamRealms 加载完毕");
    }
}
