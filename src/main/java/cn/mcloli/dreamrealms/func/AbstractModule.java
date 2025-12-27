package cn.mcloli.dreamrealms.func;

import cn.mcloli.dreamrealms.DreamRealms;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * 模块基类
 */
public abstract class AbstractModule extends top.mrxiaom.pluginbase.func.AbstractModule<DreamRealms> {

    private final String moduleId;
    private boolean enabled = true;
    private boolean debug = false;

    public AbstractModule(DreamRealms plugin) {
        super(plugin);
        // 从类名推断模块ID: ExampleModule -> example
        String className = getClass().getSimpleName();
        if (className.endsWith("Module")) {
            className = className.substring(0, className.length() - 6);
        }
        this.moduleId = className.toLowerCase();
    }

    public AbstractModule(DreamRealms plugin, String moduleId) {
        super(plugin);
        this.moduleId = moduleId;
    }

    /**
     * 获取模块ID
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * 获取模块配置路径 (相对于插件数据目录)
     */
    public String getModulePath() {
        return "modules/" + moduleId;
    }

    /**
     * 获取模块 GUI 配置路径
     */
    public String getModuleMenuPath() {
        return getModulePath() + "/menu";
    }

    /**
     * 检查模块是否启用
     */
    public boolean isModuleEnabled() {
        return enabled;
    }

    /**
     * 设置模块启用状态
     */
    protected void setModuleEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 检查 debug 是否开启
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置 debug 状态
     */
    protected void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * 从主配置检查模块开关
     * 默认值为 false，新模块必须在 config.yml 中显式启用
     * 如果配置中不存在该模块的开关，会自动添加并保存
     */
    protected boolean checkModuleEnabled(MemoryConfiguration config) {
        String key = "modules." + moduleId;
        
        // 检查配置中是否存在该模块的开关
        if (!config.contains(key)) {
            // 自动添加模块开关到 config.yml
            addModuleToConfig();
        }
        
        this.enabled = config.getBoolean(key, false);
        
        // 模块启用时自动保存 README.md
        if (this.enabled) {
            saveModuleResource("README.md");
        }
        
        return this.enabled;
    }
    
    /**
     * 获取模块描述，子类可重写
     * @return 模块描述，用于在 config.yml 中添加注释
     */
    protected String getModuleDescription() {
        return null;
    }

    /**
     * 将模块开关添加到 config.yml
     */
    private void addModuleToConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }
        
        try {
            // 读取文件内容
            java.util.List<String> lines = java.nio.file.Files.readAllLines(configFile.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            
            // 查找 modules: 行的位置
            int modulesIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("modules:")) {
                    modulesIndex = i;
                    break;
                }
            }
            
            if (modulesIndex == -1) {
                // 没有 modules 节点，使用 YamlConfiguration 添加
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
                yaml.set("modules." + moduleId, false);
                yaml.save(configFile);
            } else {
                // 在 modules: 下添加新模块
                String description = getModuleDescription();
                java.util.List<String> newLines = new java.util.ArrayList<>(lines);
                
                // 找到插入位置 (modules: 的下一行)
                int insertIndex = modulesIndex + 1;
                
                // 跳过已有的注释和模块
                while (insertIndex < newLines.size()) {
                    String line = newLines.get(insertIndex);
                    if (line.trim().isEmpty() || line.trim().startsWith("#") || line.startsWith("  ")) {
                        insertIndex++;
                    } else {
                        break;
                    }
                }
                
                // 添加模块描述注释和开关
                if (description != null && !description.isEmpty()) {
                    newLines.add(insertIndex, "  # " + description);
                    insertIndex++;
                }
                newLines.add(insertIndex, "  " + moduleId + ": false");
                
                // 写回文件
                java.nio.file.Files.write(configFile.toPath(), newLines, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            info("已自动添加模块开关到 config.yml (默认禁用)");
        } catch (IOException e) {
            warn("无法保存模块开关到 config.yml: " + e.getMessage());
        }
    }

    /**
     * 获取模块配置文件
     */
    public File getModuleConfigFile(String fileName) {
        return new File(plugin.getDataFolder(), getModulePath() + "/" + fileName);
    }

    /**
     * 加载模块配置文件
     */
    public YamlConfiguration loadModuleConfig(String fileName) {
        File file = getModuleConfigFile(fileName);
        if (!file.exists()) {
            saveModuleResource(fileName);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * 保存模块默认配置
     */
    public void saveModuleResource(String fileName) {
        File file = getModuleConfigFile(fileName);
        if (!file.exists()) {
            String resourcePath = getModulePath() + "/" + fileName;
            plugin.saveResource(resourcePath, file);
        }
    }

    /**
     * Debug 日志 - 仅在 debug 开启时输出
     */
    public void debug(String... lines) {
        if (!debug) return;
        for (String line : lines) {
            plugin.info("[" + moduleId + "] [DEBUG] " + line);
        }
    }

    /**
     * 日志方法 (带模块前缀)
     */
    @Override
    public void info(String... lines) {
        for (String line : lines) {
            plugin.info("[" + moduleId + "] " + line);
        }
    }

    @Override
    public void warn(String... lines) {
        for (String line : lines) {
            plugin.warn("[" + moduleId + "] " + line);
        }
    }
}
