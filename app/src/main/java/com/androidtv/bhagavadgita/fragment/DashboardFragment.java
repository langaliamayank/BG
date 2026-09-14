package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.VerticalGridView;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.DarshanActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.MyApplication;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ActionModel;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.presenter.DarshanPresenter;
import com.androidtv.bhagavadgita.presenter.DarshanTodayPresenter;
import com.androidtv.bhagavadgita.presenter.MorePresenter;
import com.androidtv.bhagavadgita.presenter.MyListRowPresenter;
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

public class DashboardFragment extends BrowseSupportFragment {

    private ArrayObjectAdapter mRowsAdapter;
    private DarshanTodayPresenter darshanTodayPresenter;

    private static final String SPINNER_TAG = "LoadingOverlay";
    private SpinnerSupportFragment mSpinnerFragment;

    // --- Darshan status refresh state ---
    private List<DarshanModel> mDarshanScheduleList;      // full day schedule, kept for recompute
    private ArrayObjectAdapter mDarshanScheduleAdapter;    // "DARSHAN SCHEDULE" row adapter
    private ArrayObjectAdapter mDarshanTodayAdapter;       // "TODAY'S DARSHAN" row adapter (single item)

    private static final long DARSHAN_TICK_INTERVAL_SECONDS = 5L;


    // --- API Calls ---
    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> checkSubscription = null;
    private ScheduledFuture<?> darshanStatusFuture = null;

    //    /*For Background*/
    private BackgroundManager backgroundManager;
    private Drawable defaultBackground;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable backgroundRunnable;
    private static final int BACKGROUND_UPDATE_DELAY_MS = 300;

    public interface OnBrowseRowListener {
        void onItemSelected(Object item, long index);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLoader();
        setupUIElements();
        setupRowAdapter();
        setupEventListeners();
    }

    private void showLoader() {
        if (getParentFragmentManager().findFragmentByTag(SPINNER_TAG) != null) return;
        mSpinnerFragment = new SpinnerSupportFragment();
        getParentFragmentManager().beginTransaction()
                .add(android.R.id.content, mSpinnerFragment, SPINNER_TAG)
                .commitAllowingStateLoss();
    }

    private void hideLoader() {
        if (!isAdded()) return;
        Fragment fragment = getParentFragmentManager().findFragmentByTag(SPINNER_TAG);
        if (fragment != null) {
            getParentFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
            mSpinnerFragment = null;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Step 1: Initialize BackgroundManager
        backgroundManager = BackgroundManager.getInstance(requireActivity());

        updateBackgroundColorDelayed(SharePreferenceManager.getString("KEY_THEME_COLOR"));

        prepareEntranceTransition();

        view.post(() -> {
            if (!isAdded()) return;
            if (getRowsSupportFragment() != null) {
                VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
                if (vgv != null) {
                    vgv.setItemAnimator(null);
                    vgv.setClipChildren(false);
                    vgv.setClipToPadding(false);
                    vgv.setFocusScrollStrategy(BaseGridView.FOCUS_SCROLL_ALIGNED);
                    vgv.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_NO_EDGE);
                    vgv.setWindowAlignmentOffsetPercent(0f);
                    disableClipping(vgv);
                }
            }
        });
    }

    public void disableClipping(View view) {
        if (view == null) return;
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
        }
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            disableClipping((View) parent);
        }
    }

    private void setupUIElements() {
        setHeadersState(HEADERS_DISABLED);
        setHeadersTransitionOnBackEnabled(false);
    }

    private void setupRowAdapter() {
        MyListRowPresenter selector = new MyListRowPresenter((MasterActivity) requireActivity(), 0);
        mRowsAdapter = new ArrayObjectAdapter(selector);
        setAdapter(mRowsAdapter);

        createNextD();
    }

    private void createMenu() {
        MorePresenter cardPresenter = new MorePresenter((MasterActivity) getActivity());
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

        ActionModel action1 = new ActionModel(0, "Ashta-yama Darshan", false, R.drawable.ic_action_krishn1);
        ActionModel action2 = new ActionModel(1, "Shrinathji Kirtan", false, R.drawable.ic_action_kirtan);
        ActionModel action3 = new ActionModel(2, "Manorath Seva", false, R.drawable.ic_action_manorath);
        ActionModel action4 = new ActionModel(3, "Pushtimarg", false, R.drawable.ic_action_logo);

        ActionModel action5 = new ActionModel(4, "Calendar", false, R.drawable.ic_action_calendar);
        ActionModel action6 = new ActionModel(5, "Shriji Nyochhavar Seva", false, R.drawable.ic_action_nyochavar);
        ActionModel action7 = new ActionModel(6, "Vallabhacharya", false, R.drawable.ic_action_vallabhacharya);
        ActionModel action8 = new ActionModel(7, "Darshan Booking", false, R.drawable.ic_action_darshan_book);

        ActionModel action9 = new ActionModel(8, "Gaumataji Seva", false, R.drawable.ic_action_gaumata);
        ActionModel action10 = new ActionModel(9, "History", false, R.drawable.ic_action_history);
        ActionModel action11 = new ActionModel(10, "Bhagavad Gita", false, R.drawable.ic_action_bg);
        ActionModel action12 = new ActionModel(11, "Samagri Seva Bhent", false, R.drawable.ic_action_samagri);

        ArrayList<ActionModel> mWidgetActionsList = new ArrayList<>();
        mWidgetActionsList.add(action1);
        mWidgetActionsList.add(action2);
        mWidgetActionsList.add(action3);
        mWidgetActionsList.add(action4);
        mWidgetActionsList.add(action5);
        mWidgetActionsList.add(action6);
        mWidgetActionsList.add(action7);
        mWidgetActionsList.add(action8);

        mWidgetActionsList.add(action9);
        mWidgetActionsList.add(action10);
        mWidgetActionsList.add(action11);
        mWidgetActionsList.add(action12);

        listRowAdapter.addAll(0, mWidgetActionsList);

        RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Shrinathji", null);
        cardPresenterHeader.setDescription("More Option");

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
            if (getActivity() == null || item == null) return;

            if (item instanceof ActionModel) {

                ActionModel action = (ActionModel) item;
                long id = action.getId();

                switch ((int) id) {
                    case 0:
                    case 1:
                    case 3:
                    case 6:
                    case 9:
                    case 10:
                        startActivity(CommanActivity.createIntent(requireActivity(), action));
                        return;

                    case 4:
                        startActivity(new Intent(requireActivity(), CalendarActivity.class));
                        return;

                    default:
                        Toast.makeText(requireActivity(), action.getTitle() + " Coming Soon", Toast.LENGTH_SHORT).show();
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
                hideLoader();

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
        if (mDarshanScheduleAdapter != null && mDarshanScheduleAdapter.size() > 0) {
            mDarshanScheduleAdapter.notifyArrayItemRangeChanged(0, mDarshanScheduleAdapter.size());
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
            try {
                Date startTime = sdf.parse(d.getStartTime().toUpperCase());
                Date endTime = sdf.parse(d.getEndTime().toUpperCase());

                if (startTime == null || endTime == null) continue;

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startTime);
                startCal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(endTime);
                endCal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

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