package com.androidtv.bhagavadgita.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridView;
import androidx.media3.common.util.UnstableApi;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.PlayerManager;
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
import com.androidtv.bhagavadgita.playback.PlaybackActivity;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.androidtv.bhagavadgita.presenter.MyVerticalGridPresenter;
import com.androidtv.bhagavadgita.presenter.SongsListPresenter;
import com.google.gson.Gson;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class MusicDetailFragment extends VerticalGridSupportFragment {
    private static final int NUM_COLUMNS = 1;
    private Object object;

    private SongsListPresenter songsPresenter;
    private ArrayObjectAdapter arrayObjectAdapter;
    private PostAdapter postAdapter;
    private int PAGE_START = 1;
    private int currentPage = PAGE_START;

    private ArtistsResultModel artistsResultModel;
    private AlbumsResultsModel albumsResultsModel;
    private final ArrayList<SongsResultModel> mMasterSongList = new ArrayList<>();
    private  MyVerticalGridPresenter myVerticalGridPresenter;
    private Receiver receiver = new Receiver();

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                int mSelectedIndex = intent.getIntExtra("index", 0);
                selectRowAt(mSelectedIndex);
            }
        }
    }

    private void selectRowAt(int lastSelectedIndex) {
        setSelectedPosition(lastSelectedIndex);

        if (getAdapter() instanceof ArrayObjectAdapter) {
            ArrayObjectAdapter a = (ArrayObjectAdapter) getAdapter();
            a.notifyArrayItemRangeChanged(0, a.size());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (receiver != null) getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }

        myVerticalGridPresenter = new MyVerticalGridPresenter();
        myVerticalGridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(myVerticalGridPresenter);
        setSelectedPosition(0);

        songsPresenter = new SongsListPresenter(getActivity());
        postAdapter = new PostAdapter(getActivity(), songsPresenter, "tag");

        object = getActivity().getIntent().getSerializableExtra("DATA");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IntentFilter intentFilter = new IntentFilter("LAST_WATCH");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getActivity().registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            }
        }

        if (artistsResultModel != null) {
            setAdapter(postAdapter);
            if (postAdapter.size() == 0) {
                getArtistsDataList(artistsResultModel.getId(), false);
            }
        } else if (albumsResultsModel != null) {
            if (arrayObjectAdapter == null) {
                arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);
                getAlbumDataList(albumsResultsModel);
            } else {
                setAdapter(arrayObjectAdapter);
            }
        } else if (object instanceof PlaylistsResultModel) {
            PlaylistsResultModel playlist = (PlaylistsResultModel) object;
            if (arrayObjectAdapter == null) {
                arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);
                getPlaylistDataList(playlist);
            } else {
                setAdapter(arrayObjectAdapter);
            }
        } else {
            // Initial load check based on Intent data
            if (object instanceof ArtistsResultModel) {
                artistsResultModel = (ArtistsResultModel) object;
                setAdapter(postAdapter);
                getArtistsDataList(artistsResultModel.getId(), false);
            } else if (object instanceof AlbumsResultsModel) {
                albumsResultsModel = (AlbumsResultsModel) object;
                arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);
                setAdapter(arrayObjectAdapter);
                getAlbumDataList(albumsResultsModel);
            } else if (object instanceof PlaylistsResultModel) {
                PlaylistsResultModel playlist = (PlaylistsResultModel) object;
                arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);
                setAdapter(arrayObjectAdapter);
                getPlaylistDataList(playlist);
            }
        }

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {

                if (item instanceof SongsResultModel && getAdapter() == postAdapter) {
                    int itemIndex = postAdapter.indexOf(item);
                    int totalCount = postAdapter.size();
                    int minimumIndex = totalCount - NUM_COLUMNS;

                    if (itemIndex >= minimumIndex && postAdapter.shouldLoadNextPage()) {
                        if (artistsResultModel != null) {
                            currentPage += 1;
                            getArtistsDataList(artistsResultModel.getId(), true);
                        }
                    }
                }
            }
        });

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @UnstableApi
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof SongsResultModel) {
                    SongsResultModel song = (SongsResultModel) item;

                    int itemIndex = -1;
                    ObjectAdapter activeAdapter = getAdapter();

                    if (activeAdapter instanceof ArrayObjectAdapter) {
                        itemIndex = ((ArrayObjectAdapter) activeAdapter).indexOf(item);
                    } else if (activeAdapter instanceof PostAdapter) {
                        itemIndex = ((PostAdapter) activeAdapter).indexOf(item);
                    }

                    if (itemIndex == -1) {
                        for (int i = 0; i < mMasterSongList.size(); i++) {
                            if (mMasterSongList.get(i).getId() != null
                                    && mMasterSongList.get(i).getId().equals(song.getId())) {
                                itemIndex = i;
                                break;
                            }
                        }
                    }

                    PlayerManager.getInstance(requireActivity()).setPlayData(song);
                    PlayerManager.getInstance(requireActivity()).setPlaylist(mMasterSongList);
                    PlayerManager.getInstance(requireActivity()).setCurrentIndex(itemIndex);
                    startActivity(new Intent(getActivity(), PlaybackActivity.class));
                }
            }
        });
    }

    private void getArtistsDataList(String artistsID, boolean loadNext) {
        if (postAdapter.shouldShowLoadingIndicator()) postAdapter.showLoadingIndicator();

        Map<String, String> options = postAdapter.getAdapterOptions();
        final String anchor = options.get(PaginationAdapter.KEY_ANCHOR);
        String nextPage = options.get(PaginationAdapter.KEY_NEXT_PAGE);

        int targetPage = 1;
        if (loadNext && nextPage != null) {
            try {
                targetPage = Integer.parseInt(nextPage);
            } catch (NumberFormatException ignored) {
                targetPage = currentPage;
            }
        }

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.retrieveArtistSongs(artistsID, targetPage, "latest", "desc");
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {
                LogTag.e("getArtistsDataList " + response);

                Gson gson = new Gson();
                Reader reader = new StringReader(response);
                ArtistModel artistModel = gson.fromJson(reader, ArtistModel.class);

                if (postAdapter.size() == 0 && (artistModel.getData() == null || artistModel.getData().getSongs().isEmpty())) {
                    postAdapter.showReloadCard();
                } else if (artistModel.getData() != null) {
                    if (anchor == null) {
                        postAdapter.setAnchor(null);
                    }

                    mMasterSongList.addAll(artistModel.getData().getSongs());
                    postAdapter.setNextPage(currentPage + 1);
                    postAdapter.addAllItems(artistModel.getData().getSongs());
                    postAdapter.removeLoadingIndicator();

                    if (getView() != null) {
                        getView().post(() -> {
                            if (isAdded()) {
                                startEntranceTransition();
                            }
                        });
                    }

                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
                postAdapter.removeLoadingIndicator();
            }

            @Override
            public void onError(String error) {
                postAdapter.removeLoadingIndicator();
            }
        });
    }

    private void getAlbumDataList(AlbumsResultsModel alResults) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.retrieveAlbumById(alResults.getId());
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Reader reader = new StringReader(response);
                AlbumModel albumModel = gson.fromJson(reader, AlbumModel.class);

                arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);
                if (albumModel.getData() != null && !albumModel.getData().getSongs().isEmpty()) {
                    for (SongsResultModel song : albumModel.getData().getSongs()) {
                        arrayObjectAdapter.add(song);
                    }
                }

                mMasterSongList.clear();
                mMasterSongList.addAll(albumModel.getData().getSongs());

                if (getView() != null) {
                    getView().post(() -> {
                        if (isAdded()) {
                            startEntranceTransition();
                        }
                    });
                }

                setAdapter(arrayObjectAdapter);
            }

            @Override
            public void onFailure(String error, int responseCode) {}

            @Override
            public void onError(String error) {}
        });
    }

    private void getPlaylistDataList(PlaylistsResultModel plResults) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.retrievePlaylistById(plResults.getId(), 0, 50);
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Reader reader = new StringReader(response);
                PlaylistModel playlistModel = gson.fromJson(reader, PlaylistModel.class);

                arrayObjectAdapter = new ArrayObjectAdapter(songsPresenter);
                if (playlistModel.getData() != null && !playlistModel.getData().getSongs().isEmpty()) {
                    for (SongsResultModel song : playlistModel.getData().getSongs()) {
                        arrayObjectAdapter.add(song);
                    }
                }

                mMasterSongList.clear();
                mMasterSongList.addAll(playlistModel.getData().getSongs());

                if (getView() != null) {
                    getView().post(() -> {
                        if (isAdded()) {
                            startEntranceTransition();
                        }
                    });
                }

                setAdapter(arrayObjectAdapter);
            }

            @Override
            public void onFailure(String error, int responseCode) {}

            @Override
            public void onError(String error) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (postAdapter != null && getAdapter() == postAdapter) {
//            postAdapter.notifyArrayItemRangeChanged(0, postAdapter.size());
//        }

        if (getView() != null) {
            getView().post(() -> {
                VerticalGridView gridView = findVerticalGridView(getView());
                if (gridView != null) {
                    gridView.requestFocus();
                    if (myVerticalGridPresenter != null && myVerticalGridPresenter.getBorderDecoration() != null) {
                        myVerticalGridPresenter.getBorderDecoration().refreshFocus(gridView);
                    }
                }
            });
        }
    }

    private VerticalGridView findVerticalGridView(View view) {
        if (view instanceof VerticalGridView) {
            return (VerticalGridView) view;
        }
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                VerticalGridView found = findVerticalGridView(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}