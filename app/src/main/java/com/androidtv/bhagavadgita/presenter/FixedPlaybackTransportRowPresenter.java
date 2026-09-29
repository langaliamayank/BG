package com.androidtv.bhagavadgita.presenter;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.PlaybackTransportRowPresenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.playback.HeatmapSeekBar;

import org.jspecify.annotations.NonNull;

public class FixedPlaybackTransportRowPresenter extends PlaybackTransportRowPresenter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long RETRY_DELAY_MS = 150;
    private static final long VERIFY_DELAY_MS = 80;

    private float[] mHeatData;
    private ViewHolder mActiveViewHolder; // Holds the currently active bound ViewHolder

    public interface OnSecondaryActionClickListener {
        void onActionClicked(View view);
    }

    private OnSecondaryActionClickListener actionClickListener;

    public void setOnSecondaryActionClickListener(OnSecondaryActionClickListener listener) {
        this.actionClickListener = listener;
    }

    /**
     * Sets heatmap wave data and pushes it immediately to the active view if bound.
     */
    public void setHeatmapData(float[] data) {
        this.mHeatData = data;

        // When data arrives asynchronously via callback, immediately bind and redraw
        if (mActiveViewHolder != null) {
            bindHeatmapToView(mActiveViewHolder);
        }
    }

    /**
     * Binds the heatmap data directly into the HeatmapSeekBar and invalidates the view.
     */
    private void bindHeatmapToView(ViewHolder vh) {
        if (vh == null || vh.view == null) return;

        View hmSeekBar = vh.view.findViewById(R.id.heatmapSeekBar);
        if (hmSeekBar instanceof HeatmapSeekBar) {
            HeatmapSeekBar heatmapSeekBar = (HeatmapSeekBar) hmSeekBar;
            heatmapSeekBar.setHeatData(mHeatData);
            heatmapSeekBar.setVisibility(mHeatData != null && mHeatData.length > 0 ? View.VISIBLE : View.GONE);
            heatmapSeekBar.invalidate();
        }
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.@NonNull ViewHolder holder, @NonNull Object item) {
        super.onBindRowViewHolder(holder, item);

        View card = holder.view.findViewById(R.id.controls_card);
        if (card != null) card.setVisibility(View.VISIBLE);

        if (holder instanceof ViewHolder) {
            mActiveViewHolder = (ViewHolder) holder;
            // Apply data if it arrived before or during row binding
            bindHeatmapToView(mActiveViewHolder);

            ViewGroup root = (ViewGroup) mActiveViewHolder.view;
            int[] buttonIds = {
                    R.id.btn_repeat,
                    R.id.btn_shuffle,
                    R.id.btn_settings,
                    R.id.btn_quality,
                    R.id.btn_playpause,
                    R.id.btn_next,
                    R.id.btn_prev
            };

            for (int id : buttonIds) {
                View btn = root.findViewById(id);
                if (btn != null) {
                    btn.setOnClickListener(v -> {
                        if (actionClickListener != null) {
                            actionClickListener.onActionClicked(v);
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onUnbindRowViewHolder(RowPresenter.@NonNull ViewHolder holder) {
        super.onUnbindRowViewHolder(holder);

        if (holder instanceof ViewHolder) {
            ViewHolder vh = (ViewHolder) holder;
            View progressBar = vh.view.findViewById(R.id.heatmapSeekBar);
            if (progressBar instanceof HeatmapSeekBar) {
                ((HeatmapSeekBar) progressBar).setHeatData(null);
            }

            if (mActiveViewHolder == holder) {
                mActiveViewHolder = null;
            }
        }
    }

    @Override
    protected void onRowViewAttachedToWindow(RowPresenter.ViewHolder vh) {
        super.onRowViewAttachedToWindow(vh);
        if (vh instanceof ViewHolder) {
            scheduleFocusAttempt((ViewHolder) vh, 0);
        }
    }

    private void scheduleFocusAttempt(ViewHolder vh, int attempt) {
        if (vh == null || vh.view == null || attempt >= MAX_ATTEMPTS) return;

        vh.view.postDelayed(() -> {
            View target = findPlayPauseFocusTarget(vh);
            if (target == null) {
                scheduleFocusAttempt(vh, attempt + 1);
                return;
            }

            target.setFocusable(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                target.setFocusedByDefault(true);
            }
            target.requestFocus();

            target.postDelayed(() -> {
                if (!target.isFocused()) {
                    scheduleFocusAttempt(vh, attempt + 1);
                }
            }, VERIFY_DELAY_MS);

        }, attempt == 0 ? 0 : RETRY_DELAY_MS);
    }

    private View findPlayPauseFocusTarget(ViewHolder vh) {
        if (vh.view == null) return null;

        View controlsDock = vh.view.findViewById(androidx.leanback.R.id.controls_dock);
        if (!(controlsDock instanceof ViewGroup)) return null;
        ViewGroup dock = (ViewGroup) controlsDock;
        if (dock.getChildCount() == 0) return null;

        View controlBarView = dock.getChildAt(0);
        if (!(controlBarView instanceof ViewGroup)) return null;
        ViewGroup bar = (ViewGroup) controlBarView;
        if (bar.getChildCount() == 0) return null;

        int targetIndex = -1;
        Row row = vh.getRow();
        if (row instanceof PlaybackControlsRow) {
            ArrayObjectAdapter actions =
                    (ArrayObjectAdapter) ((PlaybackControlsRow) row).getPrimaryActionsAdapter();
            if (actions != null) {
                for (int i = 0; i < actions.size(); i++) {
                    if (actions.get(i) instanceof PlaybackControlsRow.PlayPauseAction) {
                        targetIndex = i;
                        break;
                    }
                }
            }
        }

        if (targetIndex < 0 || targetIndex >= bar.getChildCount()) {
            targetIndex = bar.getChildCount() > 1 ? bar.getChildCount() / 2 : 0;
        }

        View itemRoot = bar.getChildAt(targetIndex);
        if (itemRoot == null) return null;

        View button = itemRoot.findViewById(R.id.button);
        return button != null ? button : itemRoot;
    }
}