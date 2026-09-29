package com.androidtv.bhagavadgita.presenter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;

import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.model.VersesModel;

import java.util.ArrayList;

public class ChapterVersesPresenter extends AbstractBasePresenter<BaseCardView> {
    private Context mContext;
    private ArrayList<VersesModel> mVersesList = new ArrayList<>();
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;

    public ChapterVersesPresenter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        mDefaultBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorBlack50);
        mSelectedBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorTransparent);

        BaseCardView cardView = new BaseCardView(mContext, null, R.style.SideInfoCardStyle) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };
        cardView.setFocusable(true);
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_verse_item, null));
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
        if (object instanceof VersesModel) {
            VersesModel versesModel = (VersesModel) object;

            ((TextView) cardView.findViewById(R.id.textVerseNumber)).setText("Verse " + versesModel.getVerseNumber());
            ((TextView) cardView.findViewById(R.id.textVerse)).setText(versesModel.getText().trim());
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
