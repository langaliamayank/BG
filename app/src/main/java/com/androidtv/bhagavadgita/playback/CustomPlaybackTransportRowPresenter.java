package com.androidtv.bhagavadgita.playback;

import androidx.leanback.widget.PlaybackTransportRowPresenter;
import androidx.leanback.widget.RowPresenter;

public class CustomPlaybackTransportRowPresenter extends PlaybackTransportRowPresenter {
    private float[] mHeatData;

    public CustomPlaybackTransportRowPresenter() {
        super();
        // Tell the presenter to use your custom layout instead of the default
        setHeaderPresenter(null);
    }

    public void setHeatmapData(float[] data) {
        this.mHeatData = data;

    }

    protected void onBindRowViewHolder(RowPresenter.ViewHolder vh, Object item) {
        // 1. Call super first to let Leanback set up standard playback logic
        super.onBindRowViewHolder(vh, item);

        ViewHolder playbackViewHolder =
                (ViewHolder) vh;

        android.view.View progressBar = playbackViewHolder.view.findViewById(
                androidx.leanback.R.id.playback_progress);

        if (progressBar instanceof HeatmapSeekBar) {
            HeatmapSeekBar heatmapSeekBar = (HeatmapSeekBar) progressBar;

            // 3. Apply the data
            if (mHeatData != null) {
                heatmapSeekBar.setHeatData(mHeatData);
            }
        }
    }

    @Override
    protected void onUnbindRowViewHolder(RowPresenter.ViewHolder vh) {

        ViewHolder playbackViewHolder =
                (ViewHolder) vh;

        android.view.View progressBar = playbackViewHolder.view.findViewById(
                androidx.leanback.R.id.playback_progress);

        if (progressBar instanceof HeatmapSeekBar) {
            ((HeatmapSeekBar) progressBar).setHeatData(null);
        }
        super.onUnbindRowViewHolder(vh);
    }
}