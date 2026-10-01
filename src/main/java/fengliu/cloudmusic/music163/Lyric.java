package fengliu.cloudmusic.music163;

import com.google.gson.JsonObject;
import fengliu.cloudmusic.CloudMusicClient;
import fengliu.cloudmusic.command.MusicCommand;
import fengliu.cloudmusic.util.LyricDebug;
import fengliu.cloudmusic.util.MusicPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 滚动歌词
 */
public class Lyric implements Runnable{
    private final Map<Long, String> lyric;
    private final Map<Long, String> tlyric;
    private boolean loopIn = true;
    private boolean load = true;
    private String[] toLyric = {};

    /**
     * 将歌词时间字符串转换为毫秒
     * @param n 歌词时间字符串
     * @return 歌词时间毫秒
     */
    public static long timeStrToTime(String n){
        try{
            String[] timeStr = n.split(":");
            String[] secondStr = timeStr[1].split("\\.");

            int minute = Integer.parseInt(timeStr[0]) * 60 * 1000;
            int second = Integer.parseInt(secondStr[0]) * 1000;

            // 修复：按毫秒位数自适应，而不是硬编码 ×10
            String msStr = secondStr[1];
            int millisecond;
            if (msStr.length() == 1) {
                millisecond = Integer.parseInt(msStr) * 100;
            } else if (msStr.length() == 2) {
                millisecond = Integer.parseInt(msStr) * 10;
            } else {
                millisecond = Integer.parseInt(msStr.substring(0, 3));
            }

            return minute + second + millisecond;
        }catch (Exception err){
            return 0;
        }
    }

    /**
     * 处理歌词字符串
     * @param lyric 歌词字符串
     * @return 歌词 Map
     */
    public static Map<Long, String> lyricToMap(String lyric){
        Map<Long, String> lyricMap = new LinkedHashMap<>();
        for (String lyricRow : lyric.split("\n")) {
            try {
                String[] lyricRows = lyricRow.substring(1).split("]", 2);
                if (lyricRows.length < 2){
                    continue;
                }

                lyricMap.put(timeStrToTime(lyricRows[0]), lyricRows[1]);
            }catch(Exception err){
            }
        }
        return lyricMap;
    }

    public Lyric(JsonObject data, String songName){
        String lyric = data.getAsJsonObject("lrc").get("lyric").getAsString();

        // debug：保存原始 LRC
        LyricDebug.save(songName, "lyric", lyric);

        if(lyric.equals("")){
            this.lyric = new LinkedHashMap<>();
            this.tlyric = this.lyric;
            return;
        }

        this.lyric = lyricToMap(lyric);
        CloudMusicClient.LOGGER.info("[Lyric] {} 解析到 {} 句原歌词", songName, this.lyric.size());

        if(!data.has("tlyric")){
            this.tlyric = new LinkedHashMap<>();
            return;
        }

        String tlyric = data.getAsJsonObject("tlyric").get("lyric").getAsString();
        LyricDebug.save(songName, "tlyric", tlyric);

        if(tlyric.equals("")){
            this.tlyric = new LinkedHashMap<>();
            return;
        }

        this.tlyric = lyricToMap(tlyric);
        CloudMusicClient.LOGGER.info("[Lyric] {} 解析到 {} 句翻译", songName, this.tlyric.size());

        // 检查时间戳是否严格递增
        long prev = -1;
        int backwardCount = 0;
        for (Long t : this.lyric.keySet()) {
            if (t < prev) backwardCount++;
            prev = t;
        }
        if (backwardCount > 0) {
            CloudMusicClient.LOGGER.warn("[Lyric] {} 时间戳有 {} 处回退！", songName, backwardCount);
        }
    }

    @Override
    public void run() {
        if(this.lyric.isEmpty()){
            this.toLyric.clone();
            return;
        }

        MusicPlayer player = MusicCommand.getPlayer();
        this.lyric.forEach((lyricTime, lyricData) -> {
            if (!this.loopIn) {
                return;
            }

            String lyric = null, tlyric = null;
            while (this.loopIn) {
                synchronized(this){
                    while(!load) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                long time = player.getPlayingProgress();
                if (time >= lyricTime){
                    lyric = lyricData;
                    tlyric = this.tlyric.get(lyricTime);
                    CloudMusicClient.LOGGER.info("[Lyric] 进度={}ms 切句={}ms 内容={}",
                            time, lyricTime, lyricData);
                    break;
                }
            }

            if (lyric != null && tlyric == null){
                this.toLyric = new String[]{lyric};
            }

            if(lyric != null && tlyric != null){
                this.toLyric = new String[]{lyric, tlyric};
            }

        });
        this.toLyric.clone();
    }

    /**
     * 获取当前滚动到的歌词
     * @return 歌词
     */
    public String[] getToLyric() {
        return toLyric;
    }

    /**
     * 开始歌词滚动
     */
    public void start(){
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("CloudMusicLyric thread");
        thread.start();
    }

    /**
     * 退出歌词滚动
     */
    public void exit(){
        this.loopIn = false;
        synchronized(this){
            this.load = true;
            notifyAll();
        }
    }

    /**
     * 暂停歌词滚动
     */
    public void stop(){
        synchronized(this){
            this.load = false;
            notifyAll();
        }
    }

    /**
     * 继续歌词滚动
     */
    public void continues(){
        synchronized(this){
            this.load = true;
            notifyAll();
        }
    }
}