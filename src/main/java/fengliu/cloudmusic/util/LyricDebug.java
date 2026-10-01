package fengliu.cloudmusic.util;

import fengliu.cloudmusic.CloudMusicClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class LyricDebug {
    public static boolean ENABLED = false;   // 除非你是java大手子，或者你想虐待你的游戏，我不知道这个作用，毕竟这是ai写的debug，恶魔轮盘赌，你赢了false，输了你永远开着true，不守规矩永远都是PVP_RUNNER

    public static void save(String songName, String type, String content) {
        if (!ENABLED) return;
        try {
            Path dir = CloudMusicClient.MC_PATH.resolve("cloudmusic").resolve("debug_cache");
            File dirFile = dir.toFile();
            if (!dirFile.exists()) dirFile.mkdirs();

            String safeName = songName.replaceAll("[\\\\/:*?\"<>|]", "_");
            File file = dir.resolve(safeName + "_" + type + ".txt").toFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }
            CloudMusicClient.LOGGER.info("[LyricDebug] saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            CloudMusicClient.LOGGER.error("[LyricDebug] save failed", e);
        }
    }
}