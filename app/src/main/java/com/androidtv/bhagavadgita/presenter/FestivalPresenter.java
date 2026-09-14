package com.androidtv.bhagavadgita.presenter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.androidtv.bhagavadgita.model.VersesModel;

import java.time.LocalDate;
import java.util.ArrayList;

public class FestivalPresenter extends AbstractBasePresenter<BaseCardView> {
    private MasterActivity mContext;
    private ArrayList<VersesModel> mVersesList = new ArrayList<>();
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;

    public FestivalPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        mDefaultBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorCard);
        mSelectedBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorGreen);

        BaseCardView cardView = new BaseCardView(mContext) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_calendar_event_item, null));
        cardView.addOnLayoutChangeListener(sLayoutChangeListener);

        updateCardBackgroundColor(cardView, false);
        return cardView;
    }


    private void updateCardBackgroundColor(BaseCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;
        view.setBackgroundColor(color);
    }

    private View.OnLayoutChangeListener sLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            v.setPivotY(v.getMeasuredHeight());
        }
    };

    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {

        if (object instanceof FestivalModel) {
            FestivalModel festivalModel = (FestivalModel) object;

            ((TextView) cardView.findViewById(R.id.textTitle)).setText(festivalModel.getTitle());

            String tithi;
            try {
                LocalDate localDate = LocalDate.parse(festivalModel.getDate()); // expects "yyyy-MM-dd"
                tithi = PanchangCalculator.getPanchang(
                        localDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getCompactDescription();
            } catch (Exception e) {
                tithi = "";
            }

            ((TextView) cardView.findViewById(R.id.textTithi)).setText(tithi);
            ((TextView) cardView.findViewById(R.id.textDescription)).setText(festivalModel.getDescription());
            ((TextView) cardView.findViewById(R.id.textDate)).setText(festivalModel.getDate());
        }
    }

    @Override
    public void onUnbindViewHolder(BaseCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }

    public void setList(ArrayList<VersesModel> listFromAdapter) {
        mVersesList.addAll(listFromAdapter);
    }

    public ArrayList<VersesModel> getList() {
        return mVersesList;
    }

}
