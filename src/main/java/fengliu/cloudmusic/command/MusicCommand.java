package fengliu.cloudmusic.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fengliu.cloudmusic.config.Configs;
import fengliu.cloudmusic.music163.*;
import fengliu.cloudmusic.music163.data.*;
import fengliu.cloudmusic.util.MusicPlayer;
import fengliu.cloudmusic.util.TextClickItem;
import fengliu.cloudmusic.util.page.Page;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MusicCommand {
    private static final LoginMusic163 loginMusic163 = new LoginMusic163();
    private static volatile Music163 music163 = null;
    private static MusicPlayer player = new MusicPlayer(new ArrayList<>());
    private static volatile Page page = null;
    private static Object data = null;
    private static My my = null;
    public static volatile boolean loadQRCode = false;

    private static final Text[] helps = {
            Text.translatable("cloudmusic.help.music"),
            Text.translatable("cloudmusic.help.music.play"),
            Text.translatable("cloudmusic.help.music.like"),
            Text.translatable("cloudmusic.help.music.unlike"),
            Text.translatable("cloudmusic.help.music.similar.music"),
            Text.translatable("cloudmusic.help.music.similar.playlist"),
            Text.translatable("cloudmusic.help.music.comment"),
            Text.translatable("cloudmusic.help.music.hot.comment"),
            Text.translatable("cloudmusic.help.music.send.comment"),

            Text.translatable("cloudmusic.help.playlist"),
            Text.translatable("cloudmusic.help.playlist.play"),
            Text.translatable("cloudmusic.help.playlist.subscribe"),
            Text.translatable("cloudmusic.help.playlist.unsubscribe"),
            Text.translatable("cloudmusic.help.playlist.add"),
            Text.translatable("cloudmusic.help.playlist.del"),
            Text.translatable("cloudmusic.help.playlist.comment"),
            Text.translatable("cloudmusic.help.playlist.hot.comment"),
            Text.translatable("cloudmusic.help.playlist.send.comment"),

            Text.translatable("cloudmusic.help.artist"),
            Text.translatable("cloudmusic.help.artist.top"),
            Text.translatable("cloudmusic.help.artist.album"),
            Text.translatable("cloudmusic.help.artist.similar"),
            Text.translatable("cloudmusic.help.artist.subscribe"),
            Text.translatable("cloudmusic.help.artist.unsubscribe"),

            Text.translatable("cloudmusic.help.album"),
            Text.translatable("cloudmusic.help.album.play"),
            Text.translatable("cloudmusic.help.album.subscribe"),
            Text.translatable("cloudmusic.help.album.unsubscribe"),
            Text.translatable("cloudmusic.help.album.comment"),
            Text.translatable("cloudmusic.help.album.hot.comment"),
            Text.translatable("cloudmusic.help.album.send.comment"),

            Text.translatable("cloudmusic.help.dj"),
            Text.translatable("cloudmusic.help.dj.play"),
            Text.translatable("cloudmusic.help.dj.music"),
            Text.translatable("cloudmusic.help.dj.music.play"),
            Text.translatable("cloudmusic.help.dj.music.send.comment"),
            Text.translatable("cloudmusic.help.dj.music.comment"),
            Text.translatable("cloudmusic.help.dj.music.hot.comment"),
            Text.translatable("cloudmusic.help.dj.subscribe"),
            Text.translatable("cloudmusic.help.dj.unsubscribe"),
            Text.translatable("cloudmusic.help.dj.send.comment"),
            Text.translatable("cloudmusic.help.dj.comment"),
            Text.translatable("cloudmusic.help.dj.hot.comment"),

            Text.translatable("cloudmusic.help.comment"),
            Text.translatable("cloudmusic.help.comment.floors"),
            Text.translatable("cloudmusic.help.comment.like"),
            Text.translatable("cloudmusic.help.comment.unlike"),
            Text.translatable("cloudmusic.help.comment.delete"),
            Text.translatable("cloudmusic.help.comment.reply"),

            Text.translatable("cloudmusic.help.user"),
            Text.translatable("cloudmusic.help.user.playlist"),
            Text.translatable("cloudmusic.help.user.dj"),
            Text.translatable("cloudmusic.help.user.like"),
            Text.translatable("cloudmusic.help.user.record.all"),
            Text.translatable("cloudmusic.help.user.record.week"),

            Text.translatable("cloudmusic.help.my"),
            Text.translatable("cloudmusic.help.my.fm"),
            Text.translatable("cloudmusic.help.my.intelligence"),
            Text.translatable("cloudmusic.help.my.like"),
            Text.translatable("cloudmusic.help.my.playlist"),
            Text.translatable("cloudmusic.help.my.dj"),
            Text.translatable("cloudmusic.help.my.style"),
            Text.translatable("cloudmusic.help.my.playlist.add"),
            Text.translatable("cloudmusic.help.my.playlist.del"),
            Text.translatable("cloudmusic.help.my.recommend.music"),
            Text.translatable("cloudmusic.help.my.recommend.playlist"),
            Text.translatable("cloudmusic.help.my.recommend.history"),
            Text.translatable("cloudmusic.help.my.recommend.history.date"),
            Text.translatable("cloudmusic.help.my.sublist.album"),
            Text.translatable("cloudmusic.help.my.sublist.artist"),
            Text.translatable("cloudmusic.help.my.sublist.dj"),
            Text.translatable("cloudmusic.help.my.record.music"),
            Text.translatable("cloudmusic.help.my.record.djmusic"),
            Text.translatable("cloudmusic.help.my.record.playlist"),
            Text.translatable("cloudmusic.help.my.record.album"),
            Text.translatable("cloudmusic.help.my.record.dj"),

            Text.translatable("cloudmusic.help.style"),
            Text.translatable("cloudmusic.help.style.all"),
            Text.translatable("cloudmusic.help.style.children"),
            Text.translatable("cloudmusic.help.style.music"),
            Text.translatable("cloudmusic.help.style.playlist"),
            Text.translatable("cloudmusic.help.style.artist"),
            Text.translatable("cloudmusic.help.style.album"),

            Text.translatable("cloudmusic.help.top.list"),
            Text.translatable("cloudmusic.help.top.artist"),
            Text.translatable("cloudmusic.help.top.playlist.highquality.tags"),
            Text.translatable("cloudmusic.help.top.playlist.highquality"),
            Text.translatable("cloudmusic.help.top.playlist.tags"),
            Text.translatable("cloudmusic.help.top.playlist.tags.hot"),
            Text.translatable("cloudmusic.help.top.playlist"),

            Text.translatable("cloudmusic.help.search.music"),
            Text.translatable("cloudmusic.help.search.album"),
            Text.translatable("cloudmusic.help.search.artist"),
            Text.translatable("cloudmusic.help.search.playlist"),
            Text.translatable("cloudmusic.help.search.dj"),

            Text.translatable("cloudmusic.help.login.email"),
            Text.translatable("cloudmusic.help.login.captcha"),
            Text.translatable("cloudmusic.help.login.captcha.login"),
            Text.translatable("cloudmusic.help.login.captcha.phone"),
            Text.translatable("cloudmusic.help.login.qr"),

            Text.translatable("cloudmusic.help.volume"),
            Text.translatable("cloudmusic.help.volume.volume"),

            Text.translatable("cloudmusic.help.page.prev"),
            Text.translatable("cloudmusic.help.page.next"),
            Text.translatable("cloudmusic.help.page.to"),

            Text.translatable("cloudmusic.help.playing"),
            Text.translatable("cloudmusic.help.playing.all"),

            Text.translatable("cloudmusic.help.stop"),
            Text.translatable("cloudmusic.help.continue"),
            Text.translatable("cloudmusic.help.prev"),
            Text.translatable("cloudmusic.help.next"),
            Text.translatable("cloudmusic.help.to"),
            Text.translatable("cloudmusic.help.del"),
            Text.translatable("cloudmusic.help.trash"),
            Text.translatable("cloudmusic.help.random"),
            Text.translatable("cloudmusic.help.exit"),
            Text.translatable("cloudmusic.help.cloudmusic"),
    };

    private static final List<Text> helpsList = new ArrayList<>();

    public static MusicPlayer getPlayer() {
        return player;
    }

    public static Music163 getMusic163() {
        if (music163 == null) {
            music163 = new Music163(Configs.LOGIN.COOKIE.getStringValue());
        }
        return music163;
    }

    public static void setPage(Page Page) {
        page = Page;
    }

    public static My getMy(boolean reset) {
        if (my == null || reset) {
            my = getMusic163().my();
        }
        return my;
    }

    private static void resetPlayer(List<IMusic> musics) {
        try {
            player.exit();
        } catch (Exception ignored) {
        }
        player = new MusicPlayer(musics);
    }

    private static void resetPlayer(MusicPlayer newPlayer) {
        try {
            player.exit();
        } catch (Exception ignored) {
        }
        player = newPlayer;
    }

    private static void resetPlayer(IMusic music) {
        List<IMusic> musics = new ArrayList<>();
        musics.add(music);
        resetPlayer(musics);
    }

    private static void resetCookie(String cookie) {
        if (cookie == null || getMusic163().getHttpClient().getCookies().equals(cookie)) {
            return;
        }
        Configs.LOGIN.COOKIE.setValueFromString(cookie);
        music163 = new Music163(cookie);
        getMy(true);
        Configs.INSTANCE.save();
    }

    private interface Job {
        void fun(CommandContext<FabricClientCommandSource> context) throws Exception;
    }

    private static void runCommand(CommandContext<FabricClientCommandSource> context, Job job) {
        Thread commandThread = new Thread(() -> {
            try {
                job.fun(context);
            } catch (Exception err) {
                String msg = err.getMessage() != null ? err.getMessage() : err.toString();
                MinecraftClient.getInstance().execute(() ->
                        context.getSource().sendFeedback(Text.literal(msg)));
            }
        });
        commandThread.setDaemon(true);
        commandThread.setName("CloudMusic Thread");
        commandThread.start();
    }

    private static void feedback(CommandContext<FabricClientCommandSource> context, Text text) {
        MinecraftClient.getInstance().execute(() ->
                context.getSource().sendFeedback(text));
    }

    private static void lookPage(CommandContext<FabricClientCommandSource> context, Page p) {
        MinecraftClient.getInstance().execute(() ->
                p.look(context.getSource()));
    }

    public static void registerAll() {
        LiteralArgumentBuilder<FabricClientCommandSource> CloudMusic = literal("cloudmusic");
        LiteralArgumentBuilder<FabricClientCommandSource> Music = literal("music");
        LiteralArgumentBuilder<FabricClientCommandSource> PlayList = literal("playlist");
        LiteralArgumentBuilder<FabricClientCommandSource> Artist = literal("artist");
        LiteralArgumentBuilder<FabricClientCommandSource> Album = literal("album");
        LiteralArgumentBuilder<FabricClientCommandSource> Dj = literal("dj");
        LiteralArgumentBuilder<FabricClientCommandSource> User = literal("user");
        LiteralArgumentBuilder<FabricClientCommandSource> My = literal("my");
        LiteralArgumentBuilder<FabricClientCommandSource> Style = literal("style");
        LiteralArgumentBuilder<FabricClientCommandSource> Top = literal("top");
        LiteralArgumentBuilder<FabricClientCommandSource> Playing = literal("playing");
        LiteralArgumentBuilder<FabricClientCommandSource> Search = literal("search");
        LiteralArgumentBuilder<FabricClientCommandSource> Volume = literal("volume");
        LiteralArgumentBuilder<FabricClientCommandSource> Page = literal("page");
        LiteralArgumentBuilder<FabricClientCommandSource> Login = literal("login");
        LiteralArgumentBuilder<FabricClientCommandSource> Comment = literal("comment");

        Collections.addAll(helpsList, helps);

        CloudMusic.executes(context -> {
            page = new Page(helpsList) {
                @Override
                protected TextClickItem putPageItem(Object data) {
                    return new TextClickItem((MutableText) data, "");
                }
            };
            page.setInfoText(Text.translatable("cloudmusic.info.page.help"));
            lookPage(context, page);
            return Command.SINGLE_SUCCESS;
        });

        CloudMusic.then(Music.then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                        MinecraftClient.getInstance().execute(() -> music.printToChatHud(context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ));

        CloudMusic.then(Music.then(literal("play").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                        resetPlayer(music);
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.music.play", music.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Music.then(literal("like").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                        music.like();
                        feedback(context, Text.translatable("cloudmusic.info.command.music.like", music.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Music.then(literal("unlike").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                        music.unlike();
                        feedback(context, Text.translatable("cloudmusic.info.command.music.unlike", music.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        LiteralArgumentBuilder<FabricClientCommandSource> Similar = literal("similar");

        CloudMusic.then(Music.then(Similar.then(literal("music").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                        Page p = music.similar();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.music.similar", music.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Music.then(Similar.then(literal("playlist").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                        Page p = music.similarPlaylist();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.music.similar.playlist", music.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Music.then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                        Page p = music.comments(false);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.music.comments", music.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Music.then(literal("hotComment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                        Page p = music.comments(true);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.music.hot.comments", music.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Music.then(literal("send").then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).then(
                        argument("content", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                Music music = getMusic163().music(LongArgumentType.getLong(context, "id"));
                                music.send(StringArgumentType.getString(context, "content"));
                                feedback(context, Text.translatable("cloudmusic.info.command.send.comment", music.name));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                )))));

        CloudMusic.then(PlayList.then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        PlayList playList = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                        MinecraftClient.getInstance().execute(() -> playList.printToChatHud(context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ));

        CloudMusic.then(PlayList.then(literal("play").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        PlayList playList = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                        resetPlayer(playList.getMusics());
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.playlist.play", playList.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(PlayList.then(literal("send").then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).then(
                        argument("content", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                PlayList playlist = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                                playlist.send(StringArgumentType.getString(context, "content"));
                                feedback(context, Text.translatable("cloudmusic.info.command.send.comment", playlist.name));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                )))));

        CloudMusic.then(PlayList.then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        PlayList playList = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                        Page p = playList.comments(false);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.playlist.comments", playList.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(PlayList.then(literal("hotComment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        PlayList playList = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                        Page p = playList.comments(true);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.playlist.hot.comments", playList.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(PlayList.then(literal("subscribe").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        PlayList playList = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                        playList.subscribe();
                        feedback(context, Text.translatable("cloudmusic.info.command.playlist.subscribe", playList.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(PlayList.then(literal("unsubscribe").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        PlayList playList = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                        playList.unsubscribe();
                        feedback(context, Text.translatable("cloudmusic.info.command.playlist.unsubscribe", playList.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(PlayList.then(literal("add").then(
                argument("id", LongArgumentType.longArg()).then(
                        argument("musicId", LongArgumentType.longArg()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                long musicId = LongArgumentType.getLong(context, "musicId");
                                PlayList playList = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                                playList.add(musicId);
                                feedback(context, Text.translatable("cloudmusic.info.command.playlist.add", playList.name, musicId));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                ))
        ));

        CloudMusic.then(PlayList.then(literal("del").then(
                argument("id", LongArgumentType.longArg()).then(
                        argument("musicId", LongArgumentType.longArg()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                long musicId = LongArgumentType.getLong(context, "musicId");
                                PlayList playList = getMusic163().playlist(LongArgumentType.getLong(context, "id"));
                                playList.del(musicId);
                                feedback(context, Text.translatable("cloudmusic.info.command.playlist.del", playList.name, musicId));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                ))
        ));

        CloudMusic.then(Artist.then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Artist artist = getMusic163().artist(LongArgumentType.getLong(context, "id"));
                        MinecraftClient.getInstance().execute(() -> artist.printToChatHud(context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ));

        CloudMusic.then(Artist.then(literal("top").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Artist artist = getMusic163().artist(LongArgumentType.getLong(context, "id"));
                        resetPlayer(artist.topSong());
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.artist.top.play", artist.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Artist.then(literal("album").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Artist artist = getMusic163().artist(LongArgumentType.getLong(context, "id"));
                        Page p = artist.albumPage();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.artist.album", artist.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Artist.then(literal("similar").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Artist artist = getMusic163().artist(LongArgumentType.getLong(context, "id"));
                        Page p = artist.similar();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.artist.similar", artist.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Artist.then(literal("subscribe").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Artist artist = getMusic163().artist(LongArgumentType.getLong(context, "id"));
                        artist.subscribe();
                        feedback(context, Text.translatable("cloudmusic.info.command.artist.subscribe", artist.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Artist.then(literal("unsubscribe").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Artist artist = getMusic163().artist(LongArgumentType.getLong(context, "id"));
                        artist.unsubscribe();
                        feedback(context, Text.translatable("cloudmusic.info.command.artist.unsubscribe", artist.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Album.then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Album album = getMusic163().album(LongArgumentType.getLong(context, "id"));
                        MinecraftClient.getInstance().execute(() -> album.printToChatHud(context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ));

        CloudMusic.then(Album.then(literal("play").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Album album = getMusic163().album(LongArgumentType.getLong(context, "id"));
                        resetPlayer(album.getMusics());
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.album.play", album.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Album.then(literal("send").then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).then(
                        argument("content", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                Album album = getMusic163().album(LongArgumentType.getLong(context, "id"));
                                album.send(StringArgumentType.getString(context, "content"));
                                feedback(context, Text.translatable("cloudmusic.info.command.send.comment", album.name));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                )))));

        CloudMusic.then(Album.then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Album album = getMusic163().album(LongArgumentType.getLong(context, "id"));
                        Page p = album.comments(false);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.album.comments", album.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Album.then(literal("hotComment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Album album = getMusic163().album(LongArgumentType.getLong(context, "id"));
                        Page p = album.comments(true);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.album.hot.comments", album.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Album.then(literal("subscribe").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Album album = getMusic163().album(LongArgumentType.getLong(context, "id"));
                        album.subscribe();
                        feedback(context, Text.translatable("cloudmusic.info.command.album.subscribe", album.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Album.then(literal("unsubscribe").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Album album = getMusic163().album(LongArgumentType.getLong(context, "id"));
                        album.unsubscribe();
                        feedback(context, Text.translatable("cloudmusic.info.command.album.unsubscribe", album.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Dj.then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjRadio djRadio = getMusic163().djRadio(LongArgumentType.getLong(context, "id"));
                        MinecraftClient.getInstance().execute(() -> djRadio.printToChatHud(context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ));

        CloudMusic.then(Dj.then(literal("play").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjRadio djRadio = getMusic163().djRadio(LongArgumentType.getLong(context, "id"));
                        resetPlayer(djRadio);
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.dj.play", djRadio.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Dj.then(literal("send").then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).then(
                        argument("content", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                DjRadio djRadio = getMusic163().djRadio(LongArgumentType.getLong(context, "id"));
                                djRadio.send(StringArgumentType.getString(context, "content"));
                                feedback(context, Text.translatable("cloudmusic.info.command.send.comment", djRadio.name));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                )))));

        CloudMusic.then(Dj.then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjRadio djRadio = getMusic163().djRadio(LongArgumentType.getLong(context, "id"));
                        Page p = djRadio.comments(false);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.dj.radio.comments", djRadio.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Dj.then(literal("hotComment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjRadio djRadio = getMusic163().djRadio(LongArgumentType.getLong(context, "id"));
                        Page p = djRadio.comments(true);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.dj.radio.hot.comments", djRadio.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        LiteralArgumentBuilder<FabricClientCommandSource> DjMusic = literal("music");

        CloudMusic.then(Dj.then(DjMusic.then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjMusic music = getMusic163().djMusic(LongArgumentType.getLong(context, "id"));
                        MinecraftClient.getInstance().execute(() -> music.printToChatHud(context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Dj.then(DjMusic.then(literal("play").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjMusic music = getMusic163().djMusic(LongArgumentType.getLong(context, "id"));
                        resetPlayer(music);
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.dj.music.play", music.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Dj.then(DjMusic.then(literal("send").then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).then(
                        argument("content", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                DjMusic music = getMusic163().djMusic(LongArgumentType.getLong(context, "id"));
                                music.send(StringArgumentType.getString(context, "content"));
                                feedback(context, Text.translatable("cloudmusic.info.command.send.comment", music.name));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                ))))));

        CloudMusic.then(Dj.then(DjMusic.then(literal("comment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjMusic music = getMusic163().djMusic(LongArgumentType.getLong(context, "id"));
                        Page p = music.comments(false);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.dj.music.comments", music.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Dj.then(DjMusic.then(literal("hotComment").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjMusic music = getMusic163().djMusic(LongArgumentType.getLong(context, "id"));
                        Page p = music.comments(true);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.dj.music.hot.comments", music.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Dj.then(literal("subscribe").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjRadio djRadio = getMusic163().djRadio(LongArgumentType.getLong(context, "id"));
                        djRadio.subscribe();
                        feedback(context, Text.translatable("cloudmusic.info.command.dj.subscribe", djRadio.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Dj.then(literal("unsubscribe").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        DjRadio djRadio = getMusic163().djRadio(LongArgumentType.getLong(context, "id"));
                        djRadio.unsubscribe();
                        feedback(context, Text.translatable("cloudmusic.info.command.dj.unsubscribe", djRadio.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(User.then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        User user = getMusic163().user(LongArgumentType.getLong(context, "id"));
                        MinecraftClient.getInstance().execute(() -> user.printToChatHud(context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ));

        CloudMusic.then(User.then(literal("playlist").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        User user = getMusic163().user(LongArgumentType.getLong(context, "id"));
                        Page p = user.playListsPage();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.user.playlist", user.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                }))
        ));

        CloudMusic.then(User.then(literal("dj").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        User user = getMusic163().user(LongArgumentType.getLong(context, "id"));
                        Page p = user.djRadio();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.user.dj", user.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                }))
        ));

        CloudMusic.then(User.then(literal("like").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        User user = getMusic163().user(LongArgumentType.getLong(context, "id"));
                        resetPlayer(user.likeMusicPlayList().getMusics());
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.like", user.name));
                    });
                    return Command.SINGLE_SUCCESS;
                }))
        ));

        LiteralArgumentBuilder<FabricClientCommandSource> Record = literal("record");

        CloudMusic.then(User.then(Record.then(literal("all").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        User user = getMusic163().user(LongArgumentType.getLong(context, "id"));
                        resetPlayer(user.recordAll());
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.user.record.all", user.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })))
        ));

        CloudMusic.then(User.then(Record.then(literal("week").then(
                argument("id", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        User user = getMusic163().user(LongArgumentType.getLong(context, "id"));
                        resetPlayer(user.recordWeek());
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.user.record.week", user.name));
                    });
                    return Command.SINGLE_SUCCESS;
                })))
        ));

        CloudMusic.then(My.executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(true);
                MinecraftClient.getInstance().execute(() -> m.printToChatHud(context.getSource()));
            });
            return Command.SINGLE_SUCCESS;
        }));

        CloudMusic.then(My.then(literal("like").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                resetPlayer(m.likeMusicPlayList().getMusics());
                player.start();
                feedback(context, Text.translatable("cloudmusic.info.command.like", m.name));
            });
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(My.then(literal("fm").executes(contextData -> {
            runCommand(contextData, context -> {
                resetPlayer(new Fm(getMy(false)));
                player.start();
                feedback(context, Text.translatable("cloudmusic.info.command.fm"));
            });
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(My.then(literal("intelligence").executes(contextData -> {
            runCommand(contextData, context -> {
                resetPlayer(getMy(false).intelligencePlayMode());
                player.start();
                feedback(context, Text.translatable("cloudmusic.info.command.intelligence"));
            });
            return Command.SINGLE_SUCCESS;
        })));

        LiteralArgumentBuilder<FabricClientCommandSource> MyPlayList = literal("playlist");

        CloudMusic.then(My.then(MyPlayList.executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.playListsPage();
                p.setInfoText(Text.translatable("cloudmusic.info.page.user.playlist", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(My.then(literal("dj").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.djRadio();
                p.setInfoText(Text.translatable("cloudmusic.info.page.user.dj", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(My.then(literal("style").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.preferenceStyles();
                p.setInfoText(Text.translatable("cloudmusic.info.page.preference.style", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })));

        LiteralArgumentBuilder<FabricClientCommandSource> PlayRecord = literal("record");

        CloudMusic.then(My.then(PlayRecord.then(literal("music").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                resetPlayer(m.recordPlayMusic());
                player.start();
                feedback(context, Text.translatable("cloudmusic.info.command.record.music", m.name));
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(PlayRecord.then(literal("djmusic").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                resetPlayer(m.recordPlayDjMusic());
                player.start();
                feedback(context, Text.translatable("cloudmusic.info.command.record.djmusic", m.name));
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(PlayRecord.then(literal("playlist").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.recordPlayPlayList();
                p.setInfoText(Text.translatable("cloudmusic.info.page.record.playlist", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(PlayRecord.then(literal("album").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.recordPlayAlbum();
                p.setInfoText(Text.translatable("cloudmusic.info.page.record.album", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(PlayRecord.then(literal("dj").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.recordPlayDj();
                p.setInfoText(Text.translatable("cloudmusic.info.page.record.dj", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(MyPlayList.then(literal("add").then(
                argument("musicId", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        My m = getMy(false);
                        Page p = m.playListSetMusic(LongArgumentType.getLong(context, "musicId"), "add");
                        p.setInfoText(Text.translatable("cloudmusic.info.page.user.playlist.add", m.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(My.then(MyPlayList.then(literal("del").then(
                argument("musicId", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        My m = getMy(false);
                        Page p = m.playListSetMusic(LongArgumentType.getLong(context, "musicId"), "del");
                        p.setInfoText(Text.translatable("cloudmusic.info.page.user.playlist.del", m.name));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        LiteralArgumentBuilder<FabricClientCommandSource> Recommend = literal("recommend");

        CloudMusic.then(My.then(Recommend.then(literal("music").executes(contextData -> {
            runCommand(contextData, context -> {
                resetPlayer(getMy(false).recommendSongs());
                player.start();
                feedback(context, Text.translatable("cloudmusic.info.command.recommend.music"));
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(Recommend.then(literal("playlist").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.recommendResource();
                p.setInfoText(Text.translatable("cloudmusic.info.page.recommend.playlist", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(Recommend.then(literal("history").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.recommendHistorySongsRecent();
                p.setInfoText(Text.translatable("cloudmusic.info.page.recommend.history", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(Recommend.then(literal("history").then(
                argument("date", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        String date = StringArgumentType.getString(context, "date");
                        resetPlayer(getMy(false).recommendHistorySongs(date));
                        player.start();
                        feedback(context, Text.translatable("cloudmusic.info.command.recommend.history.music", date));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        LiteralArgumentBuilder<FabricClientCommandSource> Sublist = literal("sublist");

        CloudMusic.then(My.then(Sublist.then(literal("album").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.sublistAlbum();
                p.setInfoText(Text.translatable("cloudmusic.info.page.sublist.album", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(Sublist.then(literal("artist").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.sublistArtist();
                p.setInfoText(Text.translatable("cloudmusic.info.page.sublist.artist", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(My.then(Sublist.then(literal("dj").executes(contextData -> {
            runCommand(contextData, context -> {
                My m = getMy(false);
                Page p = m.sublistDjRadio();
                p.setInfoText(Text.translatable("cloudmusic.info.page.sublist.dj", m.name));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(Style.then(
                argument("id", IntegerArgumentType.integer()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        StyleTag style = getMusic163().style(IntegerArgumentType.getInteger(context, "id"));
                        MinecraftClient.getInstance().execute(() -> style.printToChatHud(context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ));

        CloudMusic.then(Style.then(literal("all").executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = getMusic163().styleList();
                p.setInfoText(Text.translatable("cloudmusic.info.page.style"));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(Style.then(literal("children").then(
                argument("id", IntegerArgumentType.integer()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        StyleTag style = getMusic163().style(IntegerArgumentType.getInteger(context, "id"));
                        Page p = style.childrenStyles();
                        if (p == null) {
                            feedback(context, Text.translatable("cloudmusic.info.command.style.not.children", style.name, style.enName));
                            return;
                        }
                        p.setInfoText(Text.translatable("cloudmusic.info.page.style.children", style.name, style.enName));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Style.then(literal("music").then(
                argument("id", IntegerArgumentType.integer()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        StyleTag style = getMusic163().style(IntegerArgumentType.getInteger(context, "id"));
                        Page p = style.music();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.style.music", style.name, style.enName));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Style.then(literal("playlist").then(
                argument("id", IntegerArgumentType.integer()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        StyleTag style = getMusic163().style(IntegerArgumentType.getInteger(context, "id"));
                        Page p = style.playlist();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.style.playlist", style.name, style.enName));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Style.then(literal("artist").then(
                argument("id", IntegerArgumentType.integer()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        StyleTag style = getMusic163().style(IntegerArgumentType.getInteger(context, "id"));
                        Page p = style.artist();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.style.artist", style.name, style.enName));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Style.then(literal("album").then(
                argument("id", IntegerArgumentType.integer()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        StyleTag style = getMusic163().style(IntegerArgumentType.getInteger(context, "id"));
                        Page p = style.album();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.style.album", style.name, style.enName));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Top.then(literal("list").executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = getMusic163().topList();
                p.setInfoText(Text.translatable("cloudmusic.info.page.top.list"));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(Top.then(literal("artist").executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = getMusic163().topArtistList();
                p.setInfoText(Text.translatable("cloudmusic.info.page.top.artist"));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })));

        LiteralArgumentBuilder<FabricClientCommandSource> TopPlayList = literal("playlist");
        LiteralArgumentBuilder<FabricClientCommandSource> HighQuality = literal("highquality");

        CloudMusic.then(Top.then(TopPlayList.then(HighQuality.then(literal("tags").executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = getMusic163().playListHighQualityTags();
                p.setInfoText(Text.translatable("cloudmusic.info.page.top.playlist.highquality.tags"));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })))));

        CloudMusic.then(Top.then(TopPlayList.then(HighQuality.executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = getMusic163().topPlayListHighQuality("全部");
                p.setInfoText(Text.translatable("cloudmusic.info.page.top.playlist.highquality", "全部"));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(Top.then(TopPlayList.then(HighQuality.then(
                argument("tag", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        String tag = StringArgumentType.getString(context, "tag");
                        Page p = getMusic163().topPlayListHighQuality(tag);
                        if (p == null) {
                            feedback(context, Text.translatable("cloudmusic.info.command.tag.not.top.playlist.highquality", tag));
                            return;
                        }
                        p.setInfoText(Text.translatable("cloudmusic.info.page.top.playlist.highquality", tag));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        LiteralArgumentBuilder<FabricClientCommandSource> Tags = literal("tags");

        CloudMusic.then(Top.then(TopPlayList.then(Tags.executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = getMusic163().playListTags();
                p.setInfoText(Text.translatable("cloudmusic.info.page.top.playlist.tags"));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        }))));

        CloudMusic.then(Top.then(TopPlayList.then(Tags.then(literal("hot").executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = getMusic163().playListTagsHot();
                p.setInfoText(Text.translatable("cloudmusic.info.page.top.playlist.hot.tags"));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })))));

        CloudMusic.then(Top.then(TopPlayList.executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = getMusic163().topPlayList("全部");
                p.setInfoText(Text.translatable("cloudmusic.info.page.top.playlist", "全部"));
                page = p;
                lookPage(context, p);
            });
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(Top.then(TopPlayList.then(
                argument("tag", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        String tag = StringArgumentType.getString(context, "tag");
                        Page p = getMusic163().topPlayList(tag);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.top.playlist", tag));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Search.then(literal("music").then(
                argument("key", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        String key = StringArgumentType.getString(context, "key");
                        Page p = getMusic163().searchMusic(key);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.search", key));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Search.then(literal("album").then(
                argument("key", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        String key = StringArgumentType.getString(context, "key");
                        Page p = getMusic163().searchAlbum(key);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.search", key));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Search.then(literal("artist").then(
                argument("key", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        String key = StringArgumentType.getString(context, "key");
                        Page p = getMusic163().searchArtist(key);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.search", key));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Search.then(literal("playlist").then(
                argument("key", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        String key = StringArgumentType.getString(context, "key");
                        Page p = getMusic163().searchPlayList(key);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.search", key));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Search.then(literal("dj").then(
                argument("key", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        String key = StringArgumentType.getString(context, "key");
                        Page p = getMusic163().searchDjRadio(key);
                        p.setInfoText(Text.translatable("cloudmusic.info.page.search", key));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Comment.then(
                argument("id", LongArgumentType.longArg()).then(
                        argument("threadId", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                long id = LongArgumentType.getLong(context, "id");
                                String threadId = StringArgumentType.getString(context, "threadId");
                                Page currentPage = page;
                                if (currentPage == null) {
                                    return;
                                }
                                JsonObject json = currentPage.getJsonItem(jsonObject -> jsonObject.get("commentId").getAsLong() == id);
                                if (json == null) {
                                    return;
                                }
                                Comment comment = new Comment(getMusic163().getHttpClient(), json, threadId);
                                MinecraftClient.getInstance().execute(() -> comment.printToChatHud(context.getSource()));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                ))
        );

        CloudMusic.then(Comment.then(literal("floors").then(argument("id", LongArgumentType.longArg()).then(
                argument("threadId", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        long id = LongArgumentType.getLong(context, "id");
                        String threadId = StringArgumentType.getString(context, "threadId");
                        Page currentPage = page;
                        if (currentPage == null) {
                            return;
                        }
                        JsonObject json = currentPage.getJsonItem(jsonObject -> jsonObject.get("commentId").getAsLong() == id);
                        if (json == null) {
                            return;
                        }
                        Comment comment = new Comment(getMusic163().getHttpClient(), json, threadId);
                        Page p = comment.floors();
                        p.setInfoText(Text.translatable("cloudmusic.info.page.comment.floors", comment.id));
                        page = p;
                        lookPage(context, p);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Comment.then(literal("like").then(argument("id", LongArgumentType.longArg()).then(
                argument("threadId", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        long id = LongArgumentType.getLong(context, "id");
                        String threadId = StringArgumentType.getString(context, "threadId");
                        Page currentPage = page;
                        if (currentPage == null) {
                            return;
                        }
                        JsonObject json = currentPage.getJsonItem(jsonObject -> jsonObject.get("commentId").getAsLong() == id);
                        if (json == null) {
                            return;
                        }
                        Comment comment = new Comment(getMusic163().getHttpClient(), json, threadId);
                        comment.like();
                        feedback(context, Text.translatable("cloudmusic.info.command.comment.like", comment.content));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Comment.then(literal("unlike").then(argument("id", LongArgumentType.longArg()).then(
                argument("threadId", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        long id = LongArgumentType.getLong(context, "id");
                        String threadId = StringArgumentType.getString(context, "threadId");
                        Page currentPage = page;
                        if (currentPage == null) {
                            return;
                        }
                        JsonObject json = currentPage.getJsonItem(jsonObject -> jsonObject.get("commentId").getAsLong() == id);
                        if (json == null) {
                            return;
                        }
                        Comment comment = new Comment(getMusic163().getHttpClient(), json, threadId);
                        comment.unlike();
                        feedback(context, Text.translatable("cloudmusic.info.command.comment.unlike", comment.content));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Comment.then(literal("delete").then(argument("id", LongArgumentType.longArg()).then(
                argument("threadId", StringArgumentType.string()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        long id = LongArgumentType.getLong(context, "id");
                        String threadId = StringArgumentType.getString(context, "threadId");
                        Page currentPage = page;
                        if (currentPage == null) {
                            return;
                        }
                        JsonObject json = currentPage.getJsonItem(jsonObject -> jsonObject.get("commentId").getAsLong() == id);
                        if (json == null) {
                            return;
                        }
                        Comment comment = new Comment(getMusic163().getHttpClient(), json, threadId);
                        comment.delete();
                        feedback(context, Text.translatable("cloudmusic.info.command.comment.delete", comment.content));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        ))));

        CloudMusic.then(Comment.then(literal("reply").then(argument("id", LongArgumentType.longArg()).then(
                argument("threadId", StringArgumentType.string()).then(
                        argument("content", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                long id = LongArgumentType.getLong(context, "id");
                                String threadId = StringArgumentType.getString(context, "threadId");
                                String content = StringArgumentType.getString(context, "content");
                                Page currentPage = page;
                                if (currentPage == null) {
                                    return;
                                }
                                JsonObject json = currentPage.getJsonItem(jsonObject -> jsonObject.get("commentId").getAsLong() == id);
                                if (json == null) {
                                    return;
                                }
                                Comment comment = new Comment(getMusic163().getHttpClient(), json, threadId);
                                comment.reply(content);
                                feedback(context, Text.translatable("cloudmusic.info.command.comment.reply", comment.content));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                )))));

        CloudMusic.then(Volume.executes(context -> {
            context.getSource().sendFeedback(Text.translatable("cloudmusic.info.volume", Configs.PLAY.VOLUME.getIntegerValue()));
            return Command.SINGLE_SUCCESS;
        }));

        CloudMusic.then(Volume.then(
                argument("volume", IntegerArgumentType.integer(0, 100)).executes(contextData -> {
                    runCommand(contextData, context -> {
                        player.volumeSet(IntegerArgumentType.getInteger(context, "volume"));
                    });
                    return Command.SINGLE_SUCCESS;
                }))
        );

        CloudMusic.then(Page.then(literal("prev").executes(context -> {
            Page p = page;
            if (p == null) {
                return Command.SINGLE_SUCCESS;
            }
            MinecraftClient.getInstance().execute(() -> p.prev(context.getSource()));
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(Page.then(literal("next").executes(contextData -> {
            runCommand(contextData, context -> {
                Page p = page;
                if (p == null) {
                    return;
                }
                MinecraftClient.getInstance().execute(() -> p.next(context.getSource()));
            });
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(Page.then(literal("to").then(
                argument("page", IntegerArgumentType.integer()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        Page p = page;
                        if (p == null) {
                            return;
                        }
                        int in = IntegerArgumentType.getInteger(context, "page") - 1;
                        MinecraftClient.getInstance().execute(() -> p.to(in, context.getSource()));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Playing.executes(context -> {
            IMusic music = player.getPlayingMusic();
            if (music != null) {
                MinecraftClient.getInstance().execute(() -> music.printToChatHud(context.getSource()));
            }
            return Command.SINGLE_SUCCESS;
        }));

        CloudMusic.then(Playing.then(literal("all").executes(context -> {
            Page p = player.playingAll();
            p.setInfoText(Text.translatable("cloudmusic.info.page.playing.all"));
            page = p;
            lookPage(context, p);
            return Command.SINGLE_SUCCESS;
        })));

        CloudMusic.then(Login.then(literal("email").then(
                argument("email", StringArgumentType.string()).then(
                        argument("password", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                resetCookie(loginMusic163.email(StringArgumentType.getString(context, "email"), StringArgumentType.getString(context, "password")));
                                feedback(context, Text.translatable("cloudmusic.info.command.login", my.name));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                ))
        ));

        CloudMusic.then(Login.then(literal("captcha").then(
                argument("phone", LongArgumentType.longArg()).executes(contextData -> {
                    runCommand(contextData, context -> {
                        loginMusic163.sendCaptcha(LongArgumentType.getLong(context, "phone"), Configs.LOGIN.COUNTRY_CODE.getIntegerValue());
                        feedback(context, Text.translatable("cloudmusic.info.command.login.send.captcha"));
                    });
                    return Command.SINGLE_SUCCESS;
                })
        )));

        CloudMusic.then(Login.then(literal("captcha").then(
                argument("phone", LongArgumentType.longArg()).then(
                        argument("captcha", IntegerArgumentType.integer()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                resetCookie(loginMusic163.cellphone(LongArgumentType.getLong(context, "phone"), IntegerArgumentType.getInteger(context, "captcha"), Configs.LOGIN.COUNTRY_CODE.getIntegerValue()));
                                feedback(context, Text.translatable("cloudmusic.info.command.login", my.name));
                            });
                            return Command.SINGLE_SUCCESS;
                        }))
        )));

        CloudMusic.then(Login.then(literal("phone").then(
                argument("phone", LongArgumentType.longArg()).then(
                        argument("password", StringArgumentType.string()).executes(contextData -> {
                            runCommand(contextData, context -> {
                                resetCookie(loginMusic163.cellphone(LongArgumentType.getLong(context, "phone"), StringArgumentType.getString(context, "password"), Configs.LOGIN.COUNTRY_CODE.getIntegerValue()));
                                feedback(context, Text.translatable("cloudmusic.info.command.login", my.name));
                            });
                            return Command.SINGLE_SUCCESS;
                        }))
        )));

        CloudMusic.then(Login.then(literal("qr").executes(contextData -> {
            runCommand(contextData, context -> {
                String qrKey = loginMusic163.qrKey();
                loginMusic163.getQRCodeTexture(qrKey);
                try {
                    loadQRCode = true;
                    resetCookie(loginMusic163.qrLogin(qrKey));
                } catch (Exception err) {
                    String msg = err.getMessage() != null ? err.getMessage() : err.toString();
                    feedback(context, Text.literal(msg));
                    return;
                } finally {
                    loadQRCode = false;
                }
                feedback(context, Text.translatable("cloudmusic.info.command.login", my.name));
            });
            return Command.SINGLE_SUCCESS;
        })));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    CloudMusic
                            .then(literal("stop").executes(context -> {
                                player.stop();
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(literal("continue").executes(context -> {
                                player.continues();
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(literal("prev").executes(context -> {
                                player.prev();
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(literal("next").executes(context -> {
                                player.next();
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(literal("to").then(
                                    argument("index", IntegerArgumentType.integer()).executes(context -> {
                                        player.to(IntegerArgumentType.getInteger(context, "index"));
                                        return Command.SINGLE_SUCCESS;
                                    })
                            ))
                            .then(literal("del").executes(context -> {
                                player.deletePlayingMusic();
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(literal("trash").executes(contextData -> {
                                IMusic music = player.getPlayingMusic();
                                if (!(music instanceof Music)) {
                                    return Command.SINGLE_SUCCESS;
                                }
                                player.deletePlayingMusic();
                                runCommand(contextData, context -> {
                                    ((Music) music).addTrashCan();
                                    feedback(context, Text.translatable("cloudmusic.info.command.trash", music.getName()));
                                });
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(literal("random").executes(context -> {
                                player.randomPlay();
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(literal("exit").executes(context -> {
                                resetPlayer(new ArrayList<>());
                                return Command.SINGLE_SUCCESS;
                            }))
            );
        });
    }
}