package com.androidtv.bhagavadgita.comman;

import android.content.Context;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.androidtv.bhagavadgita.model.MediaCard;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    public static PlayerManager instance;
    private ExoPlayer player;
    private final Context appContext;

    private PlayerManager(Context context) {
        appContext = context.getApplicationContext();
        player = new ExoPlayer.Builder(appContext)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(), true)               // handles audio focus
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)     // keeps CPU/Wi-Fi awake for streaming
                .build();
    }

    public static synchronized PlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerManager(context);
        }
        return instance;
    }

    public ExoPlayer getPlayer() {
        if (player == null) {
            // Recreate player if it was released
            player = new ExoPlayer.Builder(appContext).build();
        }
        return player;
    }

    public void playMedia(String writer, String url, String title, String subtitle, String artworkUrl) {
        MediaMetadata metadata = new MediaMetadata.Builder()
                .setWriter(writer)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setArtworkUri(artworkUrl != null ? android.net.Uri.parse(artworkUrl) : null)
                .build();

        MediaItem item = new MediaItem.Builder()
                .setUri(url)
                .setMediaId(title)
                .setMediaMetadata(metadata)
                .build();

        getPlayer().setMediaItem(item);
        getPlayer().prepare();
//        getPlayer().play();
    }

    public void playMediaList(List<MediaCard> mediaCardList) {

        List<MediaItem> itemList = new ArrayList<>();
        for(MediaCard mediaCard : mediaCardList){
            MediaMetadata metadata = new MediaMetadata.Builder()
                    .setWriter(mediaCard.getWriter())
                    .setTitle(mediaCard.getTitle())
                    .setSubtitle(mediaCard.getSubtitle())
                    .setArtworkUri(mediaCard.getImageUri() != null ? mediaCard.getImageUri() : null)
                    .build();

            MediaItem item = new MediaItem.Builder()
                    .setUri(mediaCard.getUrl())
                    .setMediaId(mediaCard.getTitle())
                    .setMediaMetadata(metadata)
                    .build();

            itemList.add(item);
        }

        getPlayer().setMediaItems(itemList);
        getPlayer().prepare();
        getPlayer().play();
    }

    public void resume() {
        if (!getPlayer().isPlaying()) {
            getPlayer().play();
        }
    }

    public void pause() {
        if (getPlayer().isPlaying()) {
            getPlayer().pause();
        }
    }

    public void stop() {
        getPlayer().stop();
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null; // 🔑 So next getPlayer() call recreates it
        }

        instance = null; // fully reset singleton
    }

    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    public long getDuration() {
        if (player != null && player.getPlaybackState() == Player.STATE_READY) {
            long duration = player.getDuration();
            return duration > 0 ? duration : 0;
        }
        return 0;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    private SongsResultModel mPlayData;
    private List<SongsResultModel> mPlaylist;
    private int mCurrentIndex = 0;

    public SongsResultModel getPlayData() {
        return mPlayData;
    }

    public void setPlayData(SongsResultModel mPlayData) {
        this.mPlayData = mPlayData;
    }

    public void setPlaylist(List<SongsResultModel> playlist) {
        this.mPlaylist = playlist;
    }

    public List<SongsResultModel> getPlaylist() {
        return mPlaylist;
    }

    public void setCurrentIndex(int index) {
        this.mCurrentIndex = index;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }
}