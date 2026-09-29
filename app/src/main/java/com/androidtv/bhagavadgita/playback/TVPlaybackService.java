package com.androidtv.bhagavadgita.playback;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.androidtv.bhagavadgita.comman.PlayerManager;

/**
 * Keeps the shared ExoPlayer (PlayerManager) alive while the UI is gone.
 * The player itself is NOT released here - PlayerManager owns it.
 */
@UnstableApi
public class TVPlaybackService extends MediaSessionService {
    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        ExoPlayer player = PlayerManager.getInstance(this).getPlayer();
        mediaSession = new MediaSession.Builder(this, player).build();
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    /** App swiped away from recents: stop the service if nothing is playing. */
    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        Player player = mediaSession != null ? mediaSession.getPlayer() : null;
        if (player == null || !player.getPlayWhenReady() || player.getMediaItemCount() == 0) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            Player player = mediaSession.getPlayer();
            // Don't leave audio playing with no session/notification. Do NOT release():
            // the player belongs to PlayerManager and must stay reusable.
            player.stop();
            player.clearMediaItems();
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }
}