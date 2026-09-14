package com.androidtv.bhagavadgita.fragment;

import android.os.Bundle;
import android.os.Handler;

import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.androidtv.bhagavadgita.CommanActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.MusicDetailActivity;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.model.GaumatajiSevaModel;
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
import com.androidtv.bhagavadgita.presenter.GaumatajiSevaListPresenter;
import com.androidtv.bhagavadgita.presenter.MyVerticalGridPresenter;
import com.androidtv.bhagavadgita.presenter.SongsListPresenter;
import com.google.gson.Gson;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class GaumatajiSevaFragment extends VerticalGridSupportFragment implements OnBackPressedListener {
    private static final int NUM_COLUMNS = 1;
    private static final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_NONE;
    private Object object;

    private GaumatajiSevaListPresenter gaumatajiSevaListPresenter;
    private ArrayObjectAdapter arrayObjectAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CommanActivity) getActivity()).setOnBackPressedListener(this);

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

        MyVerticalGridPresenter myVerticalGridPresenter = new MyVerticalGridPresenter(ZOOM_FACTOR, false);
        myVerticalGridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(myVerticalGridPresenter);

        object = getActivity().getIntent().getSerializableExtra("DATA");
//        if (object instanceof ArtistsResultModel) {
//            artistsResultModel = (ArtistsResultModel) object;
//            getArtistsDataList(artistsResultModel.getId(), currentPage);
//        }
//
//        if (object instanceof AlbumsResultsModel) {
//            albumsResultsModel = (AlbumsResultsModel) object;
//            getAlbumDataList(albumsResultsModel);
//        }
//
//        if (object instanceof PlaylistsResultModel) {
//            PlaylistsResultModel playlist = (PlaylistsResultModel) object;
//            getPlaylistDataList(playlist);
//        }

        getGaumatajiSevaList();
        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object
                    item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof GaumatajiSevaModel) {

                }
            }
        });
    }

    private void getGaumatajiSevaList() {

        List<GaumatajiSevaModel> gaumatajiSevaModelList = new ArrayList<>();
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(1, "One Gaumataji One Month Seva", 1100));
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(2, "One day Seva for 21 Gaumataji", 2100));
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(3, "100 Kg. Dana Seva Bhent", 2100));
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(4, "One Month Seva for One Gaumataji", 3100));
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(5, "Gaumataji Thuli/Satua Seva Bhent", 15000));
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(6, "One day Green Grass Seva Bhent", 31000));
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(7, "One day Dry fodder Seva Bhent", 35000));
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(8, "One Gaumataji one year Seva Bhent", 41000));
        gaumatajiSevaModelList.add(new GaumatajiSevaModel(9, "One day Seva for all Gaumataji", 151000));

        GaumatajiSevaListPresenter gaumatajiSevaPresenter = new GaumatajiSevaListPresenter(getActivity(), 1);
        arrayObjectAdapter = new ArrayObjectAdapter(gaumatajiSevaPresenter);

        for (GaumatajiSevaModel song : gaumatajiSevaModelList) {
            arrayObjectAdapter.add(song);
        }

        setAdapter(arrayObjectAdapter);
    }

    @Override
    public void doBack() {
        try {
            MasterActivity.selectedPosition = 0;
            ((CommanActivity) getActivity()).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
