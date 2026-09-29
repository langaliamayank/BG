package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.adapter.CalendarAdapter;
import com.androidtv.bhagavadgita.adapter.CalendarPagerAdapter;
import com.androidtv.bhagavadgita.adapter.UpcomingUtsavAdapter;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.comman.ColorUtils;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
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

public class CalendarFragment extends BrowseSupportFragment implements OnBackPressedListener {

    public static ViewPager2 viewPagerCalendar;
    private TextView tvMonthTitle, tvMonthVS;
    public static VerticalGridView verticalGridView;
    private ArrayObjectAdapter mRowsAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private View lastAnimatedView = null;

    @Override
    public void doBack() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUIElements();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate your custom layout containing the ViewPager and VerticalGridView
//        View root = inflater.inflate(R.layout.activity_calendar, container, false);
        View root = super.onCreateView(inflater, container, savedInstanceState);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView(view);
        initializeData();

        // Tweak Leanback's internal VerticalGridView if needed via rows fragment
        if (getRowsSupportFragment() != null) {
            VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
            if (vgv != null) {
                vgv.setItemAnimator(null);
                vgv.setClipChildren(false);
                vgv.setClipToPadding(false);
            }
        }
    }

    private void setupUIElements() {
        setHeadersState(HEADERS_DISABLED);
        setHeadersTransitionOnBackEnabled(false);
    }

    private void initializeView(View view) {
        viewPagerCalendar = view.findViewById(R.id.viewPagerCalendar);
        viewPagerCalendar.setUserInputEnabled(false);

        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        tvMonthVS = view.findViewById(R.id.tvMonthVS);
        verticalGridView = view.findViewById(R.id.verticalGridView);
    }

    private void initializeData() {
        YearMonth today = YearMonth.now();
        LocalDate todayDate = LocalDate.now();

        CalendarPagerAdapter pagerAdapter = new CalendarPagerAdapter(requireActivity(), today, Language.ENGLISH);
        viewPagerCalendar.setAdapter(pagerAdapter);
        viewPagerCalendar.setCurrentItem(CalendarPagerAdapter.CENTER_POSITION, false);

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

                viewPagerCalendar.post(() -> {
                    Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + position);
                    if (fragment instanceof CalendarMonthFragment) {
                        ((CalendarMonthFragment) fragment).requestFocusOnCurrentDay();
                    }
                });
            }
        });

        View btnNext = getView() != null ? getView().findViewById(R.id.btnNext) : null;
        View btnPrev = getView() != null ? getView().findViewById(R.id.btnPrev) : null;

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> viewPagerCalendar.setCurrentItem(viewPagerCalendar.getCurrentItem() + 1, false));
        }
        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> viewPagerCalendar.setCurrentItem(viewPagerCalendar.getCurrentItem() - 1, false));
        }

        getTippaniList(todayDate);
    }

    private void updateHeaderTitle(int position, YearMonth baseMonth) {
        YearMonth shown = baseMonth.plusMonths(position - CalendarPagerAdapter.CENTER_POSITION);
        String monthEn = shown.getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                .toUpperCase();

        String title = monthEn + " " + shown.getYear();
        if (tvMonthTitle != null) tvMonthTitle.setText(title);

        LocalDate today = LocalDate.now();
        LocalDate targetDate = YearMonth.from(today).equals(shown) ? today : shown.atEndOfMonth();

        if (tvMonthVS != null) {
            tvMonthVS.setText(PanchangCalculator.getPanchang(targetDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getVS(Language.ENGLISH));
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

    private void getTippaniList(LocalDate selectedDate) {
        int vikramSamvatYear = PanchangCalculator
                .getPanchang(selectedDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET)
                .getVSYear();

        Map<String, FestivalModel> activeFestivalMap = loadTippaniForYear(requireContext(), vikramSamvatYear);

        if (activeFestivalMap != null) {
            YearMonth selectedYearMonth = YearMonth.from(selectedDate);
            LocalDate today = LocalDate.now();
            YearMonth currentYearMonth = YearMonth.from(today);

            List<Map.Entry<String, FestivalModel>> monthEntries = new ArrayList<>();

            for (Map.Entry<String, FestivalModel> entry : activeFestivalMap.entrySet()) {
                String dateKey = entry.getKey();
                if (dateKey != null) {
                    try {
                        LocalDate entryDate = LocalDate.parse(dateKey);
                        if (YearMonth.from(entryDate).equals(selectedYearMonth)) {
                            monthEntries.add(entry);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            Collections.sort(monthEntries, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

            List<FestivalModel> festivalList = new ArrayList<>();
            int targetInitialIndex = -1;

            for (int i = 0; i < monthEntries.size(); i++) {
                Map.Entry<String, FestivalModel> entry = monthEntries.get(i);
                festivalList.add(entry.getValue());

                if (selectedYearMonth.equals(currentYearMonth) && targetInitialIndex == -1) {
                    LocalDate entryDate = LocalDate.parse(entry.getKey());
                    if (!entryDate.isBefore(today)) {
                        targetInitialIndex = i;
                    }
                }
            }

            if (selectedYearMonth.isBefore(currentYearMonth) || selectedYearMonth.isAfter(currentYearMonth)) {
                targetInitialIndex = 0;
            } else if (targetInitialIndex == -1) {
                targetInitialIndex = festivalList.isEmpty() ? 0 : festivalList.size() - 1;
            }

            UpcomingUtsavAdapter upcomingUtsavAdapter =
                    new UpcomingUtsavAdapter((MasterActivity) requireActivity(), festivalList);

            if (verticalGridView != null) {
                verticalGridView.setAdapter(upcomingUtsavAdapter);
                verticalGridView.setWindowAlignmentOffsetPercent(0.0f);
                verticalGridView.setWindowAlignmentOffset(0);
                verticalGridView.setWindowAlignment(BaseGridView.WINDOW_ALIGN_LOW_EDGE);
                verticalGridView.setItemAlignmentOffsetPercent(0.0f);
                verticalGridView.setItemSpacing(5);

                final int initialPosition = Math.max(0, targetInitialIndex);
                verticalGridView.post(() -> verticalGridView.setSelectedPosition(initialPosition));
            }
        }
    }

    public void highlightCalendarDate(String targetDateStr) {
        if (targetDateStr == null || targetDateStr.isEmpty()) return;

        if (lastAnimatedView != null) {
            lastAnimatedView.clearAnimation();
            lastAnimatedView = null;
        }

        try {
            LocalDate date = LocalDate.parse(targetDateStr);
            String targetDay = String.valueOf(date.getDayOfMonth());

            View root = getView();
            if (root == null) return;
            RecyclerView daysGrid = root.findViewById(R.id.recyclerViewDays);
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

                        Animation pulseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.wobble);
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
}