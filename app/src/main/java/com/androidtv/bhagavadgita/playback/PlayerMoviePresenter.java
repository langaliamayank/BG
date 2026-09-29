package com.androidtv.bhagavadgita.playback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.Presenter;
import androidx.media3.common.util.UnstableApi;

import com.androidtv.bhagavadgita.R;


@UnstableApi
public class PlayerMoviePresenter extends Presenter {

    private final Context mContext;

    public PlayerMoviePresenter(Context activity) {
        mContext = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        BaseCardView mCardView = new BaseCardView(mContext, null, R.style.SideInfoCardStyle);
        mCardView.addView(LayoutInflater.from(mContext).inflate(R.layout.item_card_player, null));
        return new ViewHolder(mCardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {

        PlaybackTransportControlGlue<ExoPlayerAdapter> glue = (PlaybackTransportControlGlue<ExoPlayerAdapter>) item;

        if (glue != null) {
            stringDecode(((ViewHolder) viewHolder).player_title, glue.getTitle().toString());
            stringDecode(((ViewHolder) viewHolder).player_detail, glue.getSubtitle().toString());
        }
    }

    public void stringDecode(TextView textView, String s) {
        textView.setText(HtmlCompat.fromHtml(s, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim());
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }

    public class ViewHolder extends Presenter.ViewHolder {
        private final TextView player_title;
        private final TextView player_detail;
        private final RelativeLayout player_container;

        public ViewHolder(View view) {
            super(view);
            player_title = view.findViewById(R.id.player_title);
            player_detail = view.findViewById(R.id.player_detail);
            player_container = view.findViewById(R.id.playerParent);

            player_container.setFocusable(false);
            player_container.setFocusableInTouchMode(false);
        }
    }
}
