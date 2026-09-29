package com.androidtv.bhagavadgita.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.CommandButton;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionCommands;
import androidx.media3.session.SessionResult;

import com.androidtv.bhagavadgita.MainNewActivity;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public class PlaybackService extends MediaSessionService implements MediaSession.Callback {

    private static Player playerInstance;
    private static MediaSession mediaSessionInstance;

    private Player player;
    private static MediaSession mediaSession;
    private long currentSongPosition = 0L;
    private int seekForward = 5000;
    private int seekBackward = 5000;
    private boolean isPauseFromLoss = false;

    private AudioManager audioManager;
    private int audioFocusState = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    private AudioFocusRequest focusRequest;
    private final AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();

    private final List<Enums.NotificationPlayerCustomCommandButton>
            notificationPlayerCustomCommandButtons = new ArrayList<>(EnumSet.allOf(Enums.NotificationPlayerCustomCommandButton.class));

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initializeSessionAndPlayer();
    }

    @SuppressLint("ObsoleteSdkInt")
    private void releaseAudioFocus() {
        // Uncomment and complete if needed based on your version handling
        if (Build.VERSION.SDK_INT >= 26) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        } else {
            audioManager.abandonAudioFocus(focusChangeListener);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void setupAndRequestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build();
            audioFocusState = audioManager.requestAudioFocus(focusRequest);
        } else {
            audioFocusState = audioManager.requestAudioFocus(focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
            if (playWhenReady) {
                setupAndRequestAudioFocus();
            }
        }
    };

    private final AudioManager.OnAudioFocusChangeListener focusChangeListener = state -> {
        audioFocusState = state;
        switch (state) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (isPauseFromLoss) {
                    player.play();
                    isPauseFromLoss = false;
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (player.isPlaying()) {
                    player.pause();
                    isPauseFromLoss = true;
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                if (player.isPlaying()) {
                    player.pause();
                }
                break;
        }
    };

    private PendingIntent getActivityPendingIntent() {
        Intent intent = new Intent(this, MainNewActivity.class);  // Replace with your target activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Ensure a fresh activity stack
        return PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void initializeSessionAndPlayer() {
        if (playerInstance == null) {
            playerInstance = new ExoPlayer.Builder(this).build();
            playerInstance.addListener(playerListener);
        }
        player = playerInstance;

        if (mediaSessionInstance == null) {
            mediaSessionInstance = new MediaSession.Builder(this, player)
                    .setCallback(this)
                    .setSessionActivity(getActivityPendingIntent()).build();
        }
        mediaSession = mediaSessionInstance;

//        List<CommandButton> commanButtoList = new ArrayList<>();
//        for (Enums.NotificationPlayerCustomCommandButton commandButton : notificationPlayerCustomCommandButtons) {
//            commanButtoList.add(commandButton.getCommandButton());
//        }
//
//        mediaSession.setCustomLayout(commanButtoList);
    }

    @Override
    public ListenableFuture<List<MediaItem>> onAddMediaItems(MediaSession mediaSession, MediaSession.ControllerInfo controller, List<MediaItem> mediaItems) {
        ArrayList<MediaItem> updatedMediaItems = new ArrayList<>();
        for (MediaItem item : mediaItems) {
            updatedMediaItems.add(item.buildUpon().setUri(item.mediaId).build());
        }

        return Futures.immediateFuture(updatedMediaItems);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public MediaSession.ConnectionResult onConnect(MediaSession session,
                                                   MediaSession.ControllerInfo controller) {
        MediaSession.ConnectionResult connectionResult = MediaSession.Callback.super.onConnect(session, controller);
        SessionCommands.Builder availableSessionCommands = connectionResult.availableSessionCommands.buildUpon();

        for (Enums.NotificationPlayerCustomCommandButton commandButton : notificationPlayerCustomCommandButtons) {
            if (commandButton.getCommandButton().sessionCommand != null) {
                availableSessionCommands.add(commandButton.getCommandButton().sessionCommand);
            }
        }

        return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands);
    }

    @Override
    public void onPostConnect(MediaSession session, MediaSession.ControllerInfo controller) {
        MediaSession.Callback.super.onPostConnect(session, controller);
        if (!notificationPlayerCustomCommandButtons.isEmpty()) {

            List<CommandButton> commanButtoList = new ArrayList<>();
            for (Enums.NotificationPlayerCustomCommandButton commandButton : notificationPlayerCustomCommandButtons) {
                commanButtoList.add(commandButton.getCommandButton());
            }

            mediaSession.setCustomLayout(commanButtoList);
            if (player.getPlayWhenReady()) {
                setupAndRequestAudioFocus();
            }
        }
    }

    @Override
    public ListenableFuture<SessionResult> onCustomCommand(
            MediaSession session,
            MediaSession.ControllerInfo controller,
            SessionCommand customCommand,
            Bundle args) {

        currentSongPosition = session.getPlayer().getCurrentPosition();

        if (player.getPlayWhenReady()) {
            setupAndRequestAudioFocus();
        }

        if (Enums.NotificationPlayerCustomCommandButton.FAVORITE.customAction.equals(customCommand.customAction)) {
            new CommandButton.Builder()
                    .setDisplayName("Favorite")
                    .setSessionCommand(new SessionCommand("FAVORITE", new Bundle()))
                    .setIconResId(androidx.media3.session.R.drawable.media3_icon_heart_filled)
                    .build();
        }

//        if (Enums.NotificationPlayerCustomCommandButton.REWIND.customAction.equals(customCommand.customAction)) {
//            if (currentSongPosition - seekBackward >= 0) {
//                session.getPlayer().seekTo(currentSongPosition - seekBackward);
//            } else {
//                session.getPlayer().seekTo(0);
//            }
//        }
//
//        if (Enums.NotificationPlayerCustomCommandButton.FORWARD.customAction.equals(customCommand.customAction)) {
//            if (currentSongPosition + seekForward <= session.getPlayer().getDuration()) {
//                session.getPlayer().seekTo(currentSongPosition + seekForward);
//            } else {
//                session.getPlayer().seekTo(player.getDuration());
//            }
//        }

        return Futures.immediateFuture(new SessionResult(SessionResult.RESULT_SUCCESS));
    }

    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    public static boolean isPlaying() {
        return mediaSession.getPlayer() != null && mediaSession.getPlayer().isPlaying();
    }

    public MediaSession getMediaSession() {
        return mediaSession;
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mediaSession != null) {
//            mediaSession.release();
//            player.release();
//            mediaSession = null;
//            playerInstance = null;
//            mediaSessionInstance = null;
//        }
//        releaseAudioFocus();
//    }

//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//        if (mediaSession != null && mediaSession.getPlayer().getPlayWhenReady()) {
//            mediaSession.getPlayer().pause();
//        }
//        stopSelf();
//        releaseAudioFocus();
//    }
}

