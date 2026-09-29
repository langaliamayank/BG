package com.androidtv.bhagavadgita.fragment;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.TitleViewAdapter;
import androidx.leanback.widget.VerticalGridView;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.MusicDetailActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.HeaderView;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.model.MediaCard;
import com.androidtv.bhagavadgita.model.music.albums.AlbumsModel;
import com.androidtv.bhagavadgita.model.music.albums.AlbumsResultsModel;
import com.androidtv.bhagavadgita.model.music.artists.ArtistsModel;
import com.androidtv.bhagavadgita.model.music.artists.ArtistsResultModel;
import com.androidtv.bhagavadgita.model.music.playlists.PlaylistsModel;
import com.androidtv.bhagavadgita.model.music.playlists.PlaylistsResultModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsAllModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.androidtv.bhagavadgita.playback.PlaybackActivity;
import com.androidtv.bhagavadgita.playback.PlaybackVideoFragment;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.androidtv.bhagavadgita.presenter.NowPlayingPresenter;
import com.androidtv.bhagavadgita.presenter.SongsPresenter;
import com.google.gson.Gson;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class MusicFragment extends MasterBrowseFragment {

    public static ArrayObjectAdapter mRowsAdapter;
    private HeaderView headerView;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private Boolean lastHeaderShowState = null;
    private int mLastSelectedRowIndex = 0;
    private boolean mLastShouldShowDetailHeader = false;
    private static final long ROW_FADE_OUT_MS = 160L;
    private static final long ROW_FADE_IN_MS = 180L;

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

        setupRowAdapter();
        setupEventListeners();
    }

    @Override
    public View onInflateTitleView(@NonNull LayoutInflater inflater, @org.jspecify.annotations.Nullable ViewGroup parent, @org.jspecify.annotations.Nullable Bundle savedInstanceState) {
        headerView = new HeaderView(inflater.getContext(), false, getActivity().getWindowManager());
        return headerView;
    }

    private final TitleViewAdapter mFallbackTitleAdapter = new TitleViewAdapter() {
        @org.jspecify.annotations.Nullable
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

    private void setupRowAdapter() {
        CustomListRowPresenter selector = new CustomListRowPresenter((MasterActivity) requireActivity());
        mRowsAdapter = new ArrayObjectAdapter(selector);
        setAdapter(mRowsAdapter);

        new Thread(() -> {
            try {
                Thread.sleep(Constants.INTERVAL);

                createSongs();
                createAlbums();
                createArtists();
                createPlaylists();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (row instanceof ListRow) {
                ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row).getAdapter());
                int selectedItemPosition = adapter.indexOf(item);
                if (itemViewHolder != null) itemViewHolder.view.setTag(selectedItemPosition);
            }

            int currentRowIndex = mRowsAdapter.indexOf(row);
            HeaderItem header = row.getHeaderItem();
            String desc = header != null && header.getDescription() != null
                    ? header.getDescription().toString().trim()
                    : "";

            boolean shouldShowDetailHeader = false;

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

        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (item instanceof SongsResultModel) {
                SongsResultModel song = (SongsResultModel) item;

                ArrayObjectAdapter adapter = (ArrayObjectAdapter) ((ListRow) row).getAdapter();
                ArrayList<SongsResultModel> songList = new ArrayList<>(adapter.unmodifiableList());
                int itemIndex = adapter.indexOf(item);

                PlayerManager.getInstance(requireActivity()).setPlayData(song);
                PlayerManager.getInstance(requireActivity()).setPlaylist(songList);
                PlayerManager.getInstance(requireActivity()).setCurrentIndex(itemIndex);

                Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                startActivity(intent);
            }

            if (item instanceof ArtistsResultModel) {
                ArtistsResultModel results = (ArtistsResultModel) item;
                startActivity(MusicDetailActivity.createIntent(getActivity(), results));
            }

            if (item instanceof AlbumsResultsModel) {
                AlbumsResultsModel albumsResultsModel = (AlbumsResultsModel) item;
                startActivity(MusicDetailActivity.createIntent(getActivity(), albumsResultsModel));
            }

            if (item instanceof PlaylistsResultModel) {
                PlaylistsResultModel results = (PlaylistsResultModel) item;
                startActivity(MusicDetailActivity.createIntent(getActivity(), results));
            }
        });
    }

    private void updateHeaderVisibility(boolean show) {
        if (lastHeaderShowState != null && lastHeaderShowState == show) return;
        lastHeaderShowState = show;

        Activity activity = getActivity();
        View contentImage = null;
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            contentImage = activity.findViewById(R.id.content_image);
        }

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

    public void createSongs(){
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getPopularSongAPI("Shrinathji", 1,50);
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Reader reader = new StringReader(response);
                SongsModel songsModel = gson.fromJson(reader, SongsModel.class);

                SongsPresenter songsPresenter = new SongsPresenter(getActivity(), 1);
                final ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);

                if (!songsModel.getData().getResults().isEmpty()) {
                    for (SongsResultModel songsResultModel : songsModel.getData().getResults()) {
                        arrayObjectAdapter.add(songsResultModel);
                    }
                }

                HeaderItem cardPresenterHeader = new HeaderItem(0, "Shrinathji");
                cardPresenterHeader.setDescription("Popular Songs");
                mRowsAdapter.add(new ListRow(cardPresenterHeader, arrayObjectAdapter));

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
    private void createAlbums() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getPopularAlbumsAPI("Shrinathji", 0, 50);
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Reader reader = new StringReader(response);
                AlbumsModel albumsModel = gson.fromJson(reader, AlbumsModel.class);

                SongsPresenter songsPresenter = new SongsPresenter(getActivity(), 1);
                final ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);

                if (!albumsModel.getData().getResults().isEmpty()) {
                    for (AlbumsResultsModel albumsResultsModel : albumsModel.getData().getResults()) {
                        arrayObjectAdapter.add(albumsResultsModel);
                    }
                }

                HeaderItem cardPresenterHeader = new HeaderItem(0, "Shrinathji");
                cardPresenterHeader.setDescription("Popular Albums");
                mRowsAdapter.add(new ListRow(cardPresenterHeader, arrayObjectAdapter));
            }

            @Override
            public void onFailure(String error, int responseCode) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }
    private void createArtists() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getPopularArtistAPI(" ", 0, 50);
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Reader reader = new StringReader(response);
                ArtistsModel artistsModel = gson.fromJson(reader, ArtistsModel.class);

                SongsPresenter songsPresenter = new SongsPresenter(getActivity(), 1);
                final ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);

                if (!artistsModel.getData().getResults().isEmpty()) {
                    for (ArtistsResultModel results : artistsModel.getData().getResults()) {
                        arrayObjectAdapter.add(results);
                    }
                }

                HeaderItem cardPresenterHeader = new HeaderItem(0, "Popular Artists");
                cardPresenterHeader.setDescription("");
                mRowsAdapter.add(new ListRow(cardPresenterHeader, arrayObjectAdapter));
            }

            @Override
            public void onFailure(String error, int responseCode) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }
    private void createPlaylists() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getPopularPlaylistAPI("Krishna", 0, 50);
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Reader reader = new StringReader(response);
                PlaylistsModel playlistsModel = gson.fromJson(reader, PlaylistsModel.class);

                SongsPresenter songsPresenter = new SongsPresenter(getActivity(), 1);
                final ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);

                if (!playlistsModel.getData().getResults().isEmpty()) {
                    for (PlaylistsResultModel results : playlistsModel.getData().getResults()) {
                        arrayObjectAdapter.add(results);
                    }
                }

                HeaderItem cardPresenterHeader = new HeaderItem(0, "Shrinathji");
                cardPresenterHeader.setDescription("Popular Playlists");
                mRowsAdapter.add(new ListRow(cardPresenterHeader, arrayObjectAdapter));
            }

            @Override
            public void onFailure(String error, int responseCode) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    @UnstableApi
    @Override
    public void onResume() {
        super.onResume();
        createNowPlaying();
    }

    @UnstableApi
    private void createNowPlaying() {
        if (getActivity() == null) return;

        // Check karein ki active transport control glue aur player playing state me hai ya nahi
        if (PlaybackVideoFragment.transportControlGlue != null &&
                PlaybackVideoFragment.transportControlGlue.isPlaying()) {

            SongsResultModel currentSong = PlaybackVideoFragment.transportControlGlue.getCurrentMovie();
            if (currentSong == null) return;

            NowPlayingPresenter nowPlayingPresenter = new NowPlayingPresenter(getActivity());
            ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(nowPlayingPresenter);

            String url = (currentSong.getDownloadUrl() != null && !currentSong.getDownloadUrl().isEmpty())
                    ? currentSong.getDownloadUrl().get(0).getUrl() : "";

            String artists = "";
            if (currentSong.getArtists() != null && currentSong.getArtists().getAll() != null) {
                artists = currentSong.getArtists().getAll().stream()
                        .map(SongsAllModel::getName)
                        .collect(Collectors.joining(", "));
            }

            String imageUrl = "";
            if (currentSong.getImage() != null && !currentSong.getImage().isEmpty()) {
                imageUrl = currentSong.getImage().get(currentSong.getImage().size() - 1).getUrl();
            }

            MediaCard card = new MediaCard(currentSong.getId(),
                    artists, url, currentSong.getName(), "", Uri.parse(imageUrl));
            arrayObjectAdapter.add(card);

            // MusicFragment ke rows adapter me row add ya replace karein
            if (mRowsAdapter != null) {
                HeaderItem updateHeader = new HeaderItem(0, "Now Playing");

                boolean rowExists = false;
                if (mRowsAdapter.size() > 0 && mRowsAdapter.get(0) instanceof ListRow) {
                    ListRow listRow = (ListRow) mRowsAdapter.get(0);
                    if (listRow.getHeaderItem() != null &&
                            "Now Playing".equalsIgnoreCase(listRow.getHeaderItem().getName())) {
                        mRowsAdapter.replace(0, new ListRow(updateHeader, arrayObjectAdapter));
                        rowExists = true;
                    }
                }

                if (!rowExists) {
                    mRowsAdapter.add(0, new ListRow(updateHeader, arrayObjectAdapter));
                }
            }
        }
    }
}


