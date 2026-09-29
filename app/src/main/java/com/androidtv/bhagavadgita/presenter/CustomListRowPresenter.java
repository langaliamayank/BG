package com.androidtv.bhagavadgita.presenter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.RowPresenter;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.LogTag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomListRowPresenter extends ListRowPresenter {

    private static final String DESC_CONTINUE_WATCHING = "Continue Watching";
    private static final String DESC_MORE_OPTION = "MORE OPTION";

    private static final float Z_FOCUSED_ROW = 10f;
    private static final float Z_BORDER = 20f;
    private static final float Z_DEFAULT = 0f;
    private static final float ROW_ALPHA_SELECTED = 1.0f;
    private static final float ROW_ALPHA_DIMMED = 0.3f;
    private static final int MORE_OPTION_COLUMNS = 4;

    private final Interpolator mHotstarInterpolator = new PathInterpolator(0.22f, 1f, 0.36f, 1f);
    private final Interpolator mDecelerate = new DecelerateInterpolator();
    private final int mDuration = 300;
    private final int mPaddingStart;

    /** Last focused column, shared by all rows so up/down keeps the same position. */
    private int mLastFocusedPosition = 0;

    /** Per-row focus/scroll listeners so we can unregister them on detach. */
    private final Map<RowPresenter.ViewHolder, RowTracker> mTrackers = new HashMap<>();

    /** Rows laid out with more than one grid row (column position restore is skipped for them). */
    private final Set<RowPresenter.ViewHolder> mMultiRowHolders = new HashSet<>();

//    private static final long KEY_DOWN_INTERVAL_MS = 250; // Adjust speed here
//    private long mLastKeyDownTime = 0L;

    private ArrayObjectAdapter mRowsAdapter;
    private final OnRowActionListener mActionListener;

    public interface OnRowActionListener {
        void onRemoveRow(int position, ListRow row);
    }

    private static class RowTracker {
        ViewTreeObserver.OnGlobalFocusChangeListener focusListener;
        RecyclerView.OnScrollListener scrollListener;
    }

    public CustomListRowPresenter(FragmentActivity activity, OnRowActionListener listener) {
        super(FocusHighlight.ZOOM_FACTOR_NONE);
        this.mActionListener = listener;
        setHeaderPresenter(new MyRowHeaderPresenter((MasterActivity) activity));
        setSelectEffectEnabled(false);
        setKeepChildForeground(false);
        setShadowEnabled(false);

        mPaddingStart = activity.getResources().getDimensionPixelSize(R.dimen.lb_browse_padding_start);
    }

    public CustomListRowPresenter(FragmentActivity activity) {
        this(activity, null);
    }

    public void setRowsAdapter(ArrayObjectAdapter rowsAdapter) {
        this.mRowsAdapter = rowsAdapter;
    }

    // ---------------------------------------------------------------------------------
    // Row creation
    // ---------------------------------------------------------------------------------

    @Override
    protected void initializeRowViewHolder(RowPresenter.ViewHolder holder) {
        super.initializeRowViewHolder(holder);
        final ViewHolder vh = (ViewHolder) holder;

        if (vh.view.findViewById(R.id.row_poster) == null) {
            LogTag.e("CRITICAL: row_poster is NULL! Check your row layout XML file.");
        }

        setupGridView(vh);
        disableClipping((ViewGroup) vh.view);
    }

    private void setupGridView(ViewHolder vh) {
        HorizontalGridView hgv = vh.getGridView();
        Context context = vh.view.getContext();

        int windowAlignmentOffset =
                context.getResources().getDimensionPixelSize(R.dimen.lb_browse_padding_start);

        hgv.setNextFocusUpId(hgv.getId());
        hgv.setNextFocusDownId(hgv.getId());

        hgv.setWindowAlignmentOffsetPercent(0.0f);
        hgv.setWindowAlignmentOffset(windowAlignmentOffset);
        hgv.setWindowAlignment(BaseGridView.WINDOW_ALIGN_NO_EDGE);

        hgv.setItemAlignmentOffsetPercent(0.0f);
        hgv.setItemSpacing(13);

        hgv.setFadingLeftEdge(true);
        hgv.setFadingLeftEdgeOffset(30);
        hgv.setFadingLeftEdgeLength(85);

//        hgv.setItemAnimator(null);
//
//        hgv.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                        long now = System.currentTimeMillis();
//                        if (now - mLastKeyDownTime < KEY_DOWN_INTERVAL_MS) {
//                            return true;
//                        }
//                        mLastKeyDownTime = now;
//                    }
//                }
//                return false;
//            }
//        });
    }

    private void disableClipping(ViewGroup view) {
        view.setClipChildren(false);
        view.setClipToPadding(false);
        if (view.getParent() instanceof ViewGroup) {
            disableClipping((ViewGroup) view.getParent());
        }
    }

    // ---------------------------------------------------------------------------------
    // Attach / detach: register focus tracking
    // ---------------------------------------------------------------------------------

    @Override
    protected void onRowViewAttachedToWindow(RowPresenter.ViewHolder holder) {
        super.onRowViewAttachedToWindow(holder);

        final ViewHolder vh = (ViewHolder) holder;
        final HorizontalGridView hgv = vh.getGridView();
        final View border = vh.view.findViewById(R.id.row_poster);

        // Avoid double registration if attach is called twice without a detach.
        unregisterTracker(holder);

        final RowTracker tracker = new RowTracker();

        // 1) Follow real focus changes.
        tracker.focusListener = (oldFocus, newFocus) -> {
            if (newFocus != null && isInside(hgv, newFocus)) {
                View item = hgv.findContainingItemView(newFocus);
                if (item != null) {
                    int pos = hgv.getChildAdapterPosition(item);
                    if (pos != RecyclerView.NO_POSITION) {
                        mLastFocusedPosition = pos;
                    }
                    updateBorder(border, hgv, item, true, false);
                }
            } else if (oldFocus != null && isInside(hgv, oldFocus)) {
                // Focus left this row's grid (other row, or the remove button).
                animateBorder(border, 0f, Z_DEFAULT, mHotstarInterpolator);
            }
        };
        vh.view.getViewTreeObserver().addOnGlobalFocusChangeListener(tracker.focusListener);

        // 2) When scrolling settles, re-sync the border to the real item position.
        tracker.scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && hgv.hasFocus()) {
                    View focused = hgv.getFocusedChild();
                    if (focused != null) {
                        updateBorder(border, hgv, focused, false, true);
                    }
                }
            }
        };
        hgv.addOnScrollListener(tracker.scrollListener);

        mTrackers.put(holder, tracker);

        // 3) Cover re-attach / rebind while this row already has focus.
        hgv.post(() -> {
            if (hgv.hasFocus()) {
                View focused = hgv.getFocusedChild();
                if (focused != null) {
                    updateBorder(border, hgv, focused, true, false);
                }
            }
        });
    }

    @Override
    protected void onRowViewDetachedFromWindow(RowPresenter.ViewHolder holder) {
        unregisterTracker(holder);
        super.onRowViewDetachedFromWindow(holder);
    }

    private void unregisterTracker(RowPresenter.ViewHolder holder) {
        RowTracker tracker = mTrackers.remove(holder);
        if (tracker == null) return;

        ViewTreeObserver vto = holder.view.getViewTreeObserver();
        if (tracker.focusListener != null && vto.isAlive()) {
            vto.removeOnGlobalFocusChangeListener(tracker.focusListener);
        }
        if (tracker.scrollListener != null) {
            ((ViewHolder) holder).getGridView().removeOnScrollListener(tracker.scrollListener);
        }
    }

    // ---------------------------------------------------------------------------------
    // Bind
    // ---------------------------------------------------------------------------------

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        final ViewHolder vh = (ViewHolder) holder;
        final HorizontalGridView hgv = vh.getGridView();

        vh.view.setAlpha(vh.isSelected() ? ROW_ALPHA_SELECTED : ROW_ALPHA_DIMMED);
        vh.view.setZ(vh.isSelected() ? Z_FOCUSED_ROW : Z_DEFAULT);

        // Only hide the border on bind if this row does not currently own focus,
        // otherwise a rebind would make a visible border disappear.
        View rowPoster = vh.view.findViewById(R.id.row_poster);
        if (rowPoster != null && !hgv.hasFocus()) {
            rowPoster.setAlpha(0f);
            rowPoster.setZ(Z_DEFAULT);
        }

        if (!(item instanceof ListRow)) return;

        final ListRow listRow = (ListRow) item;
        final HeaderItem headerItem = listRow.getHeaderItem();
        final String description = (headerItem != null && headerItem.getDescription() != null)
                ? String.valueOf(headerItem.getDescription()) : "";

        // ---- Grid rows ("More Option" is a multi-row grid, everything else single row) ----
        if (DESC_MORE_OPTION.equals(description) && hgv.getAdapter() != null) {
            int totalItems = hgv.getAdapter().getItemCount();
            int numRows = Math.max(1, (int) Math.ceil((double) totalItems / MORE_OPTION_COLUMNS));
            hgv.setNumRows(numRows);
            if (numRows > 1) mMultiRowHolders.add(vh); else mMultiRowHolders.remove(vh);
        } else {
            hgv.setNumRows(1);
            mMultiRowHolders.remove(vh);
        }

        // ---- "Continue Watching" remove action ----
        final View leftIcon = vh.view.findViewById(R.id.row_left);
        final View btnRemove = vh.view.findViewById(R.id.btn_remove_row);
        final boolean allowRemoveAction = DESC_CONTINUE_WATCHING.equalsIgnoreCase(description);

        if (leftIcon != null) {
            leftIcon.animate().cancel();
            leftIcon.setAlpha(1f);
            leftIcon.setVisibility(allowRemoveAction ? View.VISIBLE : View.GONE);
        }

        if (allowRemoveAction && btnRemove != null) {
            btnRemove.animate().cancel();
            btnRemove.setVisibility(View.GONE);
            btnRemove.setFocusable(true);
            btnRemove.setFocusableInTouchMode(true);
            btnRemove.setClickable(true);

            btnRemove.setNextFocusRightId(hgv.getId());
            hgv.setNextFocusLeftId(btnRemove.getId());

            // LEFT on first item -> reveal remove button and shift the grid right.
            hgv.setOnKeyInterceptListener(event -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                        && hgv.getSelectedPosition() == 0
                        && btnRemove.getVisibility() != View.VISIBLE) {

                    if (leftIcon != null) {
                        leftIcon.animate().cancel();
                        leftIcon.animate()
                                .alpha(0f)
                                .setDuration(100)
                                .withEndAction(() -> leftIcon.setVisibility(View.GONE))
                                .start();
                    }

                    btnRemove.setVisibility(View.VISIBLE);
                    btnRemove.setAlpha(0f);
                    btnRemove.setScaleX(0.8f);
                    btnRemove.setScaleY(0.8f);
                    btnRemove.animate().cancel();
                    btnRemove.animate()
                            .alpha(1f)
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .setInterpolator(mHotstarInterpolator)
                            .start();

                    final float shiftDistance =
                            48f * vh.view.getContext().getResources().getDisplayMetrics().density;
                    hgv.animate().cancel();
                    hgv.animate()
                            .translationX(shiftDistance)
                            .setDuration(0)
                            .setInterpolator(mHotstarInterpolator)
                            .withEndAction(btnRemove::requestFocus)
                            .start();

                    return true;
                }
                return false;
            });

            btnRemove.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    v.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150).start();
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                    v.post(() -> {
                        if (!vh.view.isAttachedToWindow() || btnRemove.hasFocus() || hgv.hasFocus()) {
                            return;
                        }
                        // Focus went to another row: put this row back to normal.
                        resetRemoveButtonAndRestoreGrid(vh, btnRemove, leftIcon, false);
                    });
                }
            });

            btnRemove.setOnClickListener(v -> triggerRemoveAction(vh, btnRemove, listRow));

            btnRemove.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    resetRemoveButtonAndRestoreGrid(vh, btnRemove, leftIcon, true);
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    triggerRemoveAction(vh, btnRemove, listRow);
                    return true;
                }

                return false;
            });

        } else {
            if (btnRemove != null) {
                btnRemove.animate().cancel();
                btnRemove.setVisibility(View.GONE);
                btnRemove.setFocusable(false);
                btnRemove.setClickable(false);
                btnRemove.setOnFocusChangeListener(null);
                btnRemove.setOnClickListener(null);
                btnRemove.setOnKeyListener(null);
            }

            hgv.animate().cancel();
            hgv.setTranslationX(0f);
            hgv.setOnKeyInterceptListener(null);
            hgv.setNextFocusLeftId(View.NO_ID);
        }
    }

    private void triggerRemoveAction(ViewHolder vh, View btnRemove, ListRow listRow) {
        btnRemove.animate().cancel();
        vh.getGridView().animate().cancel();

        if (mActionListener != null) {
            int position = (mRowsAdapter != null) ? mRowsAdapter.indexOf(listRow) : -1;
            mActionListener.onRemoveRow(position, listRow);
        }
    }

    /** Hides the remove button, shows the left icon again and slides the grid back. */
    private void resetRemoveButtonAndRestoreGrid(ViewHolder vh, View btnRemove, View leftIcon,
                                                 boolean requestGridFocus) {
        final HorizontalGridView hgv = vh.getGridView();

        if (btnRemove.getVisibility() == View.VISIBLE) {
            btnRemove.animate().cancel();
            btnRemove.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(180)
                    .setInterpolator(mHotstarInterpolator)
                    .withEndAction(() -> btnRemove.setVisibility(View.GONE))
                    .start();
        }

        if (leftIcon != null) {
            leftIcon.setVisibility(View.VISIBLE);
            leftIcon.animate().cancel();
            leftIcon.animate()
                    .alpha(1f)
                    .setDuration(100)
                    .start();
        }

        hgv.animate().cancel();
        hgv.animate()
                .translationX(0f)
                .setDuration(mDuration)
                .setInterpolator(mHotstarInterpolator)
                .withEndAction(() -> {
                    hgv.requestFocus();
                })
                .start();
    }

    // ---------------------------------------------------------------------------------
    // Selection: row alpha + z, and same-column restore on up/down
    // ---------------------------------------------------------------------------------

    @Override
    protected void onRowViewSelected(RowPresenter.ViewHolder vh, boolean selected) {
        super.onRowViewSelected(vh, selected);

        vh.view.animate()
                .alpha(selected ? ROW_ALPHA_SELECTED : ROW_ALPHA_DIMMED)
                .z(selected ? Z_FOCUSED_ROW : Z_DEFAULT)
                .setDuration(mDuration)
                .withLayer()
                .start();

        // Posted so it never runs in the middle of Leanback's vertical scroll / row fade.
        if (selected && !mMultiRowHolders.contains(vh)) {
            final HorizontalGridView hgv = ((ViewHolder) vh).getGridView();
            hgv.post(() -> {
                RecyclerView.Adapter<?> adapter = hgv.getAdapter();
                int count = adapter != null ? adapter.getItemCount() : 0;
                if (count == 0) return;
                int target = Math.min(mLastFocusedPosition, count - 1);
                if (hgv.getSelectedPosition() != target) {
                    hgv.setSelectedPosition(target);
                }
            });
        }
    }

    // ---------------------------------------------------------------------------------
    // Border helpers
    // ---------------------------------------------------------------------------------

    private static boolean isInside(View parent, View child) {
        View v = child;
        while (v != null) {
            if (v == parent) return true;
            v = (v.getParent() instanceof View) ? (View) v.getParent() : null;
        }
        return false;
    }

    /**
     * @param settled true when scrolling has finished, so the item's real position can be used;
     *                false while a scroll may still be pending (use aligned X).
     */
    private void updateBorder(final View border, final HorizontalGridView hgv,
                              final View target, final boolean animate, final boolean settled) {
        if (border == null || target == null) {
            return;
        }

        target.post(() -> {
            // Not laid out yet: retry once on the next frame.
            if (target.getWidth() == 0 || target.getHeight() == 0) {
                target.post(() -> applyBorder(border, hgv, target, animate, settled));
                return;
            }
            applyBorder(border, hgv, target, animate, settled);
        });
    }

    private void applyBorder(View border, HorizontalGridView hgv, View target,
                             boolean animate, boolean settled) {
        if (!target.isAttachedToWindow() || target.getWidth() == 0) {
            return;
        }

        syncBorderToItem(border, hgv, target, settled);

        if (animate) {
            animateBorder(border, 1f, Z_BORDER, mDecelerate);
        } else {
            border.animate().cancel();
            border.setAlpha(1f);
            border.setZ(Z_BORDER);
        }
    }

    private void syncBorderToItem(View border, HorizontalGridView hgv, View targetItem, boolean settled) {
        ViewGroup.LayoutParams params = border.getLayoutParams();
        if (params.width != targetItem.getWidth() || params.height != targetItem.getHeight()) {
            params.width = targetItem.getWidth();
            params.height = targetItem.getHeight();
            border.setLayoutParams(params);
        }

        border.setBackgroundResource(R.drawable.ic_action_focus_border);

        if (!(border.getParent() instanceof View)) return;
        View borderParent = (View) border.getParent();

        // Use window coordinates so the border's own layout left/top (margins, other views
        // in the row layout) never shifts it. translationX/Y are relative to that layout position.
        int[] parentLoc = new int[2];
        int[] itemLoc = new int[2];
        int[] gridLoc = new int[2];
        borderParent.getLocationInWindow(parentLoc);
        targetItem.getLocationInWindow(itemLoc);
        hgv.getLocationInWindow(gridLoc);

        float targetY = itemLoc[1] - parentLoc[1] - border.getTop();

        // While a scroll may be pending the item will end up at the aligned start offset
        // (measured from the grid's left edge, which already includes any translationX shift);
        // once settled, use the item's real position.
        float targetX = settled
                ? itemLoc[0] - parentLoc[0] - border.getLeft()
                : (gridLoc[0] - parentLoc[0] - border.getLeft()) + mPaddingStart;

        border.setTranslationX(targetX);
        border.setTranslationY(targetY);
    }

    private void animateBorder(View border, float alpha, float z, Interpolator interpolator) {
        if (border == null) return;
        border.animate().cancel();
        border.animate()
                .alpha(alpha)
                .z(z)
                .setDuration(mDuration)
                .setInterpolator(interpolator)
                .withLayer()
                .start();
    }

    @Override
    public boolean isUsingDefaultListSelectEffect() {
        return false;
    }

    @Override
    public boolean isUsingDefaultShadow() {
        return false;
    }
}