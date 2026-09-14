package com.androidtv.bhagavadgita.calendar;

import android.content.Context;

import com.androidtv.bhagavadgita.comman.MyApplication;
import com.androidtv.bhagavadgita.model.DayModel;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarUtils {

    // Nathdwara, Rajasthan, India coordinates
    public static final double LAT = 24.9379;
    public static final double LON = 73.8235;
    public static final double UTC_OFFSET = 5.5;

    /**
     * Backward-compatible overload — defaults to English.
     */
    public static List<DayModel> generateCalendarDays(YearMonth yearMonth) {
        return generateCalendarDays(yearMonth, Language.ENGLISH);
    }

    /**
     * Builds a full 6x7 (42-cell) grid for the given month: leading days from the
     * previous month, all days of the current month, and trailing days from the
     * next month — enough to always fill complete weeks, Google-Calendar style.
     */
//    public static List<DayModel> generateCalendarDays(YearMonth yearMonth, Language lang) {
//        List<DayModel> days = new ArrayList<>();
//        LocalDate today = LocalDate.now();
//
//        LocalDate firstOfMonth = yearMonth.atDay(1);
//        LocalDate lastOfMonth = yearMonth.atEndOfMonth();
//
//        // Sunday-first grid (matches your screenshot header: SUN MON TUE ... SAT)
//        int firstDayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() % 7; // Sun=0 ... Sat=6
//        LocalDate gridStart = firstOfMonth.minusDays(firstDayOfWeekValue);
//
//        int trailingDaysNeeded = (7 - (lastOfMonth.getDayOfWeek().getValue() % 7) - 1) % 7;
//        LocalDate gridEnd = lastOfMonth.plusDays(trailingDaysNeeded);
//
//        // Pad to a full 6 rows (42 cells) so the grid height stays consistent month to month
//        long totalCells = java.time.temporal.ChronoUnit.DAYS.between(gridStart, gridEnd) + 1;
//        if (totalCells < 42) {
//            gridEnd = gridEnd.plusDays(42 - totalCells);
//        }
//
//        for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1)) {
//            boolean isCurrentMonth = d.getMonth() == yearMonth.getMonth() && d.getYear() == yearMonth.getYear();
//            boolean isToday = d.isEqual(today);
//
//            String description;
//            try {
//                description = PanchangCalculator.getPanchang(d, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getDescription(lang);
//            } catch (Exception e) {
//                description = "";
//            }
//
//            String numScript;
//            try {
//                numScript = PanchangCalculator.getPanchang(d, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getNumberScript();
//            } catch (Exception e) {
//                numScript = "";
//            }
//
//            DayModel model = new DayModel(
//                    d,
//                    String.valueOf(d.getDayOfMonth()),
//                    numScript, // fill with your Gujarati-digit lookup if you use one, else leave blank
//                    description,
//                    isCurrentMonth,
//                    isToday);
//                    days.add(model);
//        }
//        return days;
//    }

    public static List<DayModel> generateCalendarDays(YearMonth yearMonth, Language lang) {
        List<DayModel> days = new ArrayList<>();
        LocalDate today = LocalDate.now();

        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate lastOfMonth = yearMonth.atEndOfMonth();

        // Sunday-first grid (matches your screenshot header: SUN MON TUE ... SAT)
        int firstDayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() % 7; // Sun=0 ... Sat=6
        LocalDate gridStart = firstOfMonth.minusDays(firstDayOfWeekValue);

        int trailingDaysNeeded = (7 - (lastOfMonth.getDayOfWeek().getValue() % 7) - 1) % 7;
        LocalDate gridEnd = lastOfMonth.plusDays(trailingDaysNeeded);

        // Pad to a full 6 rows (42 cells) so the grid height stays consistent month to month
        long totalCells = java.time.temporal.ChronoUnit.DAYS.between(gridStart, gridEnd) + 1;
        if (totalCells < 42) {
            gridEnd = gridEnd.plusDays(42 - totalCells);
        }

        // Tippani (festival) map for this grid's VS year(s). A month view can straddle two
        // VS years right around the March/April new-year boundary, so load both ends.
        Map<String, FestivalModel> tippaniMap = new HashMap<>();
        try {
            android.content.Context ctx = MyApplication.getInstance().getApplicationContext(); // ADJUST to your real getter
            int vsYearStart = PanchangCalculator.getPanchang(gridStart, LAT, LON, UTC_OFFSET).samvatYear;
            int vsYearEnd = PanchangCalculator.getPanchang(gridEnd, LAT, LON, UTC_OFFSET).samvatYear;

            Map<String, FestivalModel> mapStart = loadTippaniForYear(ctx, vsYearStart);
            if (mapStart != null) tippaniMap.putAll(mapStart);
            if (vsYearEnd != vsYearStart) {
                Map<String, FestivalModel> mapEnd = loadTippaniForYear(ctx, vsYearEnd);
                if (mapEnd != null) tippaniMap.putAll(mapEnd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1)) {
            boolean isCurrentMonth = d.getMonth() == yearMonth.getMonth() && d.getYear() == yearMonth.getYear();
            boolean isToday = d.isEqual(today);

            String description;
            try {
                description = PanchangCalculator.getPanchang(d, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getDescription(lang);
            } catch (Exception e) {
                description = "";
            }

            String numScript;
            try {
                numScript = PanchangCalculator.getPanchang(d, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getNumberScript();
            } catch (Exception e) {
                numScript = "";
            }

            DayModel model = new DayModel(
                    d,
                    String.valueOf(d.getDayOfMonth()),
                    numScript,
                    description,
                    isCurrentMonth,
                    isToday);

            FestivalModel festival = tippaniMap.get(d.toString()); // "yyyy-MM-dd"
            if (festival != null) {
                model.setFestivalTitle(festival.getTitle());
            }

            days.add(model);
        }
        return days;
    }

    public static Map<String, FestivalModel> loadTippaniForYear(Context context, int vikramSamvatYear) {
        Map<String, FestivalModel> festivalMap = new HashMap<>();
        String fileName = "tippani_" + vikramSamvatYear + ".json"; // e.g., "tippani_2083.json"

        try {
            // Check if file exists in assets for the given year, fallback gracefully if needed
            InputStream is = context.getAssets().open(fileName);
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

            Type type = new TypeToken<Map<String, FestivalModel>>() {}.getType();
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
}