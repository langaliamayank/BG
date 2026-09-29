package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.TitleViewAdapter;

import com.androidtv.bhagavadgita.DarshanActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.HeaderView;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.MyApplication;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.androidtv.bhagavadgita.presenter.DarshanPresenter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public class DarshanFragment extends MasterBrowseFragment {

    private ArrayObjectAdapter mRowsAdapter;
    private OnBrowseRowListener mCallback;
    private HeaderView headerView;
    private int selectedIndex = -1;
    private int upcomingIndex = -1;
    private Handler mHandler = new Handler();
    private List<DarshanModel> mDarshanScheduleList;

    public interface OnBrowseRowListener {
        void onItemSelected(Object item, long index);
    }

    @Override
    public View onInflateTitleView(@org.jspecify.annotations.NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        headerView = new HeaderView(inflater.getContext(), false, getActivity().getWindowManager());
        return headerView;
    }

    private final TitleViewAdapter mFallbackTitleAdapter = new TitleViewAdapter() {
        @Nullable
        @Override
        public View getSearchAffordanceView() {
            return null;
        }

        @Override
        public void updateComponentsVisibility(int flags) {}
    };

    @Nullable
    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        if (headerView != null && headerView.getTitleViewAdapter() != null) {
            return headerView.getTitleViewAdapter();
        }
        return mFallbackTitleAdapter;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.post(() -> {
            if (selectedIndex != -1 && mRowsAdapter != null && mRowsAdapter.size() > 0) {
                selectRowAt(0, selectedIndex);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }

        setupCallback();
        setupRowAdapter();
        setupEventListeners();
    }

    private void setupCallback() {
        if (getActivity() instanceof OnBrowseRowListener) {
            mCallback = (OnBrowseRowListener) getActivity();
        } else {
            throw new ClassCastException(getActivity().toString() + " must implement OnBrowseRowListener");
        }
    }

    private void setupRowAdapter() {
        CustomListRowPresenter selector = new CustomListRowPresenter((MasterActivity) getActivity());
        mRowsAdapter = new ArrayObjectAdapter(selector);
        setAdapter(mRowsAdapter);

        new Thread(() -> {
            try {
                Thread.sleep(Constants.INTERVAL);

                createDarshan();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        mHandler.postDelayed(mUpdateClockTask, Constants.SHORT_INTERVAL);
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (mCallback != null && row != null) {
                mCallback.onItemSelected(item, row.getHeaderItem().getId());
            }

            if (row instanceof ListRow) {
                ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                        .getAdapter());
                int selectedItemPosition = adapter.indexOf(item);

                if (itemViewHolder != null) itemViewHolder.view.setTag(selectedItemPosition);
            }

            if (item instanceof Object && row != null) {
                Object data = (Object) item;

                // 1. Update the Data
                if (headerView != null) {
                    HeaderItem header = row.getHeaderItem();
                    if (header instanceof RowHeaderItem) {
                        List<Object> childList = ((RowHeaderItem) header).getList();
                        headerView.setData(data, childList, ((RowHeaderItem) header));

                        headerView.post(() -> {
                            updateHeaderVisibility(true);
                        });
                    }
                }
            }
        });

        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (!(item instanceof Object) || !(row instanceof ListRow)) return;

            if (item instanceof DarshanModel) {
                mHandler.removeCallbacks(mUpdateClockTask);

                DarshanModel darshanModel = (DarshanModel) item;
                startActivity(DarshanActivity.createIntent(getActivity(), darshanModel));
            }
        });
    }

    private void createDarshan() {
        MyApplication.getInstance().createNextDarshan(new MyApplication.DarshanListCallback() {
            @Override
            public void onLoaded(List<DarshanModel> darshanList) {
                mDarshanScheduleList = darshanList;
                DarshanPresenter darshanPresenter = new DarshanPresenter((MasterActivity) getActivity(), 0);
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(darshanPresenter);

                LocalTime now = LocalTime.now();
                int targetIndex = -1;
                int firstUpcomingIndex = -1;

                for (int i = 0; i < darshanList.size(); i++) {
                    DarshanModel darshanModel = darshanList.get(i);
                    darshanModel.updateStatus(now);
                    listRowAdapter.add(darshanModel);

                    // Determine current (Open) or fallback (Upcoming) index
                    String status = darshanModel.getStatus();
                    if (status != null) {
                        if (targetIndex == -1 && status.startsWith("Open")) {
                            targetIndex = i;
                        } else if (firstUpcomingIndex == -1 && status.startsWith("Upcoming")) {
                            firstUpcomingIndex = i;
                        }
                    }
                }

                if (targetIndex == -1) {
                    targetIndex = (firstUpcomingIndex != -1) ? firstUpcomingIndex : 0;
                }
                selectedIndex = targetIndex;

                RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Shrinathji", Collections.singletonList(darshanList));
                cardPresenterHeader.setDescription("Darshan");

                mRowsAdapter.add(new ListRow(cardPresenterHeader, listRowAdapter));

                if (getView() != null) {
                    getView().post(() -> {
                        if (isAdded()) {
                            startEntranceTransition();

                            // Post focus task once the adapter renders the row
                            if (getView() != null) {
                                getView().post(() -> selectRowAt(0, selectedIndex));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(String error) {
            }
        });
    }

    private void updateHeaderVisibility(boolean show) {
        if (headerView == null) return;

        float targetAlpha = show ? 1f : 0f;
        headerView.animate()
                .alpha(targetAlpha)
                .setDuration(100)
                .start();

        if (!isAdded()) return;

        Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        View contentImage = activity.findViewById(R.id.content_image);
        if (contentImage != null) {
            contentImage.animate()
                    .alpha(targetAlpha)
                    .setDuration(100)
                    .start();
        }

        ImageView mainActivityImageView = getActivity().findViewById(R.id.content_image);
        View rowsContainer = getView().findViewById(androidx.leanback.R.id.scale_frame);
        if (rowsContainer != null) {
            int padding_high = getResources().getDimensionPixelSize(R.dimen.content_image_height);
            int padding_low = getResources().getDimensionPixelSize(R.dimen.low_padding);
            rowsContainer.setPadding(0, show ? padding_high : padding_low, 0, 0);
            mainActivityImageView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void selectedIndex() {
        if (mDarshanScheduleList == null || mDarshanScheduleList.isEmpty()) {
            LogTag.e("DarshanFragment: darshanList is null or empty");
            return;
        }

        // Reset indices before recalculating
        selectedIndex = -1;
        upcomingIndex = -1;

        LocalTime now = LocalTime.now();
        for (int i = 0; i < mDarshanScheduleList.size(); i++) {
            DarshanModel model = mDarshanScheduleList.get(i);
            model.updateStatus(now);

            String status = model.getStatus();
            if (status != null) {
                if (selectedIndex == -1 && status.startsWith("Open")) {
                    selectedIndex = i;
                    break;
                } else if (upcomingIndex == -1 && status.startsWith("Upcoming")) {
                    upcomingIndex = i;
                }
            }
        }

        if (selectedIndex == -1) {
            selectedIndex = (upcomingIndex != -1) ? upcomingIndex : 0;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            LogTag.e("selectedIndex " + selectedIndex);
            selectRowAt(0, selectedIndex);
        });
    }

    private void selectRowAt(int rowIndex, int lastSelectedIndex) {
        RowsSupportFragment rowsFragment = getRowsSupportFragment();
        if (rowsFragment == null) {
            LogTag.e("selectRowAt " + "RowsSupportFragment not ready yet");
            return;
        }

        rowsFragment.setSelectedPosition(rowIndex, true,
                new CustomListRowPresenter.SelectItemViewHolderTask(lastSelectedIndex));
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateClockTask);
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mUpdateClockTask);
    }

    private Runnable mUpdateClockTask = new Runnable() {
        public void run() {
            selectedIndex();
            mHandler.postDelayed(this, 1 * 60 * 1000); // 1 minutes = 60000 ms = 1 * 60 * 1000
        }
    };
}