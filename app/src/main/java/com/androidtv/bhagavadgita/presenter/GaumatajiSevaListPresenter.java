package com.androidtv.bhagavadgita.presenter;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.androidtv.bhagavadgita.PlayerActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.fragment.GaumatajiSevaFragment;
import com.androidtv.bhagavadgita.model.GaumatajiSevaModel;
import com.androidtv.bhagavadgita.model.MediaCard;
import com.androidtv.bhagavadgita.model.music.songs.SongsAllModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.stream.Collectors;

public class GaumatajiSevaListPresenter extends AbstractBasePresenter<BaseCardView> {
    private Context mContext;
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;
    private int mMode;

    public GaumatajiSevaListPresenter(Context context, int mode) {
        super(context);
        mContext = context;
        mMode = mode;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        mDefaultBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorDefault);
        mSelectedBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorGreen);

        BaseCardView cardView = new BaseCardView(mContext);
        cardView.setFocusable(false);
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_song_item, null));
        cardView.addOnLayoutChangeListener(sLayoutChangeListener);

//        cardView.setClipToOutline(true);
//        cardView.getMainImageView().setClipToOutline(true);
//        setParentRounded(cardView);
//        setChildRounded(cardView);
        return cardView;
    }

//    private void setChildRounded(BaseCardView cardView) {
//        View mainImageView = cardView.getMainImageView();
//        mainImageView.setOutlineProvider(new ViewOutlineProvider() {
//            @Override
//            public void getOutline(View view, Outline outline) {
//                int cornerRadius = 13;
//                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
//            }
//        });
//    }

//    private void setParentRounded(@Nullable View view) {
//        View mainImageView = view;
//        mainImageView.setOutlineProvider(new ViewOutlineProvider() {
//            @Override
//            public void getOutline(View view, Outline outline) {
//                int cornerRadius = 13;
//                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
//            }
//        });
//    }

    private View.OnLayoutChangeListener sLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            v.setPivotY(v.getMeasuredHeight());
        }
    };

    private void updateCardBackgroundColor(LinearLayout view, boolean selected) {
//        int color = selected ? R.drawable.card_focus : R.drawable.card_normal;
//        view.setBackgroundResource(color);
    }

    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {
        if (object instanceof GaumatajiSevaModel) {
            GaumatajiSevaModel gaumatajiSevaModel = (GaumatajiSevaModel) object;

            stringDecode(((TextView) cardView.findViewById(R.id.textTitle)), gaumatajiSevaModel.getTitle());
            stringDecode(((TextView) cardView.findViewById(R.id.textDetail)), gaumatajiSevaModel.getPrice() + "");

            ((LinearLayout) cardView.findViewById(R.id.songContainer)).setFocusable(true);
            ((LinearLayout) cardView.findViewById(R.id.songContainer)).setFocusableInTouchMode(true);
            ((LinearLayout) cardView.findViewById(R.id.songContainer)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    updateCardBackgroundColor(((LinearLayout) cardView.findViewById(R.id.songContainer)), hasFocus);
                }
            });

            ((LinearLayout) cardView.findViewById(R.id.songContainer)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (getCurrentPlayback(song)) {
//                        Intent intent = new Intent(mContext, PlayerActivity.class);
//                        mContext.startActivity(intent);
//
//                    } else {
//                        Intent intent = new Intent(mContext, PlayerActivity.class);
//                        intent.putExtra("DATA", song);
//                        mContext.startActivity(intent);
//                    }
                }
            });
        }
    }

    public boolean getCurrentPlayback(SongsResultModel songsResultModel) {
        ExoPlayer player = PlayerManager.getInstance(mContext).getPlayer();
        MediaItem current = player.getCurrentMediaItem();
        if (current != null) {
            MediaCard card = new MediaCard(
                    current.mediaMetadata.writer != null ? current.mediaMetadata.writer.toString() : "Unknown", "",
                    current.mediaMetadata.title != null ? current.mediaMetadata.title.toString() : "Unknown",
                    current.mediaMetadata.artist != null ? current.mediaMetadata.artist.toString() : "Unknown",
                    current.mediaMetadata.artworkUri);

            if (songsResultModel.getId().equalsIgnoreCase(card.getWriter())) {
                return true;
            }
        }

        return false;
    }

    public void stringDecode(TextView textView, String s) {
        try {
            textView.setText(URLDecoder.decode(s.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
