package com.androidtv.bhagavadgita.presenter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseGridView;
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

        gridView.setFocusScrollStrategy(VerticalGridView.FOCUS_SCROLL_ALIGNED);
        gridView.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_LOW_EDGE);
        gridView.setWindowAlignmentOffset(0);
        gridView.setWindowAlignmentOffsetPercent(0f);
        gridView.setItemAlignmentOffsetPercent(0f);
        gridView.setVerticalSpacing(5);

        mBorderDecoration = new MyVerticalBorderDecoration(gridView.getContext());
        gridView.addItemDecoration(mBorderDecoration);

        gridView.setOnKeyInterceptListener(event -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

            RecyclerView.Adapter<?> a = gridView.getAdapter();
            int count = (a != null) ? a.getItemCount() : 0;
            if (count < 2) return false;

            int pos = gridView.getSelectedPosition();
            int key = event.getKeyCode();

            if (key == KeyEvent.KEYCODE_DPAD_UP && pos == 0) {
                gridView.setSelectedPosition(count - 1, new androidx.leanback.widget.ViewHolderTask() {
                    @Override public void run(RecyclerView.ViewHolder vh) {
                        if (vh != null) vh.itemView.requestFocus();
                    }
                });
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_DOWN && pos == count - 1) {
                gridView.setSelectedPosition(0, new androidx.leanback.widget.ViewHolderTask() {
                    @Override public void run(RecyclerView.ViewHolder vh) {
                        if (vh != null) vh.itemView.requestFocus();
                    }
                });
                return true;
            }
            return false;
        });

        // 1. Selection change: only select if view has dimensions
        gridView.setOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(RecyclerView parent, RecyclerView.ViewHolder child, int position, int subposition) {
                if (child != null && child.itemView != null) {
                    mBorderDecoration.setFocusedChild(gridView, child.itemView);
                } else if (parent.getChildCount() == 0) {
                    mBorderDecoration.clearBorder(gridView);
                }
            }
        });

        // 2. Focus change listener
        gridView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mBorderDecoration.refreshFocus(gridView);
            } else {
                mBorderDecoration.setFocusState(gridView, false);
            }
        });

        // 3. Listen for adapter assignment to catch data insertion
        RecyclerView.Adapter<?> adapter = gridView.getAdapter();
        if (adapter != null) {
            registerDataObserver(gridView, adapter);
        } else {
            gridView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int l, int t, int r, int b, int ol, int ot, int or, int ob) {
                    RecyclerView.Adapter<?> newAdapter = gridView.getAdapter();
                    if (newAdapter != null) {
                        registerDataObserver(gridView, newAdapter);
                        gridView.removeOnLayoutChangeListener(this);
                    }
                }
            });
        }
    }

    private void registerDataObserver(final VerticalGridView gridView, final RecyclerView.Adapter<?> adapter) {
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                checkAndApplyFocus(gridView);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                checkAndApplyFocus(gridView);
            }
        });

        if (adapter.getItemCount() > 0) {
            checkAndApplyFocus(gridView);
        }
    }

    private void checkAndApplyFocus(final VerticalGridView gridView) {
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (gridView.getChildCount() > 0) {
                    int selected = gridView.getSelectedPosition();
                    RecyclerView.ViewHolder vh = gridView.findViewHolderForAdapterPosition(
                            selected != RecyclerView.NO_POSITION ? selected : 0);

                    View target = (vh != null) ? vh.itemView : gridView.getChildAt(0);
                    if (target != null && target.getWidth() > 0 && target.getHeight() > 0) {
                        mBorderDecoration.setFocusedChild(gridView, target);
                    }
                }
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

    public static class MyVerticalBorderDecoration extends RecyclerView.ItemDecoration {

        private final Drawable mBorderDrawable;
        private final Rect mBounds = new Rect();
        private View mCurrentFocusedView = null;

        public MyVerticalBorderDecoration(Context context) {
            mBorderDrawable = ContextCompat.getDrawable(context, R.drawable.ic_action_focus_border);
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if (mBorderDrawable == null) return;

            // 1. Never draw if the grid has no items or does not currently hold focus
            RecyclerView.Adapter<?> adapter = parent.getAdapter();
            if (adapter == null || adapter.getItemCount() == 0 || parent.getChildCount() == 0) {
                return;
            }

            // 2. Only draw if the grid itself or one of its descendants has focus
            if (!parent.hasFocus()) {
                return;
            }

            View target = mCurrentFocusedView;

            // Verify target is valid, attached, and measured
            if (target == null || !target.isAttachedToWindow() || !target.hasFocus()) {
                // Find the actual focused child from the window hierarchy
                target = parent.getFocusedChild();
            }

            // If no child has focus, abort completely (eliminates the empty box)
            if (target == null || target.getWidth() <= 0 || target.getHeight() <= 0) {
                return;
            }

            // 3. Draw only around the confirmed focused child
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
            if (child != null && child.hasFocus()) {
                mCurrentFocusedView = child;
            } else {
                mCurrentFocusedView = null;
            }
            parent.invalidate();
        }

        public void setFocusState(RecyclerView parent, boolean hasFocus) {
            if (!hasFocus) {
                mCurrentFocusedView = null;
            }
            parent.invalidate();
        }

        public void clearBorder(RecyclerView parent) {
            mCurrentFocusedView = null;
            parent.invalidate();
        }

        public void refreshFocus(RecyclerView parent) {
            parent.post(() -> {
                if (parent.getChildCount() == 0) {
                    clearBorder(parent);
                    return;
                }
                View focusedChild = parent.getFocusedChild();
                if (focusedChild == null) {
                    int selected = ((VerticalGridView) parent).getSelectedPosition();
                    RecyclerView.ViewHolder vh = parent.findViewHolderForAdapterPosition(
                            selected != RecyclerView.NO_POSITION ? selected : 0);
                    focusedChild = (vh != null) ? vh.itemView : parent.getChildAt(0);
                }
                if (focusedChild != null && focusedChild.getWidth() > 0 && focusedChild.getHeight() > 0) {
                    setFocusedChild(parent, focusedChild);
                }
            });
        }
    }
}