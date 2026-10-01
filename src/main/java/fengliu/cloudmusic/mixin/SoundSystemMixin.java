package fengliu.cloudmusic.mixin;

import fengliu.cloudmusic.command.MusicCommand;
import fengliu.cloudmusic.config.Configs;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在播放音乐的时候如果MC需要播放背景音乐的话就取消播放背景音乐的事件
 */
@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
    /**
     * 判断是否需要停止播放背景音乐
     * @param soundCategory 声音分类
     * @return true 需要拦截，false放行
     */
    @Unique
    private static boolean shouldBlockGameBgm(SoundCategory soundCategory){
        if (!Configs.PLAY.NOT_PLAY_GAME_MUSIC.getBooleanValue()){
            return false;
        }
        if (!MusicCommand.getPlayer().isPlaying()) {
            return false;
        }
        return soundCategory == SoundCategory.MUSIC;
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance soundInstance, CallbackInfo ci) {
        if (shouldBlockGameBgm(soundInstance.getCategory())){
            ci.cancel();
        }
    }
}
