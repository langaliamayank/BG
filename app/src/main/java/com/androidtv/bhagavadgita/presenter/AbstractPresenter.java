package com.androidtv.bhagavadgita.presenter;

import android.content.Context;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.Nullable;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

public abstract class AbstractPresenter<T extends ImageCardView> extends Presenter {

    private final Context mContext;

    protected AbstractPresenter(Context mContext) {
        this.mContext = mContext;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent) {
        T cardView = onCreateView();
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        // Prevent image view from covering the decoration
        cardView.setElevation(0f);
        cardView.setClipChildren(false);
        cardView.setClipToOutline(true);
        cardView.getMainImageView().setClipToOutline(true);
        setParentRounded(cardView);
        setChildRounded(cardView);

        return new ViewHolder(cardView);
    }

    private void setChildRounded(ImageCardView cardView) {
        View mainImageView = cardView.getMainImageView();
        //        mainImageView.setAlpha(0.5f);
        mainImageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int cornerRadius = 5;
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
    }

    private void setParentRounded(@Nullable View view) {
        View mainImageView = view;
        mainImageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int cornerRadius = 13;
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
    }

    @Override
    public final void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Object card = (Object) item;
        onBindViewHolder(card, (T) viewHolder.view);
    }

    @Override
    public final void onUnbindViewHolder(ViewHolder viewHolder) {
        onUnbindViewHolder((T) viewHolder.view);
    }

    public void onUnbindViewHolder(T cardView) {
        // Nothing to clean up. Override if necessary.
    }

    protected abstract T onCreateView();

    public abstract void onBindViewHolder(Object card, T cardView);


}
