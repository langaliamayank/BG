package com.androidtv.bhagavadgita.presenter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;

import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.model.music.albums.AlbumsResultsModel;
import com.androidtv.bhagavadgita.model.music.artists.ArtistsResultModel;
import com.androidtv.bhagavadgita.model.music.playlists.PlaylistsResultModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class SongsPresenter extends AbstractPresenter<ImageCardView> {
    private Context mContext;
    public SongsPresenter(Context context, int mode) {
        super(context);
        mContext = context;
    }

    @Override
    protected ImageCardView onCreateView() {
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite25));
        cardView.setInfoAreaBackgroundColor(Color.TRANSPARENT);
        return cardView;
    }

    @Override
    public void onBindViewHolder(Object object, ImageCardView cardView) {
        cardView.setMainImageAdjustViewBounds(true);
        cardView.setMainImageDimensions(250, 250);
        cardView.setMainImageScaleType(ImageView.ScaleType.FIT_XY);

        if (object instanceof SongsResultModel) {
            SongsResultModel song = (SongsResultModel) object;

            Glide.with(mContext)
                    .asDrawable()
                    .load(song.getImage().get(song.getImage().size() - 1).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(cardView.getMainImageView());

            cardView.setTitleText(song.getName());
            cardView.setContentText(song.getArtists().getPrimary().get(0).getName());
        }

        if (object instanceof ArtistsResultModel) {
            ArtistsResultModel artists = (ArtistsResultModel) object;

            Glide.with(mContext)
                    .asDrawable()
                    .load(artists.getImage().get(artists.getImage().size() - 1).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(cardView.getMainImageView());

            cardView.setTitleText(artists.getName());
            cardView.setContentText(artists.getRole());
        }

        if (object instanceof AlbumsResultsModel) {
            AlbumsResultsModel albums = (AlbumsResultsModel) object;

            Glide.with(mContext)
                    .asDrawable()
                    .load(albums.getImage().get(albums.getImage().size() - 1).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(cardView.getMainImageView());

            cardView.setTitleText(albums.getName());
            cardView.setContentText(albums.getYear() + "");
        }

        if (object instanceof PlaylistsResultModel) {
            PlaylistsResultModel playlist = (PlaylistsResultModel) object;

            Glide.with(mContext)
                    .asDrawable()
                    .load(playlist.getImage().get(playlist.getImage().size() - 1).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(cardView.getMainImageView());

            cardView.setTitleText(playlist.getName());
            cardView.setContentText(playlist.getLanguage() + " " + playlist.getSongCount());
        }
    }

    public Drawable getImage(String path) {
        int resId = mContext.getResources().getIdentifier(path, "drawable", mContext.getPackageName());
        return ContextCompat.getDrawable(mContext, resId);
    }

    @Override
    public void onUnbindViewHolder(ImageCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
