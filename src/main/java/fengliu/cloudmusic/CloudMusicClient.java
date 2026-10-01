package fengliu.cloudmusic;

import fengliu.cloudmusic.command.MusicCommand;
import fengliu.cloudmusic.event.CloudMusicInitHandler;
import fengliu.cloudmusic.event.HotkeysCallback;
import fengliu.cloudmusic.render.MusicHudRenderer;
import fengliu.cloudmusic.util.CacheHelper;
import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class CloudMusicClient implements ClientModInitializer {
    public static final String MOD_ID = "cloudmusic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static CacheHelper cacheHelper;
    public static Path MC_PATH;

    @Override
    public void onInitializeClient() {
        MC_PATH = FabricLoader.getInstance().getGameDir();
        cacheHelper = new CacheHelper();
        LOGGER.info("CloudMusic Mod loaded!");

        // 注册 /cloudmusic 客户端指令
        MusicCommand.registerAll();
        LOGGER.info("CloudMusic commands registered.");

        // 注册热键回调
        HotkeysCallback.init();
        LOGGER.info("CloudMusic hotkeys registered.");

        // 注册 MaLiLib 初始化处理器（按键提供者 + 配置）
        InitializationHandler.getInstance().registerInitializationHandler(new CloudMusicInitHandler());
        LOGGER.info("CloudMusic MaLiLib handlers registered.");

        // 注册 HUD 渲染（歌词 + 播放栏 + 二维码）
        MusicHudRenderer.register();
        LOGGER.info("CloudMusic HUD registered.");
    }
}