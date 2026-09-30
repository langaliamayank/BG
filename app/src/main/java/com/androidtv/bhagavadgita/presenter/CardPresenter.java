package com.androidtv.bhagavadgita.presenter;

import android.view.View;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;

import com.androidtv.bhagavadgita.ChapterVersesActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.bumptech.glide.Glide;

public class CardPresenter extends AbstractPresenter<ImageCardView> {
    private MasterActivity mContext;

    public CardPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected ImageCardView onCreateView() {
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorCard));
        cardView.setInfoVisibility(View.GONE);
        return cardView;
    }

    @Override
    public void onBindViewHolder(Object object, ImageCardView cardView) {
        if (object instanceof ChapterModel) {
            ChapterModel chapterModel = (ChapterModel) object;

            cardView.setMainImageAdjustViewBounds(true);
            cardView.setMainImageDimensions(250, 333); /* 3:4 - 1920x2560*/
            cardView.setMainImageScaleType(ImageView.ScaleType.FIT_XY);

            Glide.with(getContext())
                    .load(mContext.getImagePath(chapterModel, false))
                    .into(cardView.getMainImageView());

            cardView.setTitleText("Chapter " + chapterModel.getChapterNumber());
            cardView.setContentText(chapterModel.getVersesCount() + " Verses");

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(ChapterVersesActivity.createIntent(mContext, chapterModel));
                }
            });
        }
    }

    @Override
    public void onUnbindViewHolder(ImageCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
