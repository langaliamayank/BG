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
import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.androidtv.bhagavadgita.model.VersesModel;

import java.time.LocalDate;
import java.util.ArrayList;

public class FestivalPresenter extends AbstractBasePresenter<BaseCardView> {
    private MasterActivity mContext;
    private ArrayList<VersesModel> mVersesList = new ArrayList<>();

    public FestivalPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        BaseCardView cardView = new BaseCardView(mContext);
        cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorCard));
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_calendar_event_item, null));
        return cardView;
    }

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
