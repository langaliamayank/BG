package com.androidtv.bhagavadgita.presenter;


import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.CommanActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.MySettingsActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.model.ActionModel;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.androidtv.bhagavadgita.model.VersesModel;

import java.time.LocalDate;
import java.util.ArrayList;

public class MorePresenter extends AbstractBasePresenter<BaseCardView> {
    private MasterActivity mContext;

    public MorePresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        BaseCardView cardView = new BaseCardView(mContext);
        cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorCard));
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_action_item, null));
        return cardView;
    }

    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {

        if (object instanceof ActionModel) {
            ActionModel actionModel = (ActionModel) object;

            ((ImageView) cardView.findViewById(R.id.action_icon)).setImageResource(actionModel.getIcon());
            ((TextView) cardView.findViewById(R.id.action_text)).setText(actionModel.getTitle());

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long id = actionModel.getId();

                    switch ((int) id) {
                        case 0:
                        case 2:
                        case 4:
                            mContext.startActivity(CommanActivity.createIntent(mContext, actionModel));
                            return;

                        case 6:
                            mContext.startActivity(new Intent(mContext, CalendarActivity.class));
                            return;

                        default:
                            mContext.startActivity(new Intent(mContext, MySettingsActivity.class));
                            break;
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
