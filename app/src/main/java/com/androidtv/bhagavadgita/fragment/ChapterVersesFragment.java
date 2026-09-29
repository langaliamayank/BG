package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridView;

import com.androidtv.bhagavadgita.DetailActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.VersesDetailActivity;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.HeaderViewChapter;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.androidtv.bhagavadgita.model.VersesCache;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.androidtv.bhagavadgita.presenter.ChapterVersesPresenter;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ChapterVersesFragment extends MasterBrowseFragment implements
        View.OnFocusChangeListener {

    private static final String SPINNER_TAG = "LoadingOverlay";
    private SpinnerSupportFragment mSpinnerFragment;

    private ArrayObjectAdapter mRowsAdapter;
    private OnBrowseRowListener mCallback;
    private HeaderViewChapter headerViewChapter;
    private ChapterModel mChapterModel;
    private ChapterVersesPresenter chapterVersesPresenter;
    private Receiver receiver = new Receiver();

    /*For Background*/
    private BackgroundManager backgroundManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable backgroundRunnable;
    private static final int BACKGROUND_UPDATE_DELAY_MS = 300;

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                int mSelectedIndex = intent.getIntExtra("index", 0);
                selectRowAt(getSelectedPosition(), mSelectedIndex);
            }
        }
    }

    private void selectRowAt(int rowIndex, int lastSelectedIndex) {
        getRowsSupportFragment().setSelectedPosition(rowIndex, false, new CustomListRowPresenter.SelectItemViewHolderTask(lastSelectedIndex));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (receiver != null) getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        // Safety Check: Ensure the fragment and its view are still active
        if (getRowsSupportFragment() == null || getRowsSupportFragment().getView() == null) {
            return;
        }

        int pos = getSelectedPosition();
        RowPresenter.ViewHolder rowVh = getRowsSupportFragment().getRowViewHolder(pos);

        if (rowVh instanceof ListRowPresenter.ViewHolder) {
            View border = rowVh.view.findViewById(R.id.row_poster);
            if (border != null) {
                border.animate()
                        .alpha(hasFocus ? 0f : 1f)
                        .setDuration(200)
                        .start();
            }
        }
    }

    private void setupHeaderFocusListener() {
        if (headerViewChapter == null) return;
        View viewToFocusSeason = headerViewChapter.findViewById(R.id.buttonReadChapter);
        viewToFocusSeason.setOnFocusChangeListener(this);
    }

    public interface OnBrowseRowListener {
        void onItemSelected(Object item, long index);
    }

    @Override
    public View onInflateTitleView(LayoutInflater inflater, @org.jspecify.annotations.Nullable ViewGroup parent, @org.jspecify.annotations.Nullable Bundle savedInstanceState) {
        headerViewChapter = new HeaderViewChapter(inflater.getContext());
        return headerViewChapter;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Step 1: Initialize BackgroundManager
        backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());

        // Set a fallback background
        updateBackgroundColorDelayed(SharePreferenceManager.getString("KEY_THEME_COLOR"));

        setupHeaderFocusListener();

        IntentFilter intentFilter = new IntentFilter("LAST_WATCH");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getActivity().registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            }
        }

        if (headerViewChapter != null) {
            headerViewChapter.setOnItemClickListener(new HeaderViewChapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    startActivity(VersesDetailActivity.createIntent(getActivity(), mChapterModel));
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }

        setupUIElements();
        setupRowAdapter();
        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (backgroundRunnable != null) {
            handler.removeCallbacks(backgroundRunnable);
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

            } catch (IllegalArgumentException ignored) {}
        };

        handler.postDelayed(backgroundRunnable, BACKGROUND_UPDATE_DELAY_MS);
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (mCallback != null && row != null) {
                mCallback.onItemSelected(item, row.getHeaderItem().getId());
            }

            if (row instanceof ListRow) {
                ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                        .getAdapter());
                int selectedItemPosition = adapter.indexOf(item);

                if (itemViewHolder != null) itemViewHolder.view.setTag(selectedItemPosition);
            }

            if (item instanceof Object && row != null) {
                Object data = (Object) item;

                // 1. Update the Data
                if (headerViewChapter != null) {
                    HeaderItem header = row.getHeaderItem();
                    if (header instanceof RowHeaderItem) {
                        List<Object> childList = ((RowHeaderItem) header).getList();
                        headerViewChapter.setData((MasterActivity) getActivity(), data, childList, ((RowHeaderItem) header));
                        headerViewChapter.findViewById(R.id.buttonReadChapter).setVisibility(View.VISIBLE);
                        headerViewChapter.findViewById(R.id.imageChapter).setBackgroundResource(R.color.colorWhite10);
                    }
                }
            }
        });

        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (!(item instanceof Object) || !(row instanceof ListRow)) return;

            if (item instanceof VersesModel) {
                VersesModel mVersesModel = (VersesModel) item;
                ArrayList<VersesModel> mVersesList = chapterVersesPresenter.getList();
                int mIndex = mVersesList.indexOf(item);
                VersesCache.getInstance().setActiveList(mVersesList);

                startActivity(DetailActivity.createIntent(getActivity(), mVersesModel, mIndex, true));
            }
        });
    }

    private void setupRowAdapter() {
        CustomListRowPresenter selector = new CustomListRowPresenter((MasterActivity) getActivity());
        mRowsAdapter = new ArrayObjectAdapter(selector);
        setAdapter(mRowsAdapter);

        new Thread(() -> {
            try {
                Thread.sleep(Constants.INTERVAL);

                mChapterModel = (ChapterModel) getActivity().getIntent().getSerializableExtra("DATA");
                createVerses();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupUIElements() {
        setHeadersState(HEADERS_DISABLED);
        setHeadersTransitionOnBackEnabled(false);
        if (getActivity() instanceof OnBrowseRowListener) {
            mCallback = (OnBrowseRowListener) getActivity();
        } else {
            throw new ClassCastException(getActivity().toString() + " must implement OnBrowseRowListener");
        }
    }

    private void createVerses() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getVerses();
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {

                chapterVersesPresenter = new ChapterVersesPresenter(getActivity());
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(chapterVersesPresenter);

                Gson gson = new Gson();
                List<VersesModel> versesModelList = gson.fromJson(response,
                        new TypeToken<List<VersesModel>>() {
                        }.getType());

                for (VersesModel versesModel : versesModelList) {
                    if (versesModel.getChapterNumber().equals(mChapterModel.getChapterNumber())) {
                        listRowAdapter.add(versesModel);
                    }
                }

                chapterVersesPresenter.setList(getListFromAdapter(listRowAdapter));
                RowHeaderItem cardPresenterHeader = new RowHeaderItem(0,
                        "Chapter " + mChapterModel.getChapterNumber(), Collections.singletonList(versesModelList));
                cardPresenterHeader.setDescription(mChapterModel.getVersesCount() + " Verses");
                mRowsAdapter.add(new ListRow(cardPresenterHeader, listRowAdapter));

                if (getView() != null) {
                    getView().post(() -> {
                        if (isAdded()) {
                            startEntranceTransition();
                        }
                    });
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

    public ArrayList<VersesModel> getListFromAdapter(ArrayObjectAdapter adapter) {
        ArrayList<VersesModel> versesList = new ArrayList<>();

        for (int i = 0; i < adapter.size(); i++) {
            VersesModel item = (VersesModel) adapter.get(i);
            versesList.add(item);
        }

        return versesList;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBackgroundColorDelayed(SharePreferenceManager.getString("KEY_THEME_COLOR"));
    }

    @Override
    public void onPause() {
        super.onPause();
        updateBackgroundColorDelayed(SharePreferenceManager.getString("KEY_THEME_COLOR"));
    }
}



