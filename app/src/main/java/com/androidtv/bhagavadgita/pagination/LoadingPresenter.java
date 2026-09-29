package com.androidtv.bhagavadgita.pagination;

import android.view.ViewGroup;

import androidx.leanback.widget.Presenter;

public class LoadingPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LoadingCardView cardView = new LoadingCardView(parent.getContext());
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof LoadingCardView){
            LoadingCardView cardView = (LoadingCardView) viewHolder.view;
            cardView.isLoading(true);
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        if (viewHolder.view instanceof LoadingCardView) {
            LoadingCardView cardView = (LoadingCardView) viewHolder.view;
            cardView.isLoading(false);
        }
    }
}