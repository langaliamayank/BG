package com.androidtv.bhagavadgita.presenter;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import androidx.leanback.widget.ImageCardView;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.bumptech.glide.Glide;

public class PushtimargPresenter extends AbstractPresenter<ImageCardView> {
    private MasterActivity mContext;

    public PushtimargPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected ImageCardView onCreateView() {
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setBackgroundColor(Color.TRANSPARENT);
        cardView.setInfoVisibility(View.GONE);
        return cardView;
    }

    @Override
    public void onBindViewHolder(Object object, ImageCardView cardView) {
        if (object instanceof PushtimargModel) {
            PushtimargModel pushtimargModel = (PushtimargModel) object;

            cardView.setMainImageAdjustViewBounds(true);
            cardView.setMainImageDimensions(350, 197);
            cardView.setMainImageScaleType(ImageView.ScaleType.FIT_XY);

            Glide.with(getContext())
                    .load(mContext.getImage(pushtimargModel.getImage()))
                    .into(cardView.getMainImageView());

            cardView.setTitleText(pushtimargModel.getTitle());
        }
    }

    @Override
    public void onUnbindViewHolder(ImageCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
