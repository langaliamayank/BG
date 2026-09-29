package com.androidtv.bhagavadgita.presenter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.R;

public class MyVerticalGridPresenter extends VerticalGridPresenter {

    private MyVerticalBorderDecoration mBorderDecoration;

    public MyVerticalGridPresenter(int zoom, boolean val) {
        super(zoom, val);
        setShadowEnabled(false);
    }

    public MyVerticalGridPresenter() {
        super(FocusHighlight.ZOOM_FACTOR_NONE, false);
        setShadowEnabled(false);
    }

    @Override
    protected void initializeGridViewHolder(ViewHolder vh) {
        super.initializeGridViewHolder(vh);
        final VerticalGridView gridView = vh.getGridView();

        gridView.setPadding(0, 0, 0, 0);
        gridView.setClipChildren(false);
        gridView.setClipToPadding(false);

        gridView.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_LOW_EDGE);
        gridView.setWindowAlignmentOffset(0);
        gridView.setWindowAlignmentOffsetPercent(0f);
        gridView.setItemAlignmentOffsetPercent(0f);
        gridView.setVerticalSpacing(5);

        mBorderDecoration = new MyVerticalBorderDecoration(gridView.getContext());
        gridView.addItemDecoration(mBorderDecoration);

        // 1. Update target view on selection change
        gridView.setOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(RecyclerView parent, RecyclerView.ViewHolder child, int position, int subposition) {
                if (child != null) {
                    mBorderDecoration.setFocusedChild(gridView, child.itemView);
                }
            }
        });

        // 2. Handle overall Grid focus changes
        gridView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                View child = gridView.getFocusedChild();
                if (child == null && gridView.getChildCount() > 0) {
                    child = gridView.getChildAt(0);
                }
                mBorderDecoration.setFocusedChild(gridView, child);
            } else {
                mBorderDecoration.setFocusState(gridView, false);
            }
        });

        // 3. Automatically refresh focus when items are re-attached (handles backstack returns perfectly)
        gridView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                if (gridView.hasFocus() || gridView.getChildCount() > 0) {
                    mBorderDecoration.refreshFocus(gridView);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
            }
        });
    }

    public MyVerticalBorderDecoration getBorderDecoration() {
        return mBorderDecoration;
    }

    @Override
    public boolean isUsingDefaultShadow() {
        return false;
    }


    public class MyVerticalBorderDecoration extends RecyclerView.ItemDecoration {

        private final Drawable mBorderDrawable;
        private final Rect mBounds = new Rect();
        private View mCurrentFocusedView = null;
        private boolean mHasFocus = false;

        public MyVerticalBorderDecoration(Context context) {
            mBorderDrawable = ContextCompat.getDrawable(context, R.drawable.ic_action_focus_border);
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            // If we don't have focus or a target view, try to find the currently focused child dynamically
            if (mBorderDrawable == null) return;

            View target = mCurrentFocusedView;
            if (target == null || !target.isAttachedToWindow()) {
                target = parent.getFocusedChild();
                if (target == null && parent.getChildCount() > 0) {
                    target = parent.getChildAt(0); // Fallback to first visible item
                }
            }

            if (target == null) return;

            mBounds.set(
                    target.getLeft(),
                    target.getTop(),
                    target.getRight(),
                    target.getBottom()
            );

            mBorderDrawable.setBounds(mBounds);
            mBorderDrawable.setAlpha(255);
            mBorderDrawable.draw(c);
        }

        public void setFocusedChild(RecyclerView parent, View child) {
            mCurrentFocusedView = child;
            mHasFocus = (child != null);
            parent.invalidate();
        }

        public void setFocusState(RecyclerView parent, boolean hasFocus) {
            mHasFocus = hasFocus;
            if (!hasFocus) {
                mCurrentFocusedView = null;
            }
            parent.invalidate();
        }

        public void refreshFocus(RecyclerView parent) {
            parent.post(() -> {
                View focusedChild = parent.getFocusedChild();
                if (focusedChild == null && parent.getChildCount() > 0) {
                    focusedChild = parent.getChildAt(0);
                }
                if (focusedChild != null) {
                    setFocusedChild(parent, focusedChild);
                }
                parent.invalidate();
            });
        }
    }
}