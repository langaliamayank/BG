package com.androidtv.bhagavadgita.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.MusicDetailActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.androidtv.bhagavadgita.model.music.album.AlbumModel;
import com.androidtv.bhagavadgita.model.music.albums.AlbumsResultsModel;
import com.androidtv.bhagavadgita.model.music.artist.ArtistModel;
import com.androidtv.bhagavadgita.model.music.artists.ArtistsResultModel;
import com.androidtv.bhagavadgita.model.music.playlist.PlaylistModel;
import com.androidtv.bhagavadgita.model.music.playlists.PlaylistsResultModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.androidtv.bhagavadgita.pagination.PaginationAdapter;
import com.androidtv.bhagavadgita.pagination.PostAdapter;
import com.androidtv.bhagavadgita.presenter.FestivalPresenter;
import com.androidtv.bhagavadgita.presenter.MyVerticalGridPresenter;
import com.androidtv.bhagavadgita.presenter.SongsListPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class CalendarUtsavFragment extends VerticalGridSupportFragment implements OnBackPressedListener{
    private static final int NUM_COLUMNS = 1;
    private static final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_SMALL;
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

        MyVerticalGridPresenter myVerticalGridPresenter = new MyVerticalGridPresenter(ZOOM_FACTOR, false);
        myVerticalGridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(myVerticalGridPresenter);

//        getView().setLayoutParams(new ViewGroup.MarginLayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT)); // or a fixed height

//        postAdapter = new PostAdapter(getActivity(), new SongsListPresenter(getActivity(), 1), "tag");
//        setAdapter(postAdapter);

//        object = getActivity().getIntent().getSerializableExtra("DATA");
//        if (object instanceof ArtistsResultModel) {
////            artistsResultModel = (ArtistsResultModel) object;
////            getArtistsDataList(artistsResultModel.getId(), currentPage);
//        }

        getTippaniList(selectedDate);

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object
                    item, RowPresenter.ViewHolder rowViewHolder, Row row) {
//                if (item instanceof SongsResultModel) {
//                    ArrayList<SongsResultModel> posts = (ArrayList<SongsResultModel>) postAdapter.getAllItems();
//                    int itemIndex = postAdapter.indexOf(item);
//                    int minimumIndex = posts.size() - NUM_COLUMNS;
//
//                    if (currentPage != 0) {
//                        currentPage += 1;
//                    }
//
////                    if (itemIndex >= minimumIndex && postAdapter.shouldLoadNextPage()) {
////                        getArtistsDataList(artistsResultModel.getId(), currentPage);
////                    }
//                }
            }
        });
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

//    private void getTippaniList() {
//        int vikramSamvatYear = PanchangCalculator
//                .getPanchang(selectedDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET)
//                .getVSYear(); // adjust to your actual method for raw VS year int
//
//        Map<String, FestivalModel> activeFestivalMap = loadTippaniForYear(requireActivity(), vikramSamvatYear);
//
//        FestivalPresenter festivalPresenter = new FestivalPresenter((MasterActivity) getActivity());
//        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(festivalPresenter);
//
//        if (activeFestivalMap != null) {
//            String selectedDateKey = selectedDate.toString(); // LocalDate.toString() = "yyyy-MM-dd", matches your JSON keys directly
//
//            FestivalModel festivalModel = activeFestivalMap.get(selectedDateKey);
//
//            if (festivalModel != null) {
//                listRowAdapter.add(festivalModel);
//                LogTag.e("TippaniDebug Selected date match: " + festivalModel.getTitle() + " (" + selectedDateKey + ")");
//            } else {
//                LogTag.e("TippaniDebug No festival found for: " + selectedDateKey);
//            }
//        }
//
//        setAdapter(listRowAdapter);
//    }

//    private void getTippaniList() {
//        Map<String, FestivalModel> activeFestivalMap = loadTippaniForYear(requireActivity(), 2083);
//
//        FestivalPresenter festivalPresenter = new FestivalPresenter((MasterActivity) getActivity());
//        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(festivalPresenter);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//
//        if (activeFestivalMap != null) {
//            try {
//                // Normalize today's date to 00:00:00 so it matches the parsed JSON date keys perfectly
//                String todayStr = selectedDate.toString();
//                Date todayDate = sdf.parse(todayStr);
//
//                List<Map.Entry<String, FestivalModel>> filteredList = new ArrayList<>();
//
//                for (Map.Entry<String, FestivalModel> entry : activeFestivalMap.entrySet()) {
//                    String dateKey = entry.getKey(); // e.g., "2026-08-28"
//                    FestivalModel festivalModel = entry.getValue();
//
//                    if (dateKey != null) {
//                        Date festivalDate = sdf.parse(dateKey);
//
//                        // Compare calendar days safely (includes today and future dates)
//                        if (festivalDate != null && !festivalDate.before(todayDate)) {
//                            filteredList.add(entry);
//                            LogTag.e("TippaniDebug Added to Adapter: " + festivalModel.getTitle() + " (" + dateKey + ")");
//                        }
//                    }
//                }
//
//                // Sort filtered items chronologically by date
//                Collections.sort(filteredList, new Comparator<Map.Entry<String, FestivalModel>>() {
//                    @Override
//                    public int compare(Map.Entry<String, FestivalModel> o1, Map.Entry<String, FestivalModel> o2) {
//                        try {
//                            Date d1 = sdf.parse(o1.getKey());
//                            Date d2 = sdf.parse(o2.getKey());
//                            if (d1 != null && d2 != null) {
//                                return d1.compareTo(d2);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        return 0;
//                    }
//                });
//
//                // Add sorted items to the Leanback adapter
//                for (Map.Entry<String, FestivalModel> sortedEntry : filteredList) {
//                    listRowAdapter.add(sortedEntry.getValue());
//                }
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        setAdapter(listRowAdapter);
//
////        pendingRows.put(index, new ListRow(cardPresenterHeader, listRowAdapter));
////
////        checkAndCommitRows();
//    }

//    @Override
//    public void doBack() {
//        try {
//            MasterActivity.selectedPosition = 0;
//            ((MusicDetailActivity) getActivity()).finish();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (postAdapter != null) {
//            postAdapter.notifyArrayItemRangeChanged(0, postAdapter.size());
//        }
//    }
}
