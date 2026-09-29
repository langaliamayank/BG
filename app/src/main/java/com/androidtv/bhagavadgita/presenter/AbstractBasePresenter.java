package com.androidtv.bhagavadgita.presenter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.Presenter;

public abstract class AbstractBasePresenter<T extends BaseCardView> extends Presenter {

    private final Context mContext;

    protected AbstractBasePresenter(Context mContext) {
        this.mContext = mContext;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent) {
        T cardView = onCreateView(parent);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        // Prevent image view from covering the decoration
        cardView.setElevation(0f);
        cardView.setClipChildren(false);
        cardView.setClipToOutline(true);
        return new ViewHolder(cardView);
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

    protected abstract T onCreateView(ViewGroup parent);

    public abstract void onBindViewHolder(Object card, T cardView);


}
