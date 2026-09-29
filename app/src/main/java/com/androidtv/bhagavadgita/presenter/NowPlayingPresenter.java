package com.androidtv.bhagavadgita.presenter;


//import static com.androidtv.bhagavadgita.fragment.MusicFragment.mRowsAdapter;

import static com.androidtv.bhagavadgita.fragment.MusicFragment.mRowsAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;

import com.androidtv.bhagavadgita.PlayerActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.model.MediaCard;
import com.androidtv.bhagavadgita.playback.PlaybackActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class NowPlayingPresenter extends AbstractBasePresenter<BaseCardView> {
    private Context mContext;

    public NowPlayingPresenter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        BaseCardView cardView = new BaseCardView(mContext);
        cardView.setFocusable(false);
        cardView.setFocusableInTouchMode(false);
        cardView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_nowplaying_item, cardView, false));
        cardView.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite10));

        Button open = cardView.findViewById(R.id.buttonOpen);
        Button close = cardView.findViewById(R.id.buttonClose);
        open.setFocusable(true);
        open.setFocusableInTouchMode(true);
        close.setFocusable(true);
        close.setFocusableInTouchMode(true);

        return cardView;
    }


    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {
        if (object instanceof MediaCard) {
            MediaCard songsResultModel = (MediaCard) object;

            ((TextView) cardView.findViewById(R.id.textNPTitle)).setText(songsResultModel.getTitle());
            ((TextView) cardView.findViewById(R.id.textNPDetails)).setText(songsResultModel.getWriter());
            Glide.with(mContext)
                    .asDrawable()
                    .load(songsResultModel.getImageUri())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) cardView.findViewById(R.id.npImage));

            Glide.with(mContext)
                    .asGif()
                    .load(R.drawable.ic_action_visualizer)
                    .into((ImageView) cardView.findViewById(R.id.npVisualizer));

            ((Button) cardView.findViewById(R.id.buttonOpen)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, PlaybackActivity.class));
                }
            });

            ((Button) cardView.findViewById(R.id.buttonClose)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayerManager.getInstance(mContext).release();

                    if (mRowsAdapter != null) {
                        ListRow listRow = ((ListRow) mRowsAdapter.get(0));
                        mRowsAdapter.remove(listRow);
                    }
                }
            });
        }
    }

    @Override
    public void onUnbindViewHolder(BaseCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
