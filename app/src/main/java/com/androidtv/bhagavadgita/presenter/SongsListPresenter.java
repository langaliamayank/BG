package com.androidtv.bhagavadgita.presenter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.leanback.widget.BaseCardView;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.androidtv.bhagavadgita.PlayerActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.model.MediaCard;
import com.androidtv.bhagavadgita.model.music.songs.SongsAllModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.stream.Collectors;

public class SongsListPresenter extends AbstractBasePresenter<BaseCardView> {
    private Context mContext;

    public SongsListPresenter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        BaseCardView cardView = new BaseCardView(mContext, null, R.style.SideInfoCardStyle);
        cardView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorCard));
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_song_item, null));
        return cardView;
    }

    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {
        if (object instanceof SongsResultModel) {
            SongsResultModel song = (SongsResultModel) object;

            stringDecode(((TextView) cardView.findViewById(R.id.textTitle)), song.getName());
            String artists = song.getArtists().getAll().stream()
                    .map(SongsAllModel::getName)
                    .collect(Collectors.joining(", "));

            stringDecode(((TextView) cardView.findViewById(R.id.textDetail)), artists);

            boolean isPlaying = getCurrentPlayback(song);
            ((ImageView) cardView.findViewById(R.id.imageVisualizer)).setVisibility(isPlaying ? View.VISIBLE : View.GONE);

            Glide.with(mContext)
                    .asDrawable()
                    .load(song.getImage().get(song.getImage().size() - 1).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) cardView.findViewById(R.id.imageSong));

            Glide.with(mContext)
                    .asGif()
                    .load(R.drawable.ic_action_visualizer)
                    .into((ImageView) cardView.findViewById(R.id.imageVisualizer));
        }
    }


    public boolean getCurrentPlayback(SongsResultModel song) {
        if (song == null || song.getDownloadUrl() == null || song.getDownloadUrl().isEmpty()) return false;

        MediaItem current = PlayerManager.getInstance(mContext).getPlayer().getCurrentMediaItem();
        if (current == null || current.localConfiguration == null) return false;

        String url = song.getDownloadUrl().get(0).getUrl();
        return url != null && Uri.parse(url).equals(current.localConfiguration.uri);
    }

    public void stringDecode(TextView textView, String s) {
        textView.setText(HtmlCompat.fromHtml(s, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim());
    }

    public Drawable getImage(String path) {
        int resId = mContext.getResources().getIdentifier(path, "drawable", mContext.getPackageName());
        return ContextCompat.getDrawable(mContext, resId);
    }

    @Override
    public void onUnbindViewHolder(BaseCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
