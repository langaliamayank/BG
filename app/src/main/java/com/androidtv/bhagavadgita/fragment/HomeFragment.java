package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.TitleViewAdapter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.CommanActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.HeaderView;
import com.androidtv.bhagavadgita.comman.MyApplication;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ActionModel;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.model.VallabhacharyaModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.androidtv.bhagavadgita.presenter.DarshanTodayPresenter;
import com.androidtv.bhagavadgita.presenter.GitaPresenter;
import com.androidtv.bhagavadgita.presenter.MorePresenter;
import com.androidtv.bhagavadgita.presenter.VallabhacharyaPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class HomeFragment extends MasterBrowseFragment {

    private ArrayObjectAdapter mRowsAdapter;
    private OnBrowseRowListener mCallback;
    private HeaderView headerView;

    private CustomListRowPresenter selector;

    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private Boolean lastHeaderShowState = null;

    private int mLastSelectedRowIndex = 0;
    private boolean mLastShouldShowDetailHeader = false;

    private static final long ROW_FADE_OUT_MS = 160L;
    private static final long ROW_FADE_IN_MS = 180L;

    private DarshanTodayPresenter darshanTodayPresenter;

    // --- Darshan status refresh state ---
    private List<DarshanModel> mDarshanScheduleList;      // full day schedule, kept for recompute
    private ArrayObjectAdapter mDarshanTodayAdapter;       // "TODAY'S DARSHAN" row adapter (single item)

    private static final long DARSHAN_TICK_INTERVAL_SECONDS = 5L;

    // --- API Calls ---
    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> checkSubscription = null;
    private ScheduledFuture<?> darshanStatusFuture = null;

    /*For Background*/
    private BackgroundManager backgroundManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable backgroundRunnable;
    private static final int BACKGROUND_UPDATE_DELAY_MS = 300;

    public interface OnBrowseRowListener {
        void onItemSelected(Object item, long index);
    }

    @Override
    public View onInflateTitleView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
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

        backgroundManager = BackgroundManager.getInstance(requireActivity());

        updateBackgroundColorDelayed(SharePreferenceManager.getString("KEY_THEME_COLOR"));

        view.post(() -> {
            if (getRowsSupportFragment() != null) {
                VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
                if (vgv != null) {
                    vgv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                        @Override
                        public void onChildViewAttachedToWindow(@NonNull View child) {
                            int adapterPos = vgv.getChildAdapterPosition(child);
                            if (adapterPos == RecyclerView.NO_POSITION) return;
                            boolean hide = mLastShouldShowDetailHeader && adapterPos < mLastSelectedRowIndex;
                            child.setAlpha(hide ? 0f : 1f);
                        }

                        @Override
                        public void onChildViewDetachedFromWindow(@NonNull View child) {
                            // no-op
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupUIElements();

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }

        setupRowAdapter();
        setupEventListeners();
    }

    private void updateHeaderVisibility(boolean show) {
        if (lastHeaderShowState != null && lastHeaderShowState == show) return;
        lastHeaderShowState = show;

        Activity activity = getActivity();
        View contentImage = null;
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            contentImage = activity.findViewById(R.id.content_image);
        }

        if (headerView != null) {
            headerView.animate().cancel();
            if (show) {
                headerView.setVisibility(View.VISIBLE);
                headerView.setAlpha(0f);

                if (headerView.content_details != null) {
                    headerView.content_details.setVisibility(View.VISIBLE);
                }

                headerView.animate()
                        .alpha(1f)
                        .setStartDelay(ROW_FADE_OUT_MS)
                        .setDuration(220)
                        .setInterpolator(mInterpolator)
                        .start();
            } else {
                if (headerView.content_details != null) {
                    headerView.content_details.setVisibility(View.GONE);
                }

                headerView.animate()
                        .alpha(0f)
                        .setDuration(180)
                        .setInterpolator(mInterpolator)
                        .withEndAction(() -> {
                            if (lastHeaderShowState != null && !lastHeaderShowState && headerView != null) {
                                headerView.setVisibility(View.GONE);
                            }
                        })
                        .start();
            }
        }

        if (contentImage != null) {
            contentImage.animate().cancel();
            if (show) {
                contentImage.setVisibility(View.VISIBLE);
                contentImage.setAlpha(0f);
                contentImage.setScaleX(0.95f);
                contentImage.setScaleY(0.95f);
                contentImage.animate()
                        .alpha(1f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setStartDelay(ROW_FADE_OUT_MS)
                        .setDuration(250)
                        .setInterpolator(mInterpolator)
                        .start();
            } else {
                contentImage.animate()
                        .alpha(0f)
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(180)
                        .setInterpolator(mInterpolator)
                        .start();
            }
        }
    }

    private void fadeRowsForSelection(VerticalGridView vgv, int activeIndex, boolean shouldShowDetailHeader) {
        int totalChildCount = vgv.getChildCount();
        for (int i = 0; i < totalChildCount; i++) {
            View child = vgv.getChildAt(i);
            int adapterPos = vgv.getChildAdapterPosition(child);

            if (adapterPos != RecyclerView.NO_POSITION) {
                child.animate().cancel();
                child.setVisibility(View.VISIBLE);

                if (shouldShowDetailHeader && adapterPos < activeIndex) {
                    child.animate().alpha(0f).setDuration(ROW_FADE_OUT_MS).start();
                } else {
                    child.animate().alpha(1f).setDuration(ROW_FADE_IN_MS).start();
                }
            }
        }
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (mCallback != null && row != null) {
                mCallback.onItemSelected(item, row.getHeaderItem().getId());
            }

            if (row instanceof ListRow) {
                ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row).getAdapter());
                int selectedItemPosition = adapter.indexOf(item);
                if (itemViewHolder != null) itemViewHolder.view.setTag(selectedItemPosition);
            }

            if (item instanceof DarshanModel) { // Replace 'Movie' with your data model
                DarshanModel darshanModel = (DarshanModel) item;
                updateBackgroundColorDelayed("#" + darshanModel.getBackground());
                SharePreferenceManager.save("KEY_THEME_COLOR", "#" + darshanModel.getBackground());
            }

            int currentRowIndex = mRowsAdapter.indexOf(row);
            HeaderItem header = row.getHeaderItem();
            String desc = header != null && header.getDescription() != null
                    ? header.getDescription().toString().trim()
                    : "";

            boolean isTodayDarshan = "TODAY'S DARSHAN".equalsIgnoreCase(desc);
            boolean isMoreOption = "MORE OPTION".equalsIgnoreCase(desc);
            boolean isGita = "GITA".equalsIgnoreCase(desc);
            boolean shouldShowDetailHeader = !isTodayDarshan && !isMoreOption && !isGita;

            mLastSelectedRowIndex = currentRowIndex;
            mLastShouldShowDetailHeader = shouldShowDetailHeader;

            if (getRowsSupportFragment() != null) {
                VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
                if (vgv != null) {
                    int targetOffset = shouldShowDetailHeader
                            ? getResources().getDimensionPixelSize(R.dimen.content_image_height)
                            : getResources().getDimensionPixelSize(R.dimen.low_padding);
                    vgv.setWindowAlignmentOffset(targetOffset);

                    fadeRowsForSelection(vgv, currentRowIndex, shouldShowDetailHeader);
                }
            }

            if (headerView != null && header instanceof RowHeaderItem) {
                List<Object> childList = ((RowHeaderItem) header).getList();
                headerView.setData(item, childList, ((RowHeaderItem) header));
            }

            updateHeaderVisibility(shouldShowDetailHeader);
        });
    }

    private void setupRowAdapter() {
        selector = new CustomListRowPresenter(requireActivity());
        mRowsAdapter = new ArrayObjectAdapter(selector);
        selector.setRowsAdapter(mRowsAdapter);

        setAdapter(mRowsAdapter);

        createNextD();
    }

    private void createNextD() {

        MyApplication.getInstance().createNextDarshan(new MyApplication.DarshanListCallback() {
            @Override
            public void onLoaded(List<DarshanModel> darshanList) {
                mDarshanScheduleList = darshanList; // keep for periodic status recompute

                darshanTodayPresenter = new DarshanTodayPresenter((MasterActivity) getActivity(), darshanList);
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(darshanTodayPresenter);

                DarshanModel darshanModel = getCurrentOrNextDarshan(darshanList);
                if (darshanModel != null) {
                    listRowAdapter.add(darshanModel);
                }

                mDarshanTodayAdapter = listRowAdapter; // <-- ADD THIS

                RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "SHRINATHJI", Collections.singletonList(darshanList));
                cardPresenterHeader.setDescription("TODAY'S DARSHAN");

                mRowsAdapter.add(new ListRow(cardPresenterHeader, listRowAdapter));

                startDarshanStatusTicker();

                createVallabhacharya();

                if (getView() != null) {
                    getView().post(() -> startEntranceTransition());
                }
            }

            @Override
            public void onFailed(String error) {

            }
        });
    }

    private void startDarshanStatusTicker() {
        stopDarshanStatusTicker(); // avoid double-scheduling if called more than once

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        darshanStatusFuture = scheduler.scheduleWithFixedDelay(() -> {
            if (getActivity() == null || !isAdded()) return;
            requireActivity().runOnUiThread(this::refreshDarshanStatus);
        }, 0, DARSHAN_TICK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopDarshanStatusTicker() {
        if (darshanStatusFuture != null) {
            darshanStatusFuture.cancel(true);
            darshanStatusFuture = null;
        }
    }

    /**
     * Recomputes open/closed status for every Darshan entry and repaints the
     * two rows that depend on it: "DARSHAN SCHEDULE" (mDarshanScheduleAdapter)
     * and "TODAY'S DARSHAN" (mDarshanTodayAdapter).
     */
    private void refreshDarshanStatus() {
        if (!isAdded() || mDarshanScheduleList == null || mDarshanScheduleList.isEmpty()) return;

        LocalTime now = LocalTime.now();

        // 1. Update status on every schedule entry, then repaint that row.
        for (DarshanModel darshanModel : mDarshanScheduleList) {
            darshanModel.updateStatus(now);
        }

        // 2. Recompute which darshan is "current/next" for the top row.
        if (mDarshanTodayAdapter != null) {
            DarshanModel updated = getCurrentOrNextDarshan(mDarshanScheduleList);
            if (mDarshanTodayAdapter.size() > 0) {
                DarshanModel existing = (DarshanModel) mDarshanTodayAdapter.get(0);
                if (updated != null && updated != existing) {
                    mDarshanTodayAdapter.replace(0, updated);
                } else {
                    mDarshanTodayAdapter.notifyArrayItemRangeChanged(0, 1);
                }
            } else if (updated != null) {
                mDarshanTodayAdapter.add(updated);
            }
        }
    }

    public static DarshanModel getCurrentOrNextDarshan(List<DarshanModel> darshanList) {
        if (darshanList == null || darshanList.isEmpty()) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mma", Locale.ENGLISH);
        Date now = new Date();
        Calendar today = Calendar.getInstance();

        DarshanModel nextDarshan = null;
        long minDiff = Long.MAX_VALUE;

        for (DarshanModel d : darshanList) {
            if (d == null) continue;

            String rawStart = d.getStartTime();
            String rawEnd = d.getEndTime();

            // 1. Guard against null, empty, or blank strings
            if (rawStart == null || rawStart.trim().isEmpty() ||
                    rawEnd == null || rawEnd.trim().isEmpty()) {
                continue;
            }

            try {
                Date startTime = sdf.parse(rawStart.toUpperCase());
                Date endTime = sdf.parse(rawEnd.toUpperCase());

                if (startTime == null || endTime == null) continue;

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startTime);
                startCal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(endTime);
                endCal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                // If end time is past midnight (e.g. start 11:00 PM, end 01:00 AM)
                if (endCal.before(startCal)) {
                    endCal.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (!now.before(startCal.getTime()) && !now.after(endCal.getTime())) {
                    return d;
                }

                long diff = startCal.getTimeInMillis() - now.getTime();
                if (diff > 0 && diff < minDiff) {
                    minDiff = diff;
                    nextDarshan = d;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return nextDarshan != null ? nextDarshan : darshanList.get(0);
    }

    private void createMenu() {
        MorePresenter cardPresenter = new MorePresenter((MasterActivity) getActivity());
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

        ActionModel action1 = new ActionModel(0, "Pushtimarg", false, R.drawable.ic_action_logo);
        ActionModel action2 = new ActionModel(1, "Manorath Seva", false, R.drawable.ic_action_manorath);
        ActionModel action3 = new ActionModel(2, "History", false, R.drawable.ic_action_four); // ic_action_four
        ActionModel action4 = new ActionModel(3, "Shriji Nyochhavar Seva", false, R.drawable.ic_action_nyochavar);
        ActionModel action5 = new ActionModel(4, "Shrinathji Kirtan", false, R.drawable.ic_action_kirtan);
        ActionModel action6 = new ActionModel(5, "Gaumataji Seva", false, R.drawable.ic_action_gaumata);
        ActionModel action7 = new ActionModel(6, "Calendar", false, R.drawable.ic_action_six); //
        ActionModel action8 = new ActionModel(7, "Samagri Seva", false, R.drawable.ic_action_samagri);

        ArrayList<ActionModel> mWidgetActionsList = new ArrayList<>();
        mWidgetActionsList.add(action1);
        mWidgetActionsList.add(action2);
        mWidgetActionsList.add(action3);
        mWidgetActionsList.add(action4);
        mWidgetActionsList.add(action5);
        mWidgetActionsList.add(action6);
        mWidgetActionsList.add(action7);
        mWidgetActionsList.add(action8);

        listRowAdapter.addAll(0, mWidgetActionsList);

        RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "SHRINATHJI", null);
        cardPresenterHeader.setDescription("MORE OPTION");

        mRowsAdapter.add(new ListRow(cardPresenterHeader, listRowAdapter));

        createGita();

    }

    private void createGita() {
        GitaPresenter cardPresenter = new GitaPresenter((MasterActivity) getActivity());
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        listRowAdapter.add(new ActionModel(8, "BHAGAVAD GITA", false, 0));

        RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "BHAGAVAD", null);
        cardPresenterHeader.setDescription("GITA");

        mRowsAdapter.add(new ListRow(cardPresenterHeader, listRowAdapter));
    }

    private void createVallabhacharya() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getVallabhacharya();
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                VallabhacharyaPresenter vallabhacharyaPresenter = new VallabhacharyaPresenter((MasterActivity) getActivity());
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(vallabhacharyaPresenter);

                try {
                    List<VallabhacharyaModel> vallabhacharyaModelList = new Gson().fromJson(response, new TypeToken<List<VallabhacharyaModel>>() {
                    }.getType());
                    for (VallabhacharyaModel vallabhacharyaModel : vallabhacharyaModelList) {
                        listRowAdapter.add(vallabhacharyaModel);
                    }

                    RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "SHRINATHJI", Collections.singletonList(vallabhacharyaModelList));
                    cardPresenterHeader.setDescription("VALLABHACHARYA");
                    mRowsAdapter.add(new ListRow(cardPresenterHeader, listRowAdapter));

                    createMenu();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private void setupUIElements() {
        if (getActivity() instanceof OnBrowseRowListener) {
            mCallback = (OnBrowseRowListener) getActivity();
        } else {
            throw new ClassCastException(getActivity().toString() + " must implement OnBrowseRowListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (backgroundRunnable != null) {
            handler.removeCallbacks(backgroundRunnable);
        }

        try {
            if (checkSubscription != null) {
                checkSubscription.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopDarshanStatusTicker();

        try {
            if (scheduler != null) {
                scheduler.shutdown();
                scheduler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBackgroundColorDelayed(final String hexColor) {
        if (backgroundRunnable != null) {
            handler.removeCallbacks(backgroundRunnable);
        }

        backgroundRunnable = () -> {
            if (getActivity() == null || hexColor == null || hexColor.isEmpty()) return;

            try {
                int color = Color.parseColor(hexColor);

                // 1. setColor handles solid colors reliably in Leanback
                if (backgroundManager != null && backgroundManager.isAttached()) {
                    backgroundManager.setColor(color);
                }

                // 2. Set directly on the window to prevent Leanback from blanking it out
                getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(color));

            } catch (IllegalArgumentException ignored) {
            }
        };

        handler.postDelayed(backgroundRunnable, BACKGROUND_UPDATE_DELAY_MS);
    }


    @Override
    public void onDestroyView() {
        // views are gone, so cached state about them is invalid
        lastHeaderShowState = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        reapplyRowLayout();
    }

    private void reapplyRowLayout() {
        View root = getView();
        if (root == null) return;

        Runnable apply = () -> {
            if (!isAdded() || getRowsSupportFragment() == null) return;
            VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
            if (vgv == null) return;

            int targetOffset = mLastShouldShowDetailHeader
                    ? getResources().getDimensionPixelSize(R.dimen.content_image_height)
                    : getResources().getDimensionPixelSize(R.dimen.low_padding);

            vgv.setWindowAlignmentOffset(targetOffset);
            fadeRowsForSelection(vgv, mLastSelectedRowIndex, mLastShouldShowDetailHeader);
            updateHeaderVisibility(mLastShouldShowDetailHeader);
        };

        // Leanback applies its own alignment after the view is created,
        // so run after that, then once more as a safety net.
        root.post(apply);
        root.postDelayed(apply, 150);
    }

}