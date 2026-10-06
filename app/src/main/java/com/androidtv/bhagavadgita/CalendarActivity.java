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
import android.widget.Toast;

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
import com.androidtv.bhagavadgita.adapter.UpcomingUtsavAdapter;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends MasterActivity {

    public static ViewPager2 viewPagerCalendar;
    private TextView tvMonthTitle, tvMonthVS;
    private OnBackPressedListener onBackPressedListener;
    public static VerticalGridView verticalGridView;

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

    private void setupWeekdayHeaders() {
        TextView[] dayViews = new TextView[]{
                findViewById(R.id.textSun),
                findViewById(R.id.textMon),
                findViewById(R.id.textTue),
                findViewById(R.id.textWed),
                findViewById(R.id.textThu),
                findViewById(R.id.textFri),
                findViewById(R.id.textSat)
        };

        String[] dayNames;
        switch (getLanguage()) {
            case "HINDI":
                dayNames = new String[]{"रवि", "सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि"};
                break;

            case "GUJARATI":
                dayNames = new String[]{"રવિ", "સોમ", "મંગળ", "બુધ", "ગુરુ", "શુક્ર", "શનિ"};
                break;

            case "ENGLISH":
            default:
                dayNames = new String[]{"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
                break;
        }

        for (int i = 0; i < dayViews.length; i++) {
            if (dayViews[i] != null) {
                dayViews[i].setText(dayNames[i]);
            }
        }
    }

    private void setupOtherHeaders() {
        TextView[] dayViews = new TextView[]{
                findViewById(R.id.textToday),
                findViewById(R.id.textKP),
                findViewById(R.id.textSP),
                findViewById(R.id.textAmavas),
                findViewById(R.id.textPunam),
                findViewById(R.id.textEvents),
                findViewById(R.id.textUE)};

        String[] langNames;
        switch (getLanguage()) {
            case "HINDI":
                langNames = new String[]{"आज", "कृष्ण पक्ष", "शुक्ल पक्ष", "अमावस्या", "पूर्णिमा", "आगामी कार्यक्रम", "आगामी कार्यक्रम"};
                break;

            case "GUJARATI":
                langNames = new String[]{"આજે", "કૃષ્ણ પક્ષ (વદ)", "શુક્લ પક્ષ (સુદ)", "અમાસ", "પૂનમ", "આગામી તહેવાર", "આગામી તહેવાર"};
                break;

            case "ENGLISH":
            default:
                langNames = new String[]{"Today", "Krushna Paksh", "Shukla Paksh", "Amavasya", "Purnima", "Upcoming Event", "Upcoming Event"};
                break;
        }

        for (int i = 0; i < dayViews.length; i++) {
            if (dayViews[i] != null) {
                dayViews[i].setText(langNames[i]);
            }
        }
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

        CalendarPagerAdapter pagerAdapter = new CalendarPagerAdapter(this, today);
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

                View textToday = findViewById(R.id.textToday);
                if (textToday != null) {
                    boolean isCurrentMonth = YearMonth.now().equals(shown);
                    textToday.setVisibility(isCurrentMonth ? View.VISIBLE : View.GONE);
                }

                getTippaniList(shownDate);

                findViewById(R.id.imagePrev).setAlpha(position <= pagerAdapter.getFirstPosition() ? 0f : 1f);
                findViewById(R.id.imageNext).setAlpha(position >= pagerAdapter.getLastPosition() ? 0f : 1f);

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
            int target = viewPagerCalendar.getCurrentItem() + 1;
            if (target <= pagerAdapter.getLastPosition()) {
                viewPagerCalendar.setCurrentItem(target, false);
            }
        });

        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            int target = viewPagerCalendar.getCurrentItem() - 1;
            if (target >= pagerAdapter.getFirstPosition()) {
                viewPagerCalendar.setCurrentItem(target, false);
            }
        });

        verticalGridView = findViewById(R.id.verticalGridView);

        setupWeekdayHeaders();
        setupOtherHeaders();
        dayEdgeColor(false, findViewById(R.id.textSun), ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
        dayEdgeColor(false, findViewById(R.id.textMon), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textTue), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textWed), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textThu), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textFri), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textSat), Color.GRAY);
        dayEdgeColor(false, findViewById(R.id.textUE), Color.GRAY);

        dayEdgeColor(true, findViewById(R.id.textToday), getColor(R.color.colorFocus));

        dayEdgeColor(false, findViewById(R.id.textSP), getColor(R.color.colorShukla));
        dayEdgeColor(false, findViewById(R.id.textKP), getColor(R.color.colorKrushna));

        dayEdgeColor(false, findViewById(R.id.textAmavas), getColor(R.color.colorWhite25));
        dayEdgeColor(false, findViewById(R.id.textPunam), getColor(R.color.colorWhite25));
        dayEdgeColor(false, findViewById(R.id.textEvents), getColor(R.color.colorWhite25));

        verticalGridView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (viewPagerCalendar != null && viewPagerCalendar.getAdapter() != null) {
                        int currentItem = viewPagerCalendar.getCurrentItem();
                        int totalItems = viewPagerCalendar.getAdapter().getItemCount();

                        if (currentItem + 1 < totalItems) {
                            viewPagerCalendar.setCurrentItem(currentItem + 1, false);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        View.OnKeyListener downToGridListener = (v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
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

        View textToday = findViewById(R.id.textToday);
        if (textToday != null) {
            textToday.setVisibility(View.VISIBLE);
        }

        boolean calAP = SharePreferenceManager.getBoolean("CAL_AP", true);
        if (!calAP) {
            findViewById(R.id.textAmavas).setVisibility(View.GONE);
            findViewById(R.id.textPunam).setVisibility(View.GONE);
        } else {
            findViewById(R.id.textAmavas).setVisibility(View.VISIBLE);
            findViewById(R.id.textPunam).setVisibility(View.VISIBLE);
        }

        boolean calEVENTS = SharePreferenceManager.getBoolean("CAL_EVENTS", true);
        if (!calEVENTS) {
            findViewById(R.id.textEvents).setVisibility(View.GONE);
        } else {
            findViewById(R.id.textEvents).setVisibility(View.VISIBLE);
        }
    }

    private String getMonthTitle(YearMonth yearMonth, Locale locale) {
        String month = yearMonth.getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, locale)
                .toUpperCase(locale);
        String year = formatNumber(yearMonth.getYear(), locale);
        return month + " " + year;
    }

    private String formatNumber(int number, Locale locale) {
        Locale numLocale;
        switch (locale.getLanguage()) {
            case "hi":
                numLocale = Locale.forLanguageTag("hi-IN-u-nu-deva"); // ०१२३...
                break;
            case "gu":
                numLocale = Locale.forLanguageTag("gu-IN-u-nu-gujr"); // ૦૧૨૩...
                break;
            default:
                return String.valueOf(number); // 2026
        }
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(numLocale);
        nf.setGroupingUsed(false);
        return nf.format(number);
    }

    private String toIndicDigits(int number, Language lang) {
        String[] hindi = {"०", "१", "२", "३", "४", "५", "६", "७", "८", "९"};
        String[] gujarati = {"૦", "૧", "૨", "૩", "૪", "૫", "૬", "૭", "૮", "૯"};
        String[] map = (lang == Language.HINDI) ? hindi
                : (lang == Language.GUJARATI) ? gujarati : null;

        String s = String.valueOf(number);
        if (map == null) return s;

        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(map[c - '0']);
        }
        return sb.toString();
    }

    public static String getLanguage() {
        String lang = SharePreferenceManager.getString("LANGUAGE");
        if (lang == null) {
            return "ENGLISH";
        }
        return lang.trim().toUpperCase();
    }

    public String getTitle(YearMonth yearMonth) {
        switch (getLanguage()) {
            case "HINDI":
                return getMonthTitle(yearMonth, new java.util.Locale("hi"));
            case "GUJARATI":
                return getMonthTitle(yearMonth, new java.util.Locale("gu"));
            default:
                return getMonthTitle(yearMonth, new java.util.Locale("en"));
        }
    }

    private void updateHeaderTitle(int position, YearMonth baseMonth) {
        YearMonth shown = baseMonth.plusMonths(position - CalendarPagerAdapter.CENTER_POSITION);
        tvMonthTitle.setText(getTitle(shown));

        LocalDate today = LocalDate.now();
        LocalDate firstDate = YearMonth.from(today).equals(shown) ? today : shown.atDay(1);

        String vs = PanchangCalculator.getPanchang(firstDate, CalendarUtils.LAT,
                CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getVS();
        tvMonthVS.setText(vs);
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                .getPanchang(LocalDate.of(selectedDate.getYear(), 12, 31),
                        CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET)
                .getVSYear();

//        int vikramSamvatYear = PanchangCalculator
//                .getPanchang(selectedDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET)
//                .getVSYear();

        // Load file by calendar year -> assets/tippani_2026.json
        Map<String, FestivalModel> activeFestivalMap =
                loadTippaniForYear(CalendarActivity.this, vikramSamvatYear);

        if (activeFestivalMap == null) return;

        LocalDate today = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(today);

        YearMonth selectedYearMonth = YearMonth.from(selectedDate);
        List<Map.Entry<String, FestivalModel>> monthEntries = new ArrayList<>();

        // 1. Keep only the entries of the selected month
        for (Map.Entry<String, FestivalModel> entry : activeFestivalMap.entrySet()) {
            String dateKey = entry.getKey(); // "yyyy-MM-dd"
            if (dateKey == null) continue;
            try {
                LocalDate entryDate = LocalDate.parse(dateKey);
                if (YearMonth.from(entryDate).equals(selectedYearMonth)) {
                    monthEntries.add(entry);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. Sort by date
        Collections.sort(monthEntries, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

        // 3. Build the list
        int targetInitialIndex = -1;
        List<FestivalModel> festivalList = new ArrayList<>();

        for (int i = 0; i < monthEntries.size(); i++) {
            Map.Entry<String, FestivalModel> entry = monthEntries.get(i);
            festivalList.add(entry.getValue());

            // Only check for "today or future" if viewing the current ongoing month
            if (selectedYearMonth.equals(currentYearMonth) && targetInitialIndex == -1) {
                LocalDate entryDate = LocalDate.parse(entry.getKey());
                if (!entryDate.isBefore(today)) {
                    targetInitialIndex = i;
                }
            }
        }

        if (selectedYearMonth.isBefore(currentYearMonth)) {
            // Past month: Always start at the top
            targetInitialIndex = 0;
        } else if (selectedYearMonth.isAfter(currentYearMonth)) {
            // Future month: Always start at the top
            targetInitialIndex = 0;
        } else {
            // Current month: If no upcoming events remain, focus the last event
            if (targetInitialIndex == -1) {
                targetInitialIndex = festivalList.isEmpty() ? 0 : festivalList.size() - 1;
            }
        }

        UpcomingUtsavAdapter upcomingUtsavAdapter = new UpcomingUtsavAdapter(CalendarActivity.this, festivalList);
        verticalGridView.setAdapter(upcomingUtsavAdapter);

        verticalGridView.setWindowAlignmentOffsetPercent(0.0f);
        verticalGridView.setWindowAlignmentOffset(0);
        verticalGridView.setWindowAlignment(BaseGridView.WINDOW_ALIGN_LOW_EDGE);

        verticalGridView.setItemAlignmentOffsetPercent(0.0f);
        verticalGridView.setItemSpacing(5);

        // 5. Select and scroll to target position
        final int initialPosition = Math.max(0, targetInitialIndex);
        verticalGridView.post(() -> {
            verticalGridView.setSelectedPosition(initialPosition);
        });
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
                ColorUtils.whiten(Color.parseColor(rightEdgeColor), 0.1f));
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

    public void focusFirstDayOfCurrentPage() {
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
            List<DayModel> days = adapter.getDaysList();
            int targetPos = -1;

            // 1. If currently on the active month, check for today's date
            if (days != null) {
                for (int i = 0; i < days.size(); i++) {
                    DayModel d = days.get(i);
                    if (d != null && d.isCurrentMonth() && d.isToday()) {
                        targetPos = i;
                        break;
                    }
                }
            }

            // 2. If today is not in this month (past or future month), select the 1st day
            if (targetPos < 0) {
                targetPos = adapter.getFirstDayPosition();
            }

            // 3. Fallback scan for first current month day if getFirstDayPosition() returned invalid
            if (targetPos < 0 && days != null) {
                for (int i = 0; i < days.size(); i++) {
                    DayModel d = days.get(i);
                    if (d != null && d.isCurrentMonth()) {
                        targetPos = i;
                        break;
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
