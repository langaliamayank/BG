package com.androidtv.bhagavadgita.presenter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;

import com.androidtv.bhagavadgita.HomeDetailActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.model.VallabhacharyaModel;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.bumptech.glide.Glide;

public class VallabhacharyaPresenter extends AbstractPresenter<ImageCardView> {
    private MasterActivity mContext;

    public VallabhacharyaPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected ImageCardView onCreateView() {
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlack50));
        cardView.setInfoVisibility(View.GONE);
        return cardView;
    }

    @Override
    public void onBindViewHolder(Object object, ImageCardView cardView) {
        if (object instanceof VallabhacharyaModel) {
            VallabhacharyaModel vallabhacharyaModel = (VallabhacharyaModel) object;

            cardView.setMainImageAdjustViewBounds(true);
            cardView.setMainImageDimensions(350, 197);
            cardView.setMainImageScaleType(ImageView.ScaleType.FIT_XY);

            Glide.with(getContext())
                    .load(mContext.getImagePath(vallabhacharyaModel, false))
                    .into(cardView.getMainImageView());

            cardView.setTitleText(vallabhacharyaModel.getTitle());

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(HomeDetailActivity.createIntent(mContext, vallabhacharyaModel, 2));
                }
            });
        }
    }

    @Override
    public void onUnbindViewHolder(ImageCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
