package com.androidtv.bhagavadgita.comman;

import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class AudioUtils {

    public interface DurationCallback {
        void onDurationRetrieved(String formattedDuration);
    }

    public static void getAudioDurationFromUrl(Context context, String audioUrl, DurationCallback callback) {
        ExoPlayer player = new ExoPlayer.Builder(context).build();

        MediaItem mediaItem = MediaItem.fromUri(audioUrl);
        player.setMediaItem(mediaItem);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    long durationMs = player.getDuration();
                    int totalSeconds = (int) (durationMs / 1000);

                    String formatted = formatAudioDuration(totalSeconds);
                    callback.onDurationRetrieved(formatted);

                    player.release();
                }
            }
        });
    }

    private static String formatAudioDuration(int totalSeconds) {
        if (totalSeconds <= 0) return "0 sec audio";

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return (minutes > 0)
                    ? String.format("%d hr %d mins audio", hours, minutes)
                    : String.format("%d hr audio", hours);
        }

        if (minutes > 0) {
            String minLabel = (minutes == 1) ? "min" : "mins";
            return String.format("%d %s audio", minutes, minLabel);
        }

        return String.format("%d sec audio", seconds);
    }
}