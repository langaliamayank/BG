package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.TitleViewAdapter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.CommanActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.HeaderView;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.androidtv.bhagavadgita.model.VersesCache;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.androidtv.bhagavadgita.presenter.CardPresenter;
import com.androidtv.bhagavadgita.presenter.ContinueWatchingPresenter;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.androidtv.bhagavadgita.presenter.VersesOfTheDayPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class GitaFragment extends MasterBrowseFragment {

    private ArrayObjectAdapter mRowsAdapter;
    private VersesOfTheDayPresenter versesOfTheDayPresenter;
    private ContinueWatchingPresenter continueWatchingPresenter;
    private OnBrowseRowListener mCallback;
    private HeaderView headerView;

    private CustomListRowPresenter selector;

    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private boolean isInitialLoadComplete = false;
    private Boolean lastHeaderShowState = null;

    private int mLastSelectedRowIndex = 0;
    private boolean mLastShouldShowDetailHeader = false;

    private static final long ROW_FADE_OUT_MS = 160L;
    private static final long ROW_FADE_IN_MS = 180L;


    public interface OnBrowseRowListener {
        void onItemSelected(Object item, long index);
    }

    @Override
    public View onInflateTitleView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        headerView = new HeaderView(inflater.getContext(), false, getActivity().getWindowManager());
        return headerView;
    }

    private final TitleViewAdapter mFallbackTitleAdapter = new TitleViewAdapter() {
        @Nullable
        @Override
        public View getSearchAffordanceView() {
            return null;
        }

        @Override
        public void updateComponentsVisibility(int flags) {}
    };

    @Nullable
    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        if (headerView != null && headerView.getTitleViewAdapter() != null) {
            return headerView.getTitleViewAdapter();
        }
        return mFallbackTitleAdapter;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.post(() -> {
            if (getRowsSupportFragment() != null) {
                VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
                if (vgv != null) {
                    vgv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                        @Override
                        public void onChildViewAttachedToWindow(@NonNull View child) {
                            int adapterPos = vgv.getChildAdapterPosition(child);
                            if (adapterPos == RecyclerView.NO_POSITION) return;
                            boolean hide = mLastShouldShowDetailHeader && adapterPos < mLastSelectedRowIndex;
                            child.setAlpha(hide ? 0f : 1f);
                        }

                        @Override
                        public void onChildViewDetachedFromWindow(@NonNull View child) {
                            // no-op
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }

        setupCallback();
        setupRowAdapter();
        setupEventListeners();
    }

    private void setupCallback() {
        if (getActivity() instanceof OnBrowseRowListener) {
            mCallback = (OnBrowseRowListener) getActivity();
        } else {
            throw new ClassCastException(getActivity().toString() + " must implement OnBrowseRowListener");
        }
    }

    /**
     * Shows/hides the detail header (title/description text + hero poster image).
     *
     * IMPORTANT: when transitioning from "hidden" to "shown" the fade-IN is
     * deliberately delayed by ROW_FADE_OUT_MS. The row list fades OUT the
     * rows above the newly selected row over that same duration (see
     * setupEventListeners / restoreLastRowState / restoreRowStateAfterResume).
     * Without the delay, the outgoing row card and the incoming hero image
     * animate concurrently and are both partially visible at the same time,
     * producing a "double exposure" overlap on screen.
     */
    private void updateHeaderVisibility(boolean show) {
        if (lastHeaderShowState != null && lastHeaderShowState == show) return;
        lastHeaderShowState = show;

        Activity activity = getActivity();
        View contentImage = null;
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            contentImage = activity.findViewById(R.id.content_image);
        }

        // 1. Text Details Container
        if (headerView != null) {
            headerView.animate().cancel();
            if (show) {
                headerView.setVisibility(View.VISIBLE);
                headerView.setAlpha(0f);

                if (headerView.content_details != null) {
                    headerView.content_details.setVisibility(View.VISIBLE);
                }

                headerView.animate()
                        .alpha(1f)
                        .setStartDelay(ROW_FADE_OUT_MS)
                        .setDuration(220)
                        .setInterpolator(mInterpolator)
                        .start();
            } else {
                if (headerView.content_details != null) {
                    headerView.content_details.setVisibility(View.GONE);
                }

                headerView.animate()
                        .alpha(0f)
                        .setDuration(180)
                        .setInterpolator(mInterpolator)
                        .withEndAction(() -> {
                            if (lastHeaderShowState != null && !lastHeaderShowState && headerView != null) {
                                headerView.setVisibility(View.GONE);
                            }
                        })
                        .start();
            }
        }

        // 2. Poster / Hero Artwork Cutout
        if (contentImage != null) {
            contentImage.animate().cancel();
            if (show) {
                contentImage.setVisibility(View.VISIBLE);
                contentImage.setAlpha(0f);
                contentImage.setScaleX(0.95f);
                contentImage.setScaleY(0.95f);
                contentImage.animate()
                        .alpha(1f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setStartDelay(ROW_FADE_OUT_MS)
                        .setDuration(250)
                        .setInterpolator(mInterpolator)
                        .start();
            } else {
                contentImage.animate()
                        .alpha(0f)
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(180)
                        .setInterpolator(mInterpolator)
                        .start();
            }
        }
    }

    /**
     * Fades the rows above {@code activeIndex} out (when a detail header is
     * about to show) or back in, using ROW_FADE_OUT_MS so it stays in
     * lockstep with the header fade-in delay above.
     */
    private void fadeRowsForSelection(VerticalGridView vgv, int activeIndex, boolean shouldShowDetailHeader) {
        int totalChildCount = vgv.getChildCount();
        for (int i = 0; i < totalChildCount; i++) {
            View child = vgv.getChildAt(i);
            int adapterPos = vgv.getChildAdapterPosition(child);

            if (adapterPos != RecyclerView.NO_POSITION) {
                child.animate().cancel();
                child.setVisibility(View.VISIBLE);

                if (shouldShowDetailHeader && adapterPos < activeIndex) {
                    child.animate().alpha(0f).setDuration(ROW_FADE_OUT_MS).start();
                } else {
                    child.animate().alpha(1f).setDuration(ROW_FADE_IN_MS).start();
                }
            }
        }
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (mCallback != null && row != null) {
                mCallback.onItemSelected(item, row.getHeaderItem().getId());
            }

            if (row instanceof ListRow) {
                ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row).getAdapter());
                int selectedItemPosition = adapter.indexOf(item);
                if (itemViewHolder != null) itemViewHolder.view.setTag(selectedItemPosition);
            }

            if (!isInitialLoadComplete || item == null || row == null) return;

            int currentRowIndex = mRowsAdapter.indexOf(row);
            HeaderItem header = row.getHeaderItem();
            String desc = header != null && header.getDescription() != null
                    ? header.getDescription().toString().trim()
                    : "";

            boolean isTopBannerRow = "of the day".equalsIgnoreCase(desc);
            boolean isContinueWatching = "Continue Watching".equalsIgnoreCase(desc);
            boolean shouldShowDetailHeader = !isTopBannerRow && !isContinueWatching;

            mLastSelectedRowIndex = currentRowIndex;
            mLastShouldShowDetailHeader = shouldShowDetailHeader;

            if (getRowsSupportFragment() != null) {
                VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
                if (vgv != null) {
                    int targetOffset = shouldShowDetailHeader
                            ? getResources().getDimensionPixelSize(R.dimen.content_image_height)
                            : getResources().getDimensionPixelSize(R.dimen.low_padding);
                    vgv.setWindowAlignmentOffset(targetOffset);

                    fadeRowsForSelection(vgv, currentRowIndex, shouldShowDetailHeader);
                }
            }

            if (headerView != null && header instanceof RowHeaderItem) {
                List<Object> childList = ((RowHeaderItem) header).getList();
                headerView.setData(item, childList, ((RowHeaderItem) header));
            }

            updateHeaderVisibility(shouldShowDetailHeader);
        });
    }

    private void setupRowAdapter() {
        selector = new CustomListRowPresenter(
                (MasterActivity) requireActivity(),
                new CustomListRowPresenter.OnRowActionListener() {
                    @Override
                    public void onRemoveRow(int position, ListRow row) {
                        if (getActivity() == null) return;

                        SharePreferenceManager.save("KEY_LAST_VERSE_LIST", null);
                        SharePreferenceManager.save("KEY_LAST_VERSE_POSITION", -1);

                        int targetIndex = (position >= 0) ? position : mRowsAdapter.indexOf(row);
                        if (targetIndex >= 0 && targetIndex < mRowsAdapter.size()) {
                            mRowsAdapter.removeItems(targetIndex, 1);
                        }

                        Toast.makeText(getActivity(), "Removed from Continue Watching", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        mRowsAdapter = new ArrayObjectAdapter(selector);
        selector.setRowsAdapter(mRowsAdapter);

        buildContinueWatchingRowSync();
        fetchChaptersAndThenVerses();
    }

    private void buildContinueWatchingRowSync() {
        Type type = new TypeToken<ArrayList<VersesModel>>() {
        }.getType();
        ArrayList<VersesModel> lastList = SharePreferenceManager.getList("KEY_LAST_VERSE_LIST", type);
        int lastPosition = SharePreferenceManager.getInt("KEY_LAST_VERSE_POSITION");

        if (lastList != null && !lastList.isEmpty() && lastPosition >= 0 && lastPosition < lastList.size()) {
            if (continueWatchingPresenter == null) {
                continueWatchingPresenter = new ContinueWatchingPresenter((MasterActivity) getActivity());
            }
            continueWatchingPresenter.setList(lastList);

            ArrayObjectAdapter innerAdapter = new ArrayObjectAdapter(continueWatchingPresenter);
            innerAdapter.add(lastList.get(lastPosition));

            RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Bhagavad Gita", Collections.singletonList(lastList));
            cardPresenterHeader.setDescription("Continue Watching");

            mRowsAdapter.add(new ListRow(cardPresenterHeader, innerAdapter));

            if (getView() != null) {
                getView().post(() -> {
                    if (isAdded()) {
                        startEntranceTransition();
                    }
                });
            }
        }
    }

    private void fetchChaptersAndThenVerses() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> chaptersCall = apiInterface.getChapters();

        APIClient.callAPI((MasterActivity) getActivity(), chaptersCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    List<ChapterModel> chapterList = gson.fromJson(response, new TypeToken<List<ChapterModel>>() {
                    }.getType());

                    CardPresenter cardPresenter = new CardPresenter((MasterActivity) getActivity());
                    ArrayObjectAdapter chapterRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (ChapterModel chapterModel : chapterList) {
                        chapterRowAdapter.add(chapterModel);
                    }

                    RowHeaderItem chaptersHeader = new RowHeaderItem(1, "Bhagavad Gita", Collections.singletonList(chapterList));
                    chaptersHeader.setDescription("Chapters");

                    mRowsAdapter.add(new ListRow(chaptersHeader, chapterRowAdapter));

                    if (getView() != null) {
                        getView().post(() -> {
                            if (isAdded()) {
                                startEntranceTransition();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    fetchVersesOfTheDayAndAttach();
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
                fetchVersesOfTheDayAndAttach();
            }

            @Override
            public void onError(String error) {
                fetchVersesOfTheDayAndAttach();
            }
        });
    }

    private void fetchVersesOfTheDayAndAttach() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> versesCall = apiInterface.getVerses();

        APIClient.callAPI((MasterActivity) getActivity(), versesCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String versesResponse) {
                try {
                    ArrayList<VersesModel> versesModelList = new Gson().fromJson(
                            versesResponse, new TypeToken<List<VersesModel>>() {
                            }.getType());

                    versesOfTheDayPresenter = new VersesOfTheDayPresenter((MasterActivity) getActivity());
                    ArrayObjectAdapter bannerRowAdapter = new ArrayObjectAdapter(versesOfTheDayPresenter);

                    VersesModel verseOfTheDay = getVerseOfTheDay(versesModelList);
                    if (verseOfTheDay != null) {
                        bannerRowAdapter.add(verseOfTheDay);
                        VersesCache.getInstance().setVerses(new ArrayList<>(List.of(verseOfTheDay)));
                    }

                    RowHeaderItem bannerHeader = new RowHeaderItem(2, "Verses", Collections.singletonList(versesModelList));
                    bannerHeader.setDescription("of the day");

                    mRowsAdapter.add(new ListRow(bannerHeader, bannerRowAdapter));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finalizeAdapterAttachment();
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
                finalizeAdapterAttachment();
            }

            @Override
            public void onError(String error) {
                finalizeAdapterAttachment();
            }
        });
    }

    private void finalizeAdapterAttachment() {
        setAdapter(mRowsAdapter);

        if (getRowsSupportFragment() != null) {
            getRowsSupportFragment().setSelectedPosition(0, false);

            VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
            if (vgv != null) {
                vgv.post(() -> {
                    getRowsSupportFragment().setSelectedPosition(0, false);

                    boolean row0IsContinueWatching = false;
                    ListRow firstRow = null;

                    if (mRowsAdapter.size() > 0 && mRowsAdapter.get(0) instanceof ListRow) {
                        firstRow = (ListRow) mRowsAdapter.get(0);
                        if (firstRow.getHeaderItem() != null) {
                            row0IsContinueWatching = "Continue Watching".equalsIgnoreCase(
                                    String.valueOf(firstRow.getHeaderItem().getDescription()));
                        }
                    }

                    mLastSelectedRowIndex = 0;
                    mLastShouldShowDetailHeader = !row0IsContinueWatching;

                    int targetOffset = mLastShouldShowDetailHeader
                            ? getResources().getDimensionPixelSize(R.dimen.content_image_height)
                            : getResources().getDimensionPixelSize(R.dimen.low_padding);
                    vgv.setWindowAlignmentOffset(targetOffset);

                    // Populate headerView with row 0's first item
                    if (mLastShouldShowDetailHeader && headerView != null && firstRow != null) {
                        HeaderItem header = firstRow.getHeaderItem();
                        ArrayObjectAdapter innerAdapter = (ArrayObjectAdapter) firstRow.getAdapter();
                        Object firstItem = (innerAdapter != null && innerAdapter.size() > 0) ? innerAdapter.get(0) : null;

                        if (header instanceof RowHeaderItem && firstItem != null) {
                            List<Object> childList = ((RowHeaderItem) header).getList();
                            headerView.setData(firstItem, childList, (RowHeaderItem) header);
                        }
                    }

                    // Unlock flag and clear cached state before updating visibility
                    isInitialLoadComplete = true;
                    lastHeaderShowState = null;
                    updateHeaderVisibility(mLastShouldShowDetailHeader);

                    RecyclerView.ViewHolder vh = vgv.findViewHolderForAdapterPosition(0);
                    if (vh != null) {
                        vh.itemView.requestFocus();
                    }
                });
            }
        }
    }

    public VersesModel getVerseOfTheDay(List<VersesModel> verses) {
        if (verses == null || verses.isEmpty()) return null;

        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String savedDate = SharePreferenceManager.getString("KEY_DATE");
        int savedVerseId = SharePreferenceManager.getInt("KEY_VERSE_ID");

        if (today.equals(savedDate) && savedVerseId != -1) {
            for (VersesModel v : verses) {
                if (v.getId() == savedVerseId) {
                    return v;
                }
            }
        }

        Random random = new Random();
        VersesModel newVerse = verses.get(random.nextInt(verses.size()));

        SharePreferenceManager.save("KEY_DATE", today);
        SharePreferenceManager.save("KEY_VERSE_ID", newVerse.getId());
        return newVerse;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isInitialLoadComplete && getView() != null) {
            getView().post(() -> {
                boolean newlyAdded = refreshContinueWatchingOnResume();
                restoreRowStateAfterResume(newlyAdded);
            });
        }
    }

    private boolean refreshContinueWatchingOnResume() {
        if (mRowsAdapter == null || getActivity() == null) return false;

        Type type = new TypeToken<ArrayList<VersesModel>>() {
        }.getType();
        ArrayList<VersesModel> lastList = SharePreferenceManager.getList("KEY_LAST_VERSE_LIST", type);
        int lastPosition = SharePreferenceManager.getInt("KEY_LAST_VERSE_POSITION");

        int existingIndex = -1;
        ListRow existingRow = null;

        for (int i = 0; i < mRowsAdapter.size(); i++) {
            Object item = mRowsAdapter.get(i);
            if (item instanceof ListRow) {
                ListRow lr = (ListRow) item;
                if (lr.getHeaderItem() != null &&
                        "Continue Watching".equalsIgnoreCase(String.valueOf(lr.getHeaderItem().getDescription()))) {
                    existingIndex = i;
                    existingRow = lr;
                    break;
                }
            }
        }

        boolean hasValidData = (lastList != null && !lastList.isEmpty()
                && lastPosition >= 0 && lastPosition < lastList.size());

        // 1. Remove if no data
        if (!hasValidData) {
            if (existingRow != null && existingIndex >= 0) {
                mRowsAdapter.removeItems(existingIndex, 1);
            }
            return false;
        }

        VersesModel currentVerse = lastList.get(lastPosition);

        // 2. Row exists: update in place (no jump, no selection change)
        if (existingRow != null) {
            ArrayObjectAdapter innerAdapter = (ArrayObjectAdapter) existingRow.getAdapter();
            if (continueWatchingPresenter != null) {
                continueWatchingPresenter.setList(lastList);
            }

            if (innerAdapter != null) {
                if (innerAdapter.size() > 0) {
                    innerAdapter.replace(0, currentVerse);
                } else {
                    innerAdapter.add(currentVerse);
                }
            }
            return false;
        }

        // 3. Row did NOT exist previously: create and prepend at index 0
        if (continueWatchingPresenter == null) {
            continueWatchingPresenter = new ContinueWatchingPresenter((MasterActivity) getActivity());
        }
        continueWatchingPresenter.setList(lastList);

        ArrayObjectAdapter innerAdapter = new ArrayObjectAdapter(continueWatchingPresenter);
        innerAdapter.add(currentVerse);

        RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Bhagavad Gita", Collections.singletonList(lastList));
        cardPresenterHeader.setDescription("Continue Watching");

        ListRow newRow = new ListRow(cardPresenterHeader, innerAdapter);
        mRowsAdapter.add(0, newRow);
        return true; // Newly added at index 0
    }

    private void restoreRowStateAfterResume(boolean newlyAdded) {
        if (getRowsSupportFragment() == null) return;
        VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
        if (vgv == null) return;

        // CRITICAL: If row 0 was just created, force selection to Row 0 so it doesn't stay scrolled offscreen
        if (newlyAdded) {
            getRowsSupportFragment().setSelectedPosition(0, false);
        }

        vgv.post(() -> {
            int currentPos = getRowsSupportFragment().getSelectedPosition();
            if (currentPos < 0 || currentPos >= mRowsAdapter.size()) {
                currentPos = 0;
            }

            // Check the newly focused row
            boolean shouldShowDetail = false;
            Object selectedObj = mRowsAdapter.get(currentPos);
            if (selectedObj instanceof ListRow) {
                HeaderItem header = ((ListRow) selectedObj).getHeaderItem();
                String desc = header != null && header.getDescription() != null
                        ? header.getDescription().toString().trim()
                        : "";

                boolean isVersesOfTheDay = "of the day".equalsIgnoreCase(desc);
                boolean isContinueWatching = "Continue Watching".equalsIgnoreCase(desc);

                // ONLY Chapters row uses the large header / detail layout
                shouldShowDetail = !isVersesOfTheDay && !isContinueWatching;
            }

            mLastSelectedRowIndex = currentPos;
            mLastShouldShowDetailHeader = shouldShowDetail;

            int targetOffset = shouldShowDetail
                    ? getResources().getDimensionPixelSize(R.dimen.content_image_height)
                    : getResources().getDimensionPixelSize(R.dimen.low_padding);
            vgv.setWindowAlignmentOffset(targetOffset);

            lastHeaderShowState = null;
            updateHeaderVisibility(shouldShowDetail);

            // Clean up alpha of all rows
            final int activeIndex = currentPos;
            final boolean activeDetail = shouldShowDetail;
            int totalChildCount = vgv.getChildCount();
            for (int i = 0; i < totalChildCount; i++) {
                View child = vgv.getChildAt(i);
                int adapterPos = vgv.getChildAdapterPosition(child);

                if (adapterPos != RecyclerView.NO_POSITION) {
                    child.animate().cancel();
                    child.setVisibility(View.VISIBLE);

                    if (activeDetail && adapterPos < activeIndex) {
                        child.setAlpha(0f);
                    } else {
                        child.setAlpha(1f);
                    }
                }
            }
        });
    }
}