package com.androidtv.bhagavadgita.playback;

import android.content.Context;

import androidx.leanback.media.MediaPlayerAdapter;
import androidx.leanback.widget.PlaybackControlsRow;

public class AwesomeMediaPlayerAdapter extends MediaPlayerAdapter {

    private final static long DELAY_SKIP = 10_000;
    public boolean shuffleEnabled = false;
    public int repeatMode = 0;

    /**
     * Constructor.
     *
     * @param context
     */
    public AwesomeMediaPlayerAdapter(Context context) {
        super(context);
    }

    public void fastForward() {
        seekTo(getCurrentPosition() + DELAY_SKIP);
    }

    public void rewind() {
        seekTo(getCurrentPosition() - DELAY_SKIP);
    }

    public void setRepeatAction(int repeatActionIndex) {
        repeatMode = repeatActionIndex;
    }

    public void setShuffleAction(int shuffleActionIndex) {
        shuffleEnabled = shuffleActionIndex == PlaybackControlsRow.ShuffleAction.INDEX_ON;
    }

    @Override
    protected boolean onError(int what, int extra) {
        return super.onError(what, extra);
    }
}
