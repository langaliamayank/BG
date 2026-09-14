package com.androidtv.bhagavadgita.presenter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.bumptech.glide.Glide;

public class CardPresenter extends AbstractPresenter<ImageCardView> {
    private MasterActivity mContext;
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;

    public CardPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected ImageCardView onCreateView() {
        mDefaultBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorDefault);
        mSelectedBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorGreen);

        ImageCardView cardView = new ImageCardView(mContext) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.addOnLayoutChangeListener(sLayoutChangeListener);
        updateCardBackgroundColor(cardView, false);

//        cardView.setClipToOutline(true);
//        cardView.getMainImageView().setClipToOutline(true);
//        setParentRounded(cardView);
//        setChildRounded(cardView);
        return cardView;
    }

    private void setChildRounded(ImageCardView cardView) {
        View mainImageView = cardView.getMainImageView();
        mainImageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int cornerRadius = 13;
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
    }

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

    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? R.drawable.card_focus : R.drawable.card_normal;
        view.setBackgroundResource(color);
        view.setInfoVisibility(View.GONE);
        view.setInfoAreaBackgroundColor(Color.TRANSPARENT);
        view.findViewById(androidx.leanback.R.id.content_text).setPadding(3,3,3,3);
        view.setPadding(selected ? 5 : 0,selected ? 5 : 0,selected ? 5 : 0,selected ? 5 : 0);

        TextView titleTextView = view.findViewById(androidx.leanback.R.id.title_text);
        titleTextView.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorTextPrimary));
    }

    @Override
    public void onBindViewHolder(Object object, ImageCardView cardView) {
        if (object instanceof ChapterModel) {
            ChapterModel chapterModel = (ChapterModel) object;

            cardView.setMainImageAdjustViewBounds(true);
            cardView.setMainImageDimensions(250, 333); /* 3:4 - 1920x2560*/
            cardView.setMainImageScaleType(ImageView.ScaleType.FIT_XY);

            LogTag.e("bgposter; " + mContext.getImagePath(chapterModel, false));

            Glide.with(getContext())
                    .load(mContext.getImagePath(chapterModel, false))
                    .into(cardView.getMainImageView());

            cardView.setTitleText("Chapter " + chapterModel.getChapterNumber());
            cardView.setContentText(chapterModel.getVersesCount() + " Verses");
        }
    }

    @Override
    public void onUnbindViewHolder(ImageCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
