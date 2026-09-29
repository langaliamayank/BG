package com.androidtv.bhagavadgita.presenter;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ListRow;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderedRowManager {
    private final ArrayObjectAdapter mRowsAdapter;
    private final AtomicInteger mPendingCount = new AtomicInteger(0);
    private final Map<Integer, ListRow> mRowSlots = new ConcurrentHashMap<>();
    private final Runnable mOnAllLoadedCallback;
    private int mTotalExpected = 0;

    public OrderedRowManager(ArrayObjectAdapter adapter, Runnable onAllLoaded) {
        this.mRowsAdapter = adapter;
        this.mOnAllLoadedCallback = onAllLoaded;
    }

    public void registerRequests(int count) {
        mTotalExpected = count;
        mPendingCount.set(count);
    }

    /**
     * Put a row at its intended visual position (0, 1, 2, ...),
     * regardless of which network call finishes first.
     */
    public void submitRow(int targetPosition, ListRow row) {
        if (row != null) {
            mRowSlots.put(targetPosition, row);
        }
        checkCompletion();
    }

    public void notifyFailed() {
        checkCompletion();
    }

    private void checkCompletion() {
        if (mPendingCount.decrementAndGet() <= 0) {
            // Commit all loaded rows in strict slot order
            for (int i = 0; i < mTotalExpected; i++) {
                ListRow row = mRowSlots.get(i);
                if (row != null) {
                    mRowsAdapter.add(row);
                }
            }
            if (mOnAllLoadedCallback != null) {
                mOnAllLoadedCallback.run();
            }
        }
    }
}