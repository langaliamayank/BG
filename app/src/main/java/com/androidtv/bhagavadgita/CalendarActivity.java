package com.androidtv.bhagavadgita;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.androidtv.bhagavadgita.adapter.CalendarAdapter;
import com.androidtv.bhagavadgita.adapter.CalendarPagerAdapter;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.comman.ColorUtils;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.fragment.CalendarMonthFragment;
import com.androidtv.bhagavadgita.fragment.SpinnerSupportFragment;
import com.androidtv.bhagavadgita.model.DayModel;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarActivity extends MasterActivity {

    public static ViewPager2 viewPagerCalendar;
    private TextView tvMonthTitle, tvMonthVS;
    private OnBackPressedListener onBackPressedListener;
    private VerticalGridView verticalGridView;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String SPINNER_TAG = "LoadingOverlay";
    private SpinnerSupportFragment mSpinnerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        showLoader();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeView();
                initializeData();
            }
        }, Constants.INTERVAL);
    }

    private void showLoader() {
        if (getSupportFragmentManager().findFragmentByTag(SPINNER_TAG) != null) return;
        mSpinnerFragment = new SpinnerSupportFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mSpinnerFragment, SPINNER_TAG)
                .commitAllowingStateLoss();
    }

    private void hideLoader() {
        if (isFinishing() || isDestroyed()) return;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SPINNER_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
            mSpinnerFragment = null;
        }
    }

    private void initializeData() {
        hideLoader();

        findViewById(R.id.parentContainer).setVisibility(View.VISIBLE);

        YearMonth today = YearMonth.now();
        LocalDate todayDate = LocalDate.now();

        CalendarPagerAdapter pagerAdapter = new CalendarPagerAdapter(this, today, Language.ENGLISH);
        viewPagerCalendar.setAdapter(pagerAdapter);
        viewPagerCalendar.setCurrentItem(CalendarPagerAdapter.CENTER_POSITION, false);

        // Optional: Update Title on page change
        updateHeaderTitle(CalendarPagerAdapter.CENTER_POSITION, today);
        viewPagerCalendar.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                updateHeaderTitle(position, today);

                YearMonth shown = today.plusMonths(position - CalendarPagerAdapter.CENTER_POSITION);
                LocalDate shownDate = YearMonth.from(LocalDate.now()).equals(shown)
                        ? LocalDate.now()
                        : shown.atDay(1);

                getTippaniList(shownDate);

                // Focus the first day on the visible page
//                focusFirstDayOfCurrentPage();

                viewPagerCalendar.post(() -> {
                    // ViewPager2 tags fragment tags with "f" + position
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + position);
                    if (fragment instanceof CalendarMonthFragment) {
                        ((CalendarMonthFragment) fragment).requestFocusOnCurrentDay();
                    }
                });

            }
        });

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            viewPagerCalendar.setCurrentItem(viewPagerCalendar.getCurrentItem() + 1, false);
        });

        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            viewPagerCalendar.setCurrentItem(viewPagerCalendar.getCurrentItem() - 1, false);
        });

        verticalGridView = findViewById(R.id.verticalGridView);

        dayEdgeColor(false, findViewById(R.id.textSun), Color.RED);
        dayEdgeColor(false, findViewById(R.id.textMon), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textTue), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textWed), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textThu), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textFri), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textSat), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textUE), Color.GRAY);

        dayEdgeColor(true, findViewById(R.id.imageToday), getColor(R.color.colorFocus));
        dayEdgeColor(false, findViewById(R.id.imageSP), getColor(R.color.colorShukla));
        dayEdgeColor(false, findViewById(R.id.imageKP), getColor(R.color.colorKrushna));


        View.OnKeyListener downToGridListener = (v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
//                RecyclerView internalPagerRecycler = (RecyclerView) viewPagerCalendar.getChildAt(0);
//                if (internalPagerRecycler != null) {
//                    int currentItem = viewPagerCalendar.getCurrentItem();
//                    RecyclerView.ViewHolder pageViewHolder =
//                            internalPagerRecycler.findViewHolderForAdapterPosition(currentItem);
//
//                    if (pageViewHolder != null) {
//                        RecyclerView daysGrid = pageViewHolder.itemView.findViewById(R.id.recyclerViewDays);
//
//                        if (daysGrid != null && daysGrid.getAdapter() instanceof CalendarAdapter) {
//                            CalendarAdapter adapter = (CalendarAdapter) daysGrid.getAdapter();
//                            int firstDayPos = adapter.getFirstDayPosition();
//
//                            if (firstDayPos != -1) {
//                                RecyclerView.ViewHolder dayViewHolder =
//                                        daysGrid.findViewHolderForAdapterPosition(firstDayPos);
//
//                                if (dayViewHolder != null) {
//                                    dayViewHolder.itemView.requestFocus();
//                                    return true;
//                                }
//                            }
//                        }
//                    }
//                }

                RecyclerView daysGrid = findViewById(R.id.recyclerViewDays);
                if (daysGrid != null && daysGrid.getAdapter() instanceof CalendarAdapter) {
                    CalendarAdapter adapter = (CalendarAdapter) daysGrid.getAdapter();
                    List<DayModel> list = adapter.getDaysList();

                    int targetIndex = -1;
                    for (int i = 0; i < list.size(); i++) {
                        DayModel day = list.get(i);
                        if (day != null && day.isToday()) {
                            targetIndex = i;
                            break;
                        }
                    }

                    // Fallback to first day of month if 'today' is not in this month
                    if (targetIndex == -1) {
                        targetIndex = adapter.getFirstDayPosition();
                    }

                    if (targetIndex != -1) {
                        RecyclerView.ViewHolder vh = daysGrid.findViewHolderForAdapterPosition(targetIndex);
                        if (vh != null) {
                            View selector = vh.itemView.findViewById(R.id.selector);
                            if (selector != null) {
                                selector.requestFocus();
                                return true;
                            }
                        }
                    }
                }

            }
            return false;
        };

        findViewById(R.id.btnNext).setOnKeyListener(downToGridListener);
        findViewById(R.id.btnPrev).setOnKeyListener(downToGridListener);

        getTippaniList(todayDate);
    }

    private void initializeView() {
        viewPagerCalendar = findViewById(R.id.viewPagerCalendar);
        viewPagerCalendar.setUserInputEnabled(false);

        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvMonthVS = findViewById(R.id.tvMonthVS);
    }

    private View getCurrentPageView(int position) {
        RecyclerView recyclerView = (RecyclerView) viewPagerCalendar.getChildAt(0);
        RecyclerView.ViewHolder viewHolder =
                recyclerView.findViewHolderForAdapterPosition(position);
        return viewHolder != null ? viewHolder.itemView : null;
    }

    private void updateHeaderTitle(int position, YearMonth baseMonth) {
        YearMonth shown = baseMonth.plusMonths(position - CalendarPagerAdapter.CENTER_POSITION);
        String monthEn = shown.getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                .toUpperCase();

//        String monthHi = shown.getMonth()
//                .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("hi"));
//
//        String monthGu = shown.getMonth()
//                .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("gu"));

        String title = monthEn + " " + shown.getYear();

        tvMonthTitle.setText(title);

        LocalDate today = LocalDate.now();
        LocalDate targetDate = YearMonth.from(today).equals(shown)
                ? today                     // showing current month -> use today's date
                : shown.atEndOfMonth();     // otherwise fall back to some date in that month

        tvMonthVS.setText(PanchangCalculator.getPanchang(targetDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getVS(Language.ENGLISH));
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    protected void onResume() {
        super.onResume();

        LogTag.e("currentFocus " + getCurrentFocus());

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (onBackPressedListener != null) {
                            onBackPressedListener.doBack();
                        } else {
                            finish();
                        }
                    }
                });
    }

    private void getTippaniList(LocalDate selectedDate) {
        int vikramSamvatYear = PanchangCalculator
                .getPanchang(selectedDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET)
                .getVSYear(); // adjust to your actual method for raw VS year int

        Map<String, FestivalModel> activeFestivalMap = loadTippaniForYear(CalendarActivity.this, vikramSamvatYear);

        if (activeFestivalMap != null) {
            YearMonth selectedYearMonth = YearMonth.from(selectedDate);

            List<Map.Entry<String, FestivalModel>> filteredList = new ArrayList<>();

            for (Map.Entry<String, FestivalModel> entry : activeFestivalMap.entrySet()) {
                String dateKey = entry.getKey(); // "yyyy-MM-dd"
                if (dateKey != null) {
                    try {
                        LocalDate entryDate = LocalDate.parse(dateKey);
//                        LocalDate today = LocalDate.now();
//
//                        // entryDate is today or in the future
//                        if (!entryDate.isBefore(today)) {
//                            // If you also need to ensure it matches the selected YearMonth:
//                            if (selectedYearMonth == null || YearMonth.from(entryDate).equals(selectedYearMonth)) {
//                                filteredList.add(entry);
//                            }
//                        }

                        if (YearMonth.from(entryDate).equals(selectedYearMonth)) {
                            filteredList.add(entry);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Sort chronologically within the month
            Collections.sort(filteredList, (o1, o2) -> o1.getKey().compareTo(o2.getKey())); // "yyyy-MM-dd" strings sort correctly as plain strings

            List<FestivalModel> festivalList = new ArrayList<>();
            for (Map.Entry<String, FestivalModel> entry : filteredList) {
                festivalList.add(entry.getValue());
            }

            UpcomingUtsavAdapter upcomingUtsavAdapter = new UpcomingUtsavAdapter(CalendarActivity.this, festivalList);
            verticalGridView.setAdapter(upcomingUtsavAdapter);

            verticalGridView.setWindowAlignmentOffsetPercent(0.0f);
            verticalGridView.setWindowAlignmentOffset(0);
            verticalGridView.setWindowAlignment(BaseGridView.WINDOW_ALIGN_LOW_EDGE);

            verticalGridView.setItemAlignmentOffsetPercent(0.0f);
            verticalGridView.setItemSpacing(5);

            verticalGridView.setSelectedPosition(0);
        }
    }

    public class UpcomingUtsavAdapter extends RecyclerView.Adapter<UpcomingUtsavAdapter.UtsavViewHolder> {

        private List<FestivalModel> mList;
        private CalendarActivity mContext;

        public UpcomingUtsavAdapter(CalendarActivity context, List<FestivalModel> listItems) {
            mContext = context;
            mList = listItems;
        }

        @Override
        public UtsavViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.card_calendar_event_item, viewGroup, false);
            return new UtsavViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UtsavViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
            FestivalModel festivalModel = mList.get(position);
            viewHolder.textTitle.setText(festivalModel.getTitle());

            String tithi;
            try {
                LocalDate localDate = LocalDate.parse(festivalModel.getDate()); // expects "yyyy-MM-dd"
                tithi = PanchangCalculator.getPanchang(
                        localDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getCompactDescription();
            } catch (Exception e) {
                tithi = "";
            }

            viewHolder.textTithi.setText(tithi);
            viewHolder.textDescription.setText(festivalModel.getDescription());
            viewHolder.textDate.setText(festivalModel.getDate());

            viewHolder.itemView.setNextFocusLeftId(R.id.selector);
            edgeColor(viewHolder, ContextCompat.getColor(mContext, R.color.colorBlack50));
            viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    edgeColor(viewHolder, ContextCompat.getColor(mContext, hasFocus ? R.color.colorWhite : R.color.colorBlack50));

                    if (mContext instanceof CalendarActivity) {
                        if (hasFocus) {
                            ((CalendarActivity) mContext).highlightCalendarDate(festivalModel.getDate());
                        } else {
                            ((CalendarActivity) mContext).clearCalendarHighlight();
                        }
                    }
                }
            });

//          Selected Jump to First Active Day of the Month
//            viewHolder.itemView.setOnKeyListener((v, keyCode, event) -> {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
//                    RecyclerView daysGrid = mContext.findViewById(R.id.recyclerViewDays);
//                    if (daysGrid != null) {
//                        int childCount = daysGrid.getChildCount();
//                        for (int i = 0; i < childCount; i++) {
//                            View child = daysGrid.getChildAt(i);
//                            View selector = child.findViewById(R.id.selector);
//                            if (child.getVisibility() == View.VISIBLE && selector != null && selector.isFocusable()) {
//                                selector.requestFocus();
//                                return true;
//                            }
//                        }
//                    }
//                }
//                return false;
//            });

            // Jump to Selected/Today's Date
            viewHolder.itemView.setOnKeyListener((v, keyCode, event) -> {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    // Loop from last item back to first item
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (position == getItemCount() - 1 && getItemCount() > 0) {
                            verticalGridView.setSelectedPositionSmooth(0);
                            // Use post to ensure focus shifts after layout passes
                            verticalGridView.post(() -> {
                                RecyclerView.ViewHolder firstVh = verticalGridView.findViewHolderForAdapterPosition(0);
                                if (firstVh != null) {
                                    firstVh.itemView.requestFocus();
                                }
                            });
                            return true;
                        }
                    }

                    // Jump to Selected/Today's Date on DPAD_LEFT
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        RecyclerView daysGrid = mContext.findViewById(R.id.recyclerViewDays);
                        if (daysGrid != null && daysGrid.getAdapter() instanceof CalendarAdapter) {
                            CalendarAdapter adapter = (CalendarAdapter) daysGrid.getAdapter();
                            List<DayModel> list = adapter.getDaysList();

                            int targetIndex = -1;
                            for (int i = 0; i < list.size(); i++) {
                                DayModel day = list.get(i);
                                if (day != null && day.isToday()) {
                                    targetIndex = i;
                                    break;
                                }
                            }

                            if (targetIndex == -1) {
                                targetIndex = adapter.getFirstDayPosition();
                            }

                            if (targetIndex != -1) {
                                RecyclerView.ViewHolder vh = daysGrid.findViewHolderForAdapterPosition(targetIndex);
                                if (vh != null) {
                                    View selector = vh.itemView.findViewById(R.id.selector);
                                    if (selector != null) {
                                        selector.requestFocus();
                                        return true;
                                    }
                                }
                            }
                        }
                        return true;
                    }
                }
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public void edgeColor(UtsavViewHolder holder, @ColorInt int color) {
            LayerDrawable layerDrawable = (LayerDrawable) holder.itemView.getBackground();
            GradientDrawable leftEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.leftEdge);
            leftEdge.mutate();
            leftEdge.setColor(color);

            String rightEdgeColor = SharePreferenceManager.getString("KEY_THEME_COLOR");
            GradientDrawable rightEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.rightEdge);
            rightEdge.mutate();
            rightEdge.setColor(ColorUtils.darken(Color.parseColor(rightEdgeColor), 0.1f));
        }

        class UtsavViewHolder extends RecyclerView.ViewHolder {
            private TextView textTitle, textTithi, textDescription, textDate;

            public UtsavViewHolder(View itemView) {
                super(itemView);
                textTitle = itemView.findViewById(R.id.textTitle);
                textTithi = itemView.findViewById(R.id.textTithi);
                textDescription = itemView.findViewById(R.id.textDescription);
                textDate = itemView.findViewById(R.id.textDate);

                itemView.setBackgroundResource(R.drawable.bg_calendar);
                itemView.setFocusable(true);
            }
        }
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

    private View lastAnimatedView = null;

    public void dayEdgeColor(boolean bool, View view, @ColorInt int color) {
        LayerDrawable layerDrawable = (LayerDrawable) view.getBackground();
        GradientDrawable leftEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.leftEdge);
        leftEdge.mutate();
        leftEdge.setColor(bool ? ColorUtils.lighten(color, 0.1f) : color);

        String rightEdgeColor = SharePreferenceManager.getString("KEY_THEME_COLOR");
        GradientDrawable rightEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.rightEdge);
        rightEdge.mutate();
        rightEdge.setColor(bool ? ColorUtils.lighten(color, 0.5f) :
                ColorUtils.darken(Color.parseColor(rightEdgeColor), 0.1f));
    }

    public void highlightCalendarDate(String targetDateStr) {
        if (targetDateStr == null || targetDateStr.isEmpty()) return;

        // 1. Reset previously animated view to avoid stale scale states
        if (lastAnimatedView != null) {
            lastAnimatedView.clearAnimation();
            lastAnimatedView = null;
        }

        try {
            // Extract day of month as string without leading zeroes (e.g., "2026-09-04" -> "4")
            LocalDate date = LocalDate.parse(targetDateStr);
            String targetDay = String.valueOf(date.getDayOfMonth());

            RecyclerView daysGrid = findViewById(R.id.recyclerViewDays);
            if (daysGrid == null || !(daysGrid.getAdapter() instanceof CalendarAdapter)) return;

            CalendarAdapter adapter = (CalendarAdapter) daysGrid.getAdapter();
            List<DayModel> days = adapter.getDaysList();

            for (int i = 0; i < days.size(); i++) {
                DayModel day = days.get(i);
                if (day != null && day.isCurrentMonth() && targetDay.equals(day.getPrimaryDate())) {
                    RecyclerView.ViewHolder vh = daysGrid.findViewHolderForAdapterPosition(i);
                    if (vh != null) {
                        View targetView = vh.itemView.findViewById(R.id.childLayout);
                        if (targetView == null) targetView = vh.itemView;

                        Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.wobble);
                        targetView.startAnimation(pulseAnim);

                        lastAnimatedView = targetView;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCalendarHighlight() {
        if (lastAnimatedView != null) {
            lastAnimatedView.clearAnimation();
            lastAnimatedView = null;
        }
    }

    private void focusFirstDayOfCurrentPage() {
        focusFirstDayWithRetry(0);
    }

    private void focusFirstDayWithRetry(final int attempt) {
        if (isFinishing() || isDestroyed() || viewPagerCalendar == null) return;

        viewPagerCalendar.post(() -> {
            RecyclerView internalPager = (RecyclerView) viewPagerCalendar.getChildAt(0);
            if (internalPager == null) {
                retryFocus(attempt);
                return;
            }

            int currentPos = viewPagerCalendar.getCurrentItem();
            RecyclerView.ViewHolder pageVh = internalPager.findViewHolderForAdapterPosition(currentPos);

            if (pageVh == null || pageVh.itemView == null) {
                retryFocus(attempt);
                return;
            }

            RecyclerView daysGrid = pageVh.itemView.findViewById(R.id.recyclerViewDays);
            if (daysGrid == null || !(daysGrid.getAdapter() instanceof CalendarAdapter)) {
                retryFocus(attempt);
                return;
            }

            CalendarAdapter adapter = (CalendarAdapter) daysGrid.getAdapter();
            int targetPos = adapter.getFirstDayPosition();

            if (targetPos < 0) {
                List<DayModel> days = adapter.getDaysList();
                if (days != null) {
                    for (int i = 0; i < days.size(); i++) {
                        DayModel d = days.get(i);
                        if (d != null && d.isCurrentMonth()) {
                            targetPos = i;
                            break;
                        }
                    }
                }
            }

            if (targetPos < 0) {
                retryFocus(attempt);
                return;
            }

            final int finalPos = targetPos;
            RecyclerView.ViewHolder targetCellVh = daysGrid.findViewHolderForAdapterPosition(finalPos);

            if (targetCellVh != null && targetCellVh.itemView != null) {
                boolean success = applyFocus(targetCellVh.itemView);
                if (!success && attempt < 15) {
                    retryFocus(attempt);
                }
            } else {
                daysGrid.scrollToPosition(finalPos);
                retryFocus(attempt);
            }
        });
    }

    private void retryFocus(int attempt) {
        if (attempt < 15) { // Retries up to ~750ms total
            handler.postDelayed(() -> focusFirstDayWithRetry(attempt + 1), 50);
        }
    }

    private boolean applyFocus(View itemView) {
        View selector = itemView.findViewById(R.id.selector);
        if (selector != null) {
            selector.setFocusable(true);
            selector.setFocusableInTouchMode(true);
            return selector.requestFocus();
        }
        itemView.setFocusable(true);
        return itemView.requestFocus();
    }
}
