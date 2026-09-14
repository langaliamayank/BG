package com.androidtv.bhagavadgita.fragment;

import static com.androidtv.bhagavadgita.MasterActivity.loadJSONFromAsset;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.VerticalGridView;

import com.androidtv.bhagavadgita.DarshanActivity;
import com.androidtv.bhagavadgita.DetailActivity;
import com.androidtv.bhagavadgita.HomeDetailActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.HeaderView;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.MyApplication;
import com.androidtv.bhagavadgita.comman.NavigableFragment;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.comman.RowListener;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ActionModel;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.androidtv.bhagavadgita.model.HistoryModel;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.model.VallabhacharyaModel;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.androidtv.bhagavadgita.presenter.CardPresenter;
import com.androidtv.bhagavadgita.presenter.DarshanPresenter;
import com.androidtv.bhagavadgita.presenter.DarshanTodayPresenter;
import com.androidtv.bhagavadgita.presenter.FestivalPresenter;
import com.androidtv.bhagavadgita.presenter.HistoryPresenter;
import com.androidtv.bhagavadgita.presenter.MorePresenter;
import com.androidtv.bhagavadgita.presenter.MyListRowPresenter;
import com.androidtv.bhagavadgita.presenter.PushtimargPresenter;
import com.androidtv.bhagavadgita.presenter.VallabhacharyaPresenter;
import com.androidtv.bhagavadgita.presenter.VersesOfTheDayPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

//import org.jspecify.annotations.NonNull;
//import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HomeNewFragment extends BrowseSupportFragment implements NavigableFragment {

    private ArrayObjectAdapter mRowsAdapter;
    private OnBrowseRowListener mCallback;
    private View mFirstPosterView = null;

    private VersesOfTheDayPresenter versesOfTheDayPresenter;
    private HeaderView headerView;
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

    public interface OnBrowseRowListener {
        void onItemSelected(Object item, long index);
    }

    public void setFocusOnFirstPoster() {
        if (mFirstPosterView != null) {
            mFirstPosterView.requestFocus();
        }
    }

    public View getmFirstPosterView() {
        return mFirstPosterView;
    }

    @Override
    public boolean isAtTopRow() {
        return getRowsSupportFragment() != null &&
                getRowsSupportFragment().getSelectedPosition() == 0;
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

    @Override
    public @NonNull View onInflateTitleView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        headerView = new HeaderView(inflater.getContext(), false, requireActivity().getWindowManager());
        return headerView;
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

        int resId = view.getContext().getResources().getIdentifier("scale_frame", "id", view.getContext().getPackageName());
        View rowsContainer = view.findViewById(resId);
        if (rowsContainer != null) {
            int padding = getResources().getDimensionPixelSize(R.dimen.content_image_height);
            rowsContainer.setPadding(0, padding, 0, 0);
        }

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

    private void setupUIElements() {
        setHeadersState(HEADERS_DISABLED);
        setHeadersTransitionOnBackEnabled(false);
        if (getActivity() instanceof OnBrowseRowListener) {
            mCallback = (OnBrowseRowListener) getActivity();
        } else {
            throw new ClassCastException(requireActivity().toString() + " must implement OnBrowseRowListener");
        }
    }

    private void setupRowAdapter() {
        MyListRowPresenter selector = new MyListRowPresenter((MasterActivity) requireActivity(), 0);
        mRowsAdapter = new ArrayObjectAdapter(selector);
        setAdapter(mRowsAdapter);

        createNextD();
        createMenu(1);

//        createTippani(1);
//        createVallabhacharya(3);
//        createPushtimarg(4);
//        createBG(5);
//        createVerse(6);
//        createHistory(7);
    }

    private void createMenu(int index) {
//        hideLoader();
        MorePresenter cardPresenter = new MorePresenter((MasterActivity) getActivity());
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

        ActionModel action1 = new ActionModel(0, "Darshan Booking", true, R.drawable.ic_action_darshan_book);
        ActionModel action2 = new ActionModel(1, "Manorath Seva", true, R.drawable.ic_action_manorath);
        ActionModel action3 = new ActionModel(2, "Shrinathji Kirtan", false, R.drawable.ic_action_kirtan);
        ActionModel action4 = new ActionModel(3, "Shriji Nyochhavar Seva", false, R.drawable.ic_action_nyochavar);
        ActionModel action5 = new ActionModel(4, "Gaumataji Seva", false, R.drawable.ic_action_gaumata);
        ActionModel action6 = new ActionModel(5, "Samagri Seva Bhent", false, R.drawable.ic_action_samagri);
        ActionModel action7 = new ActionModel(6, "Calendar", false, R.drawable.ic_action_calendar);
        ActionModel action8 = new ActionModel(7, "Bhagavad Gita", false, R.drawable.ic_action_bg);
//        ActionModel action9 = new ActionModel(8, "Vallabhacharya", false, R.drawable.ic_action_vallabhacharya);

        ArrayList<ActionModel> mWidgetActionsList = new ArrayList<>();
        mWidgetActionsList.add(action1);
        mWidgetActionsList.add(action2);
        mWidgetActionsList.add(action3);
        mWidgetActionsList.add(action4);
        mWidgetActionsList.add(action5);
        mWidgetActionsList.add(action6);
        mWidgetActionsList.add(action7);
        mWidgetActionsList.add(action8);
//        mWidgetActionsList.add(action9);

        for (ActionModel actionModel : mWidgetActionsList) {
            listRowAdapter.add(actionModel);
        }

        RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Shrinathji", null);
        cardPresenterHeader.setDescription("More Option");

        mRowsAdapter.add(new ListRow(cardPresenterHeader, listRowAdapter));

//        checkAndCommitRows();
    }

    private void createTippani(int index) {
        Map<String, FestivalModel> activeFestivalMap = loadTippaniForYear(requireActivity(), 2083);

        FestivalPresenter festivalPresenter = new FestivalPresenter((MasterActivity) getActivity());
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(festivalPresenter);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (activeFestivalMap != null) {
            try {
                // Normalize today's date to 00:00:00 so it matches the parsed JSON date keys perfectly
                String todayStr = sdf.format(new Date());
                Date todayDate = sdf.parse(todayStr);

                List<Map.Entry<String, FestivalModel>> filteredList = new ArrayList<>();

                for (Map.Entry<String, FestivalModel> entry : activeFestivalMap.entrySet()) {
                    String dateKey = entry.getKey(); // e.g., "2026-08-28"
                    FestivalModel festivalModel = entry.getValue();

                    if (dateKey != null) {
                        Date festivalDate = sdf.parse(dateKey);

                        // Compare calendar days safely (includes today and future dates)
                        if (festivalDate != null && !festivalDate.before(todayDate)) {
                            filteredList.add(entry);
                            LogTag.e("TippaniDebug Added to Adapter: " + festivalModel.getTitle() + " (" + dateKey + ")");
                        }
                    }
                }

                // Sort filtered items chronologically by date
                Collections.sort(filteredList, new Comparator<Map.Entry<String, FestivalModel>>() {
                    @Override
                    public int compare(Map.Entry<String, FestivalModel> o1, Map.Entry<String, FestivalModel> o2) {
                        try {
                            Date d1 = sdf.parse(o1.getKey());
                            Date d2 = sdf.parse(o2.getKey());
                            if (d1 != null && d2 != null) {
                                return d1.compareTo(d2);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });

                // Add sorted items to the Leanback adapter
                for (Map.Entry<String, FestivalModel> sortedEntry : filteredList) {
                    listRowAdapter.add(sortedEntry.getValue());
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Shrinathji", null);
        cardPresenterHeader.setDescription("Upcoming Utsav Timeline (Vikram Samvat 2083)");

        mRowsAdapter.add(index, new ListRow(cardPresenterHeader, listRowAdapter));

//        checkAndCommitRows();
    }

    public static Map<String, FestivalModel> loadTippaniForYear(Context context, int vikramSamvatYear) {
        Map<String, FestivalModel> festivalMap = new HashMap<>();
        String fileName = "tippani_" + vikramSamvatYear + ".json"; // e.g., "tippani_2083.json"

        try {
            // Check if file exists in assets for the given year, fallback gracefully if needed
            InputStream is = context.getAssets().open(fileName);
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

            Type type = new TypeToken<Map<String, FestivalModel>>() {
            }.getType();
            Map<String, FestivalModel> rawMap = new Gson().fromJson(reader, type);

            if (rawMap != null) {
                for (Map.Entry<String, FestivalModel> entry : rawMap.entrySet()) {
                    String dateKey = entry.getKey(); // Format: "YYYY-MM-DD"
                    FestivalModel model = entry.getValue();
                    festivalMap.put(dateKey, new FestivalModel(model.getTitle(), model.getTithi(), model.getDescription(), dateKey));
                }
            }
            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error or load a default/placeholder message if the year file isn't found
        }
        return festivalMap;
    }
    private void updateHeaderVisibility(boolean show) {
        if (headerView == null || !isAdded()) return;

        float targetAlpha = show ? 1f : 0f;
        headerView.animate().alpha(targetAlpha).setDuration(100).start();

        View mainContentImage = requireActivity().findViewById(R.id.content_image);
        if (mainContentImage != null) {
            mainContentImage.animate().alpha(targetAlpha).setDuration(100).start();
            mainContentImage.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        View rowsContainer = requireView().findViewById(androidx.leanback.R.id.scale_frame);
        if (rowsContainer != null) {
            int paddingHigh = getResources().getDimensionPixelSize(R.dimen.content_image_height);
            int paddingLow = getResources().getDimensionPixelSize(R.dimen.low_padding);
            rowsContainer.setPadding(0, show ? paddingHigh : paddingLow, 0, 0);
        }
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (mCallback != null && row != null) {
                mCallback.onItemSelected(item, row.getHeaderItem().getId());
            }

            if (row instanceof ListRow) {
                ArrayObjectAdapter adapter = (ArrayObjectAdapter) ((ListRow) row).getAdapter();
                int selectedItemPosition = adapter.indexOf(item);

                if (itemViewHolder != null) {
                    itemViewHolder.view.setTag(selectedItemPosition);

                    if (selectedItemPosition == 0) {
                        mFirstPosterView = itemViewHolder.view;
                    }
                }
            }

            if (item != null && row != null && headerView != null) {
                HeaderItem header = row.getHeaderItem();
                if (header instanceof RowHeaderItem) {
                    List<Object> childList = ((RowHeaderItem) header).getList();
                    headerView.setData(item, childList, (RowHeaderItem) header);

                    headerView.post(() -> {
                        CharSequence desc = header.getDescription();
                        boolean shouldShow = desc != null && !desc.equals("TODAY'S DARSHAN");

                        LogTag.e("shouldShow " + shouldShow);
                        updateHeaderVisibility(shouldShow);
                    });
                }
            }

        });

        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (getActivity() == null || item == null) return;

            if (item instanceof HistoryModel) {
                startActivity(HomeDetailActivity.createIntent(getActivity(), (HistoryModel) item, 0));
            } else if (item instanceof PushtimargModel) {
                startActivity(HomeDetailActivity.createIntent(getActivity(), (PushtimargModel) item, 1));
            } else if (item instanceof VallabhacharyaModel) {
                startActivity(HomeDetailActivity.createIntent(getActivity(), (VallabhacharyaModel) item, 2));
            } else if (item instanceof VersesModel) {
                VersesModel versesModel = (VersesModel) item;
                if (!versesModel.isDummy() && versesOfTheDayPresenter != null) {
                    ArrayList<VersesModel> mVersesList = versesOfTheDayPresenter.getList();
                    int mIndex = mVersesList.indexOf(versesModel);
                    startActivity(DetailActivity.createIntent(getActivity(), versesModel, mIndex, true));
                }
            } else if (item instanceof DarshanModel) {
                startActivity(DarshanActivity.createIntent(getActivity(), (DarshanModel) item));
            }
        });
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
//                createDarshan(darshanList, 1);
                startDarshanStatusTicker();
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

    private void createDarshan(List<DarshanModel> newList, int index) {
        if (newList != null && !newList.isEmpty()) {
            hideLoader();
            DarshanPresenter cardPresenter = new DarshanPresenter((MasterActivity) getActivity(), 0);
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

            LocalTime now = LocalTime.now();
            for (DarshanModel darshanModel : newList) {
                darshanModel.updateStatus(now);
                listRowAdapter.add(darshanModel);
            }

            mDarshanScheduleAdapter = listRowAdapter; // <-- ADD THIS

            RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "DAILY", Collections.singletonList(newList));
            cardPresenterHeader.setDescription("DARSHAN SCHEDULE");

            mRowsAdapter.add(index, new ListRow(cardPresenterHeader, listRowAdapter));
        } else {
//            pendingRows.put(index, null);
        }
//        checkAndCommitRows();
    }

    private void createHistory(int index) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getHistory();
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                HistoryPresenter historyPresenter = new HistoryPresenter((MasterActivity) getActivity());
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(historyPresenter);

                try {
                    List<HistoryModel> historyList = new Gson().fromJson(response, new TypeToken<List<HistoryModel>>() {
                    }.getType());
                    for (HistoryModel historyModel : historyList) {
                        listRowAdapter.add(historyModel);
                    }

                    RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Shrinathji", Collections.singletonList(historyList));
                    cardPresenterHeader.setDescription("History");

//                    pendingRows.put(index, new ListRow(cardPresenterHeader, listRowAdapter));
                } catch (Exception e) {
                    e.printStackTrace();
//                    pendingRows.put(index, null);
                }
//                checkAndCommitRows();
            }

            @Override
            public void onFailure(String error, int responseCode) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }

            @Override
            public void onError(String error) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }
        });
    }

    private void createVallabhacharya(int index) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getVallabhacharya();
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                VallabhacharyaPresenter vallabhacharyaPresenter = new VallabhacharyaPresenter((MasterActivity) getActivity());
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(vallabhacharyaPresenter);

                try {
                    List<VallabhacharyaModel> pushtimargList = new Gson().fromJson(response, new TypeToken<List<VallabhacharyaModel>>() {
                    }.getType());
                    for (VallabhacharyaModel vallabhacharyaModel : pushtimargList) {
                        listRowAdapter.add(vallabhacharyaModel);
                    }

                    RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Mahaprabhuji", Collections.singletonList(pushtimargList));
                    cardPresenterHeader.setDescription("Vallabhacharya");

//                    pendingRows.put(index, new ListRow(cardPresenterHeader, listRowAdapter));
                } catch (Exception e) {
                    e.printStackTrace();
//                    pendingRows.put(index, null);
                }
//                checkAndCommitRows();
            }

            @Override
            public void onFailure(String error, int responseCode) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }

            @Override
            public void onError(String error) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }
        });
    }

    private void createPushtimarg(int index) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getPushtimarg();
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                PushtimargPresenter historyPresenter = new PushtimargPresenter((MasterActivity) getActivity());
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(historyPresenter);

                try {
                    List<PushtimargModel> pushtimargList = new Gson().fromJson(response, new TypeToken<List<PushtimargModel>>() {
                    }.getType());
                    for (PushtimargModel pushtimargModel : pushtimargList) {
                        listRowAdapter.add(pushtimargModel);
                    }

                    RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Shrinathji", Collections.singletonList(pushtimargList));
                    cardPresenterHeader.setDescription("Pushtimarg");

//                    pendingRows.put(index, new ListRow(cardPresenterHeader, listRowAdapter));
                } catch (Exception e) {
                    e.printStackTrace();
//                    pendingRows.put(index, null);
                }
//                checkAndCommitRows();
            }

            @Override
            public void onFailure(String error, int responseCode) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }

            @Override
            public void onError(String error) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }
        });
    }

    private void createBG(int index) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getChapters();
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
//                hideLoader();

                CardPresenter cardPresenter = new CardPresenter((MasterActivity) getActivity());
                final ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

//                String response = loadJSONFromAsset(getActivity(), "darshan.json");
                try {
                    Gson gson = new Gson();
                    List<ChapterModel> chapterList = gson.fromJson(response,
                            new TypeToken<List<ChapterModel>>() {
                            }.getType());

                    for (ChapterModel chapterModel : chapterList) {
                        listRowAdapter.add(chapterModel);
                    }

                    RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Bhagavad Gita", Collections.singletonList(chapterList));
                    cardPresenterHeader.setDescription("Chapters");
//                    pendingRows.put(index, new ListRow(cardPresenterHeader, listRowAdapter));

                } catch (Exception e) {
                    e.printStackTrace();
//                    pendingRows.put(index, null);
                }

//                checkAndCommitRows();

            }

            @Override
            public void onFailure(String error, int responseCode) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }

            @Override
            public void onError(String error) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }
        });
    }

    private void createVerse(int index) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getVerses();
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
//                hideLoader();

                versesOfTheDayPresenter = new VersesOfTheDayPresenter((MasterActivity) getActivity());
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(versesOfTheDayPresenter);

                try {
                    List<VersesModel> versesModelList = new Gson().fromJson(response, new TypeToken<List<VersesModel>>() {
                    }.getType());
                    VersesModel verseOfTheDay = getVerseOfTheDay(versesModelList);
                    if (verseOfTheDay != null) {
                        listRowAdapter.add(verseOfTheDay);
                    }

                    versesOfTheDayPresenter.setList(getListFromAdapter(listRowAdapter));
                    RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Verses", Collections.singletonList(getListFromAdapter(listRowAdapter)));
                    cardPresenterHeader.setDescription("of the day");

//                    pendingRows.put(index, new ListRow(cardPresenterHeader, listRowAdapter));
                } catch (Exception e) {
                    e.printStackTrace();
//                    pendingRows.put(index, null);
                }
//                checkAndCommitRows();
            }

            @Override
            public void onFailure(String error, int responseCode) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }

            @Override
            public void onError(String error) {
//                pendingRows.put(index, null);
//                checkAndCommitRows();
            }
        });
    }

    // --- Helpers ---

    public ArrayList<VersesModel> getListFromAdapter(ArrayObjectAdapter adapter) {
        ArrayList<VersesModel> versesList = new ArrayList<>();
        for (int i = 0; i < adapter.size(); i++) {
            versesList.add((VersesModel) adapter.get(i));
        }
        return versesList;
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

    public VersesModel getVerseOfTheDay(List<VersesModel> verses) {
        if (verses == null || verses.isEmpty()) return null;

        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String savedDate = SharePreferenceManager.getString("KEY_DATE");
        int savedIndex = SharePreferenceManager.getInt("KEY_VERSE_ID");

        if (today.equals(savedDate) && savedIndex != -1 && savedIndex < verses.size()) {
            return verses.get(savedIndex);
        }

        Random random = new Random();
        int newIndex = random.nextInt(verses.size());
        VersesModel newVerse = verses.get(newIndex);

        SharePreferenceManager.save("KEY_DATE", today);
        SharePreferenceManager.save("KEY_VERSE_ID", newVerse.getId());
        return newVerse;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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