package com.androidtv.bhagavadgita.playback;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.leanback.media.PlaybackGlueHost;
import androidx.leanback.media.PlayerAdapter;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.androidtv.bhagavadgita.comman.PlayerManager;

import java.util.Objects;

/**
 * Leanback adapter over the SHARED ExoPlayer owned by PlayerManager.
 * Closing the screen only detaches the UI; it never stops or releases the player.
 */
@UnstableApi
public class ExoPlayerAdapter extends PlayerAdapter implements Player.Listener {

    private static final long SKIP_MS = 10_000;
    private static final long PROGRESS_INTERVAL_MS = 100;

    private final Context mContext;
    private final ExoPlayer mPlayer;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private Uri mMediaSourceUri = null;
    private boolean mInitialized = false;

    // Used by AwesomeTransportControlGlue (same names as the old adapter)
    public boolean shuffleEnabled = false;
    public int repeatMode = PlaybackControlsRow.RepeatAction.INDEX_NONE;

    private final Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            getCallback().onCurrentPositionChanged(ExoPlayerAdapter.this);
            getCallback().onBufferedPositionChanged(ExoPlayerAdapter.this);
            mHandler.postDelayed(this, PROGRESS_INTERVAL_MS);
        }
    };

    public ExoPlayerAdapter(Context context) {
        mContext = context.getApplicationContext();
        mPlayer = PlayerManager.getInstance(mContext).getPlayer(); // shared, NOT created here
    }

    // ---------- host attach / detach ----------

    @Override
    public void onAttachedToHost(PlaybackGlueHost host) {
        super.onAttachedToHost(host);
        mPlayer.addListener(this);
    }

    @Override
    public void onDetachedFromHost() {
        // UI is going away. Only unhook the UI. Do NOT stop/release/clear the player.
        mHandler.removeCallbacks(mProgressRunnable);
        mPlayer.removeListener(this);
        super.onDetachedFromHost();
    }

    // ---------- loading ----------

    public void setDataSource(Uri uri) {
        if (uri == null || Objects.equals(mMediaSourceUri, uri)) return;
        mMediaSourceUri = uri;

        if (isAlreadyLoadedInPlayer(uri)) {
            attachToLoadedItem();      // reopened screen, keep playing, don't restart
        } else {
            prepareMediaForPlaying();  // new song
        }
    }

    private boolean isAlreadyLoadedInPlayer(Uri uri) {
        MediaItem cur = mPlayer.getCurrentMediaItem();
        int state = mPlayer.getPlaybackState();
        return cur != null
                && cur.localConfiguration != null
                && uri.equals(cur.localConfiguration.uri)
                && (state == Player.STATE_READY || state == Player.STATE_BUFFERING);
    }

    private void attachToLoadedItem() {
        mInitialized = mPlayer.getPlaybackState() == Player.STATE_READY;
        if (mInitialized) {
            getCallback().onPreparedStateChanged(this);
            getCallback().onDurationChanged(this);
        }
        getCallback().onBufferingStateChanged(this, !mInitialized);
        getCallback().onPlayStateChanged(this);
    }

    private void prepareMediaForPlaying() {
        mInitialized = false;
        // Stopping here is intentional: we are switching to a different song.
        mPlayer.stop();
        mPlayer.clearMediaItems();
        mPlayer.setMediaSource(onCreateMediaSource(mMediaSourceUri));
        mPlayer.prepare();
        getCallback().onBufferingStateChanged(this, true);
        getCallback().onPlayStateChanged(this);
    }

    public MediaSource onCreateMediaSource(Uri uri) {
        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(mContext);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        if (uri.toString().contains(".m3u8")) {
            return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        }
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
    }

    // ---------- Player.Listener ----------

    @Override
    public void onPlaybackStateChanged(int state) {
        if (state == Player.STATE_READY && !mInitialized) {
            mInitialized = true;
            getCallback().onPreparedStateChanged(this);
            getCallback().onDurationChanged(this);
        } else if (state == Player.STATE_ENDED) {
            getCallback().onPlayCompleted(this);
        }
        getCallback().onBufferingStateChanged(this, state == Player.STATE_BUFFERING || !mInitialized);
        getCallback().onPlayStateChanged(this);
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        // Keeps the UI in sync if playback is paused/resumed from the notification or remote
        getCallback().onPlayStateChanged(this);
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        getCallback().onError(this, error.errorCode, error.getMessage());
    }

    // ---------- PlayerAdapter ----------

    @Override
    public boolean isPrepared() {
        return mInitialized;   // REQUIRED so glue.playWhenPrepared() waits properly
    }

    @Override
    public void play() {
        mPlayer.play();
        getCallback().onPlayStateChanged(this);
    }

    @Override
    public void pause() {
        mPlayer.pause();
        getCallback().onPlayStateChanged(this);
    }

    @Override
    public boolean isPlaying() {
        int s = mPlayer.getPlaybackState();
        return mPlayer.getPlayWhenReady() && (s == Player.STATE_READY || s == Player.STATE_BUFFERING);
    }

    @Override
    public void setProgressUpdatingEnabled(boolean enabled) {
        mHandler.removeCallbacks(mProgressRunnable);
        if (enabled) mHandler.post(mProgressRunnable);
    }

    @Override
    public void seekTo(long positionMs) {
        if (mInitialized) mPlayer.seekTo(Math.max(0, positionMs));
    }

    @Override
    public long getDuration() {
        if (!mInitialized) return -1;
        long d = mPlayer.getDuration();
        return d == C.TIME_UNSET ? -1 : d;
    }

    @Override
    public long getCurrentPosition() {
        return mInitialized ? mPlayer.getCurrentPosition() : -1;
    }

    @Override
    public long getBufferedPosition() {
        return mPlayer.getBufferedPosition();
    }

    // ---------- helpers the glue already calls ----------

    public void fastForward() {
        long d = getDuration();
        long target = getCurrentPosition() + SKIP_MS;
        seekTo(d > 0 ? Math.min(target, d) : target);
    }

    public void rewind() {
        seekTo(getCurrentPosition() - SKIP_MS);
    }

    public void setRepeatAction(int repeatActionIndex) {
        repeatMode = repeatActionIndex;
    }

    public void setShuffleAction(int shuffleActionIndex) {
        shuffleEnabled = shuffleActionIndex == PlaybackControlsRow.ShuffleAction.INDEX_ON;
    }
}