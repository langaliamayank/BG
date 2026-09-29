package com.androidtv.bhagavadgita.presenter;

import android.view.View;
import android.widget.ImageView;

import androidx.leanback.widget.ImageCardView;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.HistoryModel;
import com.bumptech.glide.Glide;

public class HistoryPresenter extends AbstractPresenter<ImageCardView> {
    private MasterActivity mContext;

    public HistoryPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected ImageCardView onCreateView() {
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setInfoVisibility(View.GONE);
        return cardView;
    }

    @Override
    public void onBindViewHolder(Object object, ImageCardView cardView) {
        if (object instanceof HistoryModel) {
            HistoryModel historyModel = (HistoryModel) object;

            cardView.setMainImageAdjustViewBounds(true);
            cardView.setMainImageDimensions(350, 197);
            cardView.setMainImageScaleType(ImageView.ScaleType.FIT_XY);

            Glide.with(getContext())
                    .load(mContext.getImagePath(historyModel, false))
                    .into(cardView.getMainImageView());

            String selectedType = SharePreferenceManager.getString("LANGUAGE");
            if (selectedType.isEmpty() || selectedType == null || selectedType.equalsIgnoreCase("1")) {
                cardView.setTitleText(historyModel.getTitle());
            } else {
                cardView.setTitleText(historyModel.getTitleHi());
            }
        }
    }

    @Override
    public void onUnbindViewHolder(ImageCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
