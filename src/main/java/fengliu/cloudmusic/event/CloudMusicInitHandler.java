package fengliu.cloudmusic.event;

import fengliu.cloudmusic.CloudMusicClient;
import fengliu.cloudmusic.config.Configs;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;

public class CloudMusicInitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        // 注册配置
        ConfigManager.getInstance().registerConfigHandler(CloudMusicClient.MOD_ID, Configs.INSTANCE);
        // 注册按键提供者
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
    }
}