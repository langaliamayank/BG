package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.TitleViewAdapter;
import androidx.leanback.widget.VerticalGridView;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.MySettingsActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.MyApplication;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ActionModel;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.androidtv.bhagavadgita.presenter.DarshanTodayPresenter;
import com.androidtv.bhagavadgita.presenter.GitaPresenter;
import com.androidtv.bhagavadgita.presenter.MorePresenter;
import com.androidtv.bhagavadgita.CommanActivity;

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

public class DashboardFragment extends MasterBrowseFragment {

    private ArrayObjectAdapter mRowsAdapter;
    private DarshanTodayPresenter darshanTodayPresenter;

    private List<DarshanModel> mDarshanScheduleList;
    private ArrayObjectAdapter mDarshanTodayAdapter;

    private static final long DARSHAN_TICK_INTERVAL_SECONDS = 5L;

    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> checkSubscription = null;
    private ScheduledFuture<?> darshanStatusFuture = null;

    private BackgroundManager backgroundManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable backgroundRunnable;
    private static final int BACKGROUND_UPDATE_DELAY_MS = 300;

    public interface OnBrowseRowListener {
        void onItemSelected(Object item, long index);
    }

    @Nullable
    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        return new TitleViewAdapter() {
            @Nullable
            @Override
            public View getSearchAffordanceView() {
                return null;
            }

            @Override
            public void updateComponentsVisibility(int flags) {}
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupRowAdapter();
        setupEventListeners();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Step 1: Initialize BackgroundManager
        backgroundManager = BackgroundManager.getInstance(requireActivity());

        updateBackgroundColorDelayed(SharePreferenceManager.getString("KEY_THEME_COLOR"));
    }

    private void setupRowAdapter() {
        CustomListRowPresenter selector = new CustomListRowPresenter((MasterActivity) requireActivity());
        mRowsAdapter = new ArrayObjectAdapter(selector);

        setAdapter(mRowsAdapter);

        createNextD();
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

        RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Shrinathji", null);
        cardPresenterHeader.setDescription("More Option");

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

    private void setupEventListeners() {
        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (row instanceof ListRow) {
                ArrayObjectAdapter adapter = (ArrayObjectAdapter) ((ListRow) row).getAdapter();
                int selectedItemPosition = adapter.indexOf(item);

                if (itemViewHolder != null) {
                    itemViewHolder.view.setTag(selectedItemPosition);
                }
            }

            if (item instanceof DarshanModel) { // Replace 'Movie' with your data model
                DarshanModel darshanModel = (DarshanModel) item;
                updateBackgroundColorDelayed("#" + darshanModel.getBackground());
                SharePreferenceManager.save("KEY_THEME_COLOR", "#" + darshanModel.getBackground());
            }
        });

        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (item instanceof ActionModel) {

                ActionModel action = (ActionModel) item;
                long id = action.getId();

                switch ((int) id) {
                    case 0:
                    case 2:
                    case 4:
                    case 8:
                        startActivity(CommanActivity.createIntent(requireActivity(), action));
                        return;

                    case 6:
                        startActivity(new Intent(requireActivity(), CalendarActivity.class));
                        return;

                    default:
                        startActivity(new Intent(requireActivity(), MySettingsActivity.class));
                        break;
                }
            }
        });
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

                createMenu();

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

    private void refreshDarshanStatus() {
        if (!isAdded() || mDarshanScheduleList == null || mDarshanScheduleList.isEmpty()) return;

        LocalTime now = LocalTime.now();

        for (DarshanModel darshanModel : mDarshanScheduleList) {
            darshanModel.updateStatus(now);
        }

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
}