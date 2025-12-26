package cn.mcloli.dreamrealms.config;

/**
 * 权限常量
 */
public class Perms {

    public static final String PREFIX = "dreamrealms.";

    // 管理员权限
    public static final String ADMIN = PREFIX + "admin";
    public static final String RELOAD = PREFIX + "reload";

    // 命令权限
    public static final String COMMAND = PREFIX + "command";
    public static final String COMMAND_HELP = COMMAND + ".help";

    /**
     * 生成模块权限
     */
    public static String module(String moduleId) {
        return PREFIX + "module." + moduleId;
    }

    /**
     * 生成模块命令权限
     */
    public static String moduleCommand(String moduleId, String command) {
        return PREFIX + "module." + moduleId + ".command." + command;
    }

    /**
     * 生成模块管理权限
     */
    public static String moduleAdmin(String moduleId) {
        return PREFIX + "module." + moduleId + ".admin";
    }
}
