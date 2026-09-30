package com.androidtv.bhagavadgita.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.androidtv.bhagavadgita.presenter.FestivalPresenter;
import com.androidtv.bhagavadgita.presenter.MyVerticalGridPresenter;
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

public class CalendarUtsavFragment extends VerticalGridSupportFragment implements OnBackPressedListener{

    private static final String ARG_SELECTED_DATE = "arg_selected_date";
    private LocalDate selectedDate;

    public static CalendarUtsavFragment newInstance(LocalDate date) {
        CalendarUtsavFragment fragment = new CalendarUtsavFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_DATE, date.toString()); // ISO-8601, e.g. "2026-08-30"
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CalendarActivity) getActivity()).setOnBackPressedListener(this);

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startEntranceTransition();
            }
        }, 500);

        if (getArguments() != null && getArguments().containsKey(ARG_SELECTED_DATE)) {
            selectedDate = LocalDate.parse(getArguments().getString(ARG_SELECTED_DATE));
        } else {
            selectedDate = LocalDate.now();
        }

        MyVerticalGridPresenter myVerticalGridPresenter = new MyVerticalGridPresenter();
        setGridPresenter(myVerticalGridPresenter);

        getTippaniList(selectedDate);
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

    private void getTippaniList(LocalDate selectedDate) {
        int vikramSamvatYear = PanchangCalculator
                .getPanchang(selectedDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET)
                .getVSYear(); // adjust to your actual method for raw VS year int

        Map<String, FestivalModel> activeFestivalMap = loadTippaniForYear(requireActivity(), vikramSamvatYear);

        FestivalPresenter festivalPresenter = new FestivalPresenter((MasterActivity) getActivity());
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(festivalPresenter);

        if (activeFestivalMap != null) {
            YearMonth selectedYearMonth = YearMonth.from(selectedDate);

            List<Map.Entry<String, FestivalModel>> filteredList = new ArrayList<>();

            for (Map.Entry<String, FestivalModel> entry : activeFestivalMap.entrySet()) {
                String dateKey = entry.getKey(); // "yyyy-MM-dd"
                if (dateKey != null) {
                    try {
                        LocalDate entryDate = LocalDate.parse(dateKey);
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

            for (Map.Entry<String, FestivalModel> entry : filteredList) {
                listRowAdapter.add(entry.getValue());
                LogTag.e("TippaniDebug Added: " + entry.getValue().getTitle() + " (" + entry.getKey() + ")");
            }
        }

        setAdapter(listRowAdapter);
    }

    @Override
    public void doBack() {
        try {
            ((CalendarActivity) getActivity()).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
