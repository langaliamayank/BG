package com.androidtv.bhagavadgita.pagination;

import android.app.Activity;

import com.androidtv.bhagavadgita.model.music.artists.ArtistsResultModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.androidtv.bhagavadgita.presenter.SongsListPresenter;
import com.androidtv.bhagavadgita.presenter.SongsPresenter;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends PaginationAdapter {

    public PostAdapter(Activity context, SongsListPresenter songsListPresenter, String tag) {
        super(context, songsListPresenter, tag);
    }

    public PostAdapter(Activity context, SongsPresenter songsPresenter, String tag) {
        super(context, songsPresenter, tag);
    }

    @Override
    public void addAllItems(List<?> items) {
        List<SongsResultModel> currentPosts = getAllItems();
        ArrayList<SongsResultModel> posts = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Object object = items.get(i);
            if (object instanceof SongsResultModel && !currentPosts.contains(object)) {
                posts.add((SongsResultModel) object);
            }
        }
//        Collections.sort(posts);
        addPosts(posts);
    }

    @Override
    public List<SongsResultModel> getAllItems() {
        List<Object> itemList = getItems();
        ArrayList<SongsResultModel> posts = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            Object object = itemList.get(i);
            if (object instanceof SongsResultModel) posts.add((SongsResultModel) object);
        }
        return posts;
    }
}
