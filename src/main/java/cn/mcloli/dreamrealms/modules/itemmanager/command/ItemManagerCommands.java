package cn.mcloli.dreamrealms.modules.itemmanager.command;

import cn.mcloli.dreamrealms.command.ModuleCommandManager;
import cn.mcloli.dreamrealms.modules.itemmanager.ItemManagerModule;

/**
 * 物品管理器命令工厂
 */
public class ItemManagerCommands {

    public static ModuleCommandManager create(ItemManagerModule module) {
        return new ModuleCommandManager(module)
                .register(new AddCommand(module))
                .register(new GiveCommand(module))
                .register(new GetCommand(module))
                .register(new DeleteCommand(module))
                .register(new MenuCommand(module));
    }
}
