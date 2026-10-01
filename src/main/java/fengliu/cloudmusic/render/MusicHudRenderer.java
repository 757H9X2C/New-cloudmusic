package fengliu.cloudmusic.render;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import fengliu.cloudmusic.CloudMusicClient;
import fengliu.cloudmusic.command.MusicCommand;
import fengliu.cloudmusic.config.Configs;
import fengliu.cloudmusic.music163.IMusic;
import fengliu.cloudmusic.music163.data.DjMusic;
import fengliu.cloudmusic.music163.data.Music;
import fengliu.cloudmusic.util.MusicPlayer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MusicHudRenderer {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Identifier HUD_ID = Identifier.of(CloudMusicClient.MOD_ID, "music_overlay");

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                HUD_ID,
                (context, tickCounter) -> render(context, tickCounter)
        );
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        // 1. 二维码
        renderLoginQrCode(context);

        // 2. 播放栏
        renderMusicInfo(context);

        // 3. 歌词
        renderLyric(context);
    }

    private static void renderLoginQrCode(DrawContext context) {
        if (!MusicCommand.loadQRCode || !MusicIconTexture.isQrReady()) {
            return;
        }
        RenderPipeline renderLayer = RenderPipelines.GUI_TEXTURED;
        context.drawTexture(renderLayer, MusicIconTexture.QR_CODE_ID, 5, 10, 1f, 1f, 64, 64, 64, 64);
    }

    private static void renderMusicInfo(DrawContext context) {
        MusicPlayer player = MusicCommand.getPlayer();
        IMusic playingMusic = player.getPlayingMusic();
        if (playingMusic == null) {
            return;
        }
        if (Configs.GUI.STOP_PLAY_SHOW_UI.getBooleanValue() && !player.isPlaying()) {
            return;
        }
        if (!Configs.GUI.MUSIC_INFO.getBooleanValue()) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int[] pos = getMusicInfoPos();
        int y = pos[0];
        int x = pos[1];

        context.fill(width - 175 - x, y, width - x, 48 + y, Configs.GUI.MUSIC_INFO_COLOR.getIntegerValue());
        context.fill(width - 145 - x, 40 + y, width - 30 - x, 43 + y, Configs.GUI.MUSIC_PROGRESS_BAR_COLOR.getIntegerValue());

        int progress = Math.round((115 / (float) playingMusic.getDurationSecond()) * player.getPlayingProgressSecond());
        if (progress > 115) {
            progress = 115;
        }
        RenderPipeline renderLayer = RenderPipelines.GUI_TEXTURED;
        context.fill(width - 145 - x, 40 + y, width - 145 + progress - x, 43 + y, Configs.GUI.MUSIC_PLAYED_PROGRESS_BAR_COLOR.getIntegerValue());

        context.drawTexture(renderLayer, MusicIconTexture.MUSIC_ICON_ID, width - 172 - x, (int) (2.5f + y), 32f, 32f, 32, 32, 32, 32);
        context.drawText(client.textRenderer, playingMusic.getName().length() > 16 ? playingMusic.getName().substring(0, 16) + "..." : playingMusic.getName(), width - 135 - x, 4 + y, Configs.GUI.MUSIC_INFO_TITLE_FONT_COLOR.getIntegerValue(), true);

        int progressFontColor = Configs.GUI.MUSIC_PROGRESS_FONT_COLOR.getIntegerValue();
        context.drawText(client.textRenderer, player.getPlayingProgressToString(), width - 172 - x, 38 + y, progressFontColor, true);
        context.drawText(client.textRenderer, playingMusic.getDurationToString(), width - 28 - x, 38 + y, progressFontColor, true);

        int musicFontColor = Configs.GUI.MUSIC_INFO_FONT_COLOR.getIntegerValue();
        if (playingMusic instanceof DjMusic music) {
            context.drawText(client.textRenderer, Text.translatable("cloudmusic.info.dj.creator", music.dj.get("nickname").getAsString()), width - 135 - x, 14 + y, musicFontColor, true);
            context.drawText(client.textRenderer, Text.translatable("cloudmusic.info.dj.music.count", music.listenerCount, music.likedCount), width - 135 - x, 24 + y, musicFontColor, true);
            return;
        }
        Music music = (Music) playingMusic;
        if (!music.aliasName.isEmpty()) {
            context.drawText(client.textRenderer, music.aliasName.length() > 16 ? music.aliasName.substring(0, 16) + "..." : music.aliasName, width - 135 - x, 14 + y, musicFontColor, true);
        } else {
            String album = music.album.get("name").getAsString();
            context.drawText(client.textRenderer, album.length() > 16 ? album.substring(0, 16) + "..." : album, width - 135 - x, 14 + y, musicFontColor, true);
        }

        StringBuilder artist = new StringBuilder();
        for (JsonElement artistData : music.artists.asList()) {
            artist.append(artistData.getAsJsonObject().get("name").getAsString()).append("/");
        }
        artist = new StringBuilder(artist.substring(0, artist.length() - 1));
        context.drawText(client.textRenderer, artist.length() > 16 ? artist.substring(0, 16) + "..." : artist.toString(), width - 135 - x, 24 + y, musicFontColor, true);

        if (music.freeTrialInfo == null) {
            return;
        }
        int freeTrialEndProgress = Math.round((115 / (float) playingMusic.getDurationSecond()) * music.freeTrialInfo.get("end").getAsInt());
        context.fill(width - 145 + freeTrialEndProgress - 1 - x, 40 + y, width - 145 + freeTrialEndProgress + 1 - x, 43 + y, Configs.GUI.MUSIC_PLAYED_PROGRESS_BAR_COLOR.getIntegerValue());
    }

    private static void renderLyric(DrawContext context) {
        if (!Configs.GUI.LYRIC.getBooleanValue()) {
            return;
        }

        MusicPlayer player = MusicCommand.getPlayer();
        IMusic playingMusic = player.getPlayingMusic();
        if (playingMusic == null) {
            return;
        }
        if (Configs.GUI.STOP_PLAY_SHOW_UI.getBooleanValue() && !player.isPlaying()) {
            return;
        }

        String[] lyric = player.getLyric();
        if (lyric.length == 0) {
            return;
        }

        int lyricX = Configs.GUI.LYRIC_X.getIntegerValue();
        int lyricY = Configs.GUI.LYRIC_Y.getIntegerValue();
        int lyricColor = Configs.GUI.LYRIC_COLOR.getIntegerValue();
        float scale = (float) Configs.GUI.LYRIC_SCALE.getDoubleValue();

        for (String line : lyric) {
            if (line == null || line.isEmpty()) {
                continue;
            }
            context.getMatrices().pushMatrix();
            context.getMatrices().scale(scale, scale);
            context.drawText(client.textRenderer, line, (int) (lyricX / scale), (int) (lyricY / scale), lyricColor, true);
            context.getMatrices().popMatrix();
            lyricY += (int) (10 * scale);
        }
    }

    private static int[] getMusicInfoPos() {
        int y = Configs.GUI.MUSIC_INFO_Y.getIntegerValue();
        int x = Configs.GUI.MUSIC_INFO_X.getIntegerValue();
        if (client.player == null || !Configs.GUI.MUSIC_INFO_EFFECT_OFFSET.getBooleanValue()) {
            return new int[]{y, x};
        }
        int offset = 0;
        for (StatusEffectInstance statusEffect : client.player.getStatusEffects()) {
            if (statusEffect.getEffectType().value().isBeneficial()) {
                offset = 1;
            } else {
                offset = 2;
                break;
            }
        }
        if (offset == 0) {
            return new int[]{y, x};
        }
        return new int[]{y + Configs.GUI.MUSIC_INFO_EFFECT_OFFSET_Y.getIntegerValue() * offset, x + Configs.GUI.MUSIC_INFO_EFFECT_OFFSET_X.getIntegerValue()};
    }
}