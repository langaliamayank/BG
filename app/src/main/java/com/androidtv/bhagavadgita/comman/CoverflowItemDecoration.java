package com.androidtv.bhagavadgita.comman;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.leanback.widget.HorizontalGridView;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.leanback.widget.HorizontalGridView;
import androidx.recyclerview.widget.RecyclerView;

public class CoverflowItemDecoration extends RecyclerView.ItemDecoration {

    private final int mTargetSpacingPx; // Exact visible gap between poster edges

    public CoverflowItemDecoration(int targetSpacingPx) {
        this.mTargetSpacingPx = targetSpacingPx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        HorizontalGridView hgv = (HorizontalGridView) parent;
        int position = parent.getChildAdapterPosition(view);
        int selectedPosition = hgv.getSelectedPosition();
        int distance = position - selectedPosition;
        int absDistance = Math.abs(distance);

        if (distance <= 0) {
            // First focused item or items to the left stay anchored
            outRect.left = 0;
            outRect.right = 0;
        } else {
            // Calculate scale step of current item and previous item
            float currentScale = getScaleForDistance(absDistance);
            float prevScale = getScaleForDistance(Math.abs(distance - 1));

            // Measure unscaled card width
            int cardWidth = view.getLayoutParams().width > 0 ?
                    view.getLayoutParams().width : view.getWidth();

            // Calculate the empty gap created on the left/right when views shrink
            float currentScaleOffset = (cardWidth * (1.0f - currentScale)) / 2.0f;
            float prevScaleOffset = (cardWidth * (1.0f - prevScale)) / 2.0f;

            // Equal Gap Formula: Target Visible Gap - Scale Shrink Empty Space
            int equalMargin = (int) (mTargetSpacingPx - currentScaleOffset - prevScaleOffset);

            outRect.left = equalMargin;
            outRect.right = 0;
        }
    }

    /**
     * Matches the step-down scale calculation in MyListRowPresenter
     */
    private float getScaleForDistance(int absDistance) {
        return Math.max(0.35f, 1.00f - (absDistance * 0.15f));
    }
}