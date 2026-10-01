package fengliu.cloudmusic.render;

import fengliu.cloudmusic.CloudMusicClient;
import fengliu.cloudmusic.music163.IMusic;
import fengliu.cloudmusic.util.HttpClient;
import fengliu.cloudmusic.util.PNGConverter;
import fengliu.cloudmusic.util.QRCode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;

public class MusicIconTexture {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static Identifier MUSIC_ICON_ID = Identifier.of(CloudMusicClient.MOD_ID, "texture/music_icon.png");
    public static Identifier QR_CODE_ID = Identifier.of(CloudMusicClient.MOD_ID, "qr.code");

    private static volatile boolean canUseIcon = false;
    private static volatile boolean qrReady = false;

    // 保存旧纹理用于释放资源，防止内存泄漏
    private static NativeImageBackedTexture oldMusicIconTex;
    private static NativeImageBackedTexture oldQrTex;

    /**
     * 获取封面并注册材质
     *
     * @param music 歌曲
     */
    public static void getMusicIcon(IMusic music) {
        Thread commandThread = new Thread(() -> {
            NativeImage img;
            try {
                img = NativeImage.read(PNGConverter.convertJPEGtoPNG(HttpClient.downloadStream(music.getPicUrl() + "?param=128y128")));
            } catch (Exception err) {
                err.printStackTrace();
                canUseIcon = false;
                return;
            }
            client.execute(() -> {
                // 释放上一个纹理
                if(oldMusicIconTex != null){
                    oldMusicIconTex.close();
                }
                NativeImageBackedTexture imageTexture = new NativeImageBackedTexture(() -> "music_icon", img);
                oldMusicIconTex = imageTexture;
                client.getTextureManager().registerTexture(MUSIC_ICON_ID, imageTexture);
                // ✅真正注册完成之后再置为true
                canUseIcon = true;
            });
        });
        commandThread.setDaemon(true);
        commandThread.setName("CloudMusic getMusicIcon Thread");
        commandThread.start();
    }

    public static void getQRCode(String QRCodeData) {
        qrReady = false;
        NativeImage img;
        try {
            File QRCodeFile = QRCode.generateQRCode(QRCodeData, CloudMusicClient.MC_PATH.resolve("cloud_music_qrcode.png").toString(), 128, 128, "png");
            img = NativeImage.read(new FileInputStream(QRCodeFile));
        } catch (Exception err) {
            err.printStackTrace();
            qrReady = false;
            return;
        }
        client.execute(() -> {
            if(oldQrTex != null){
                oldQrTex.close();
            }
            NativeImageBackedTexture imageTexture = new NativeImageBackedTexture(() -> "music_icon", img);
            oldQrTex = imageTexture;
            client.getTextureManager().registerTexture(QR_CODE_ID, imageTexture);
            qrReady = true; // 二维码纹理真正注册完毕标记
        });
    }

    public static boolean canUseIcon() {
        return canUseIcon;
    }

    public static boolean isQrReady(){
        return qrReady;
    }

    /** 重置二维码状态，登录完成/超时调用 */
    public static void resetQr(){
        qrReady = false;
        if(oldQrTex != null){
            client.execute(() -> {
                if(oldQrTex != null) oldQrTex.close();
                oldQrTex = null;
            });
        }
    }
}
