package com.androidtv.bhagavadgita.presenter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;

import com.androidtv.bhagavadgita.CommanActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.model.ActionModel;

public class GitaPresenter extends AbstractBasePresenter<BaseCardView> {
    private MasterActivity mContext;

    public GitaPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        BaseCardView cardView = new BaseCardView(mContext);
        cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorCard));
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_gita_item, null));
        return cardView;
    }

    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {
        if (object instanceof ActionModel) {
            ActionModel actionModel = (ActionModel) object;

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(CommanActivity.createIntent(mContext, actionModel));
                }
            });
        }

    }

    @Override
    public void onUnbindViewHolder(BaseCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
