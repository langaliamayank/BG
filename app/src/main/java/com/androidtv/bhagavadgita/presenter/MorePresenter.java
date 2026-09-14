package com.androidtv.bhagavadgita.presenter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;

import com.androidtv.bhagavadgita.MasterActivity;
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
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;

    public MorePresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        mDefaultBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorBlack50);
        mSelectedBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorTransparent);

        BaseCardView cardView = new BaseCardView(mContext) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_action_item, null));
        cardView.addOnLayoutChangeListener(sLayoutChangeListener);

        // --- Calculate width for exactly 4 cards visible ---
        int parentWidth = parent.getWidth();
        if (parentWidth <= 0) {
            parentWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        }

        // Account for Leanback left/right padding + inter-item spacing (adjust dp values to match your theme)
        int horizontalPadding = parent.getPaddingLeft() + parent.getPaddingRight();
        int itemSpacing = (int) (16 * mContext.getResources().getDisplayMetrics().density); // e.g. 16dp spacing
        int totalSpacing = horizontalPadding + (itemSpacing * 3); // 3 gaps between 4 items

        int itemWidth = (parentWidth - totalSpacing) / 4;

        // Apply calculated width to the BaseCardView
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(params);

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

        if (object instanceof ActionModel) {
            ActionModel actionModel = (ActionModel) object;

            ((ImageView) cardView.findViewById(R.id.action_icon)).setImageResource(actionModel.getIcon());
            ((TextView) cardView.findViewById(R.id.action_text)).setText(actionModel.getTitle());
        }
    }

    @Override
    public void onUnbindViewHolder(BaseCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
