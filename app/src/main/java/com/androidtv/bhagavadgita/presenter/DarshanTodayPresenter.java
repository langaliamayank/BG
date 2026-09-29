package com.androidtv.bhagavadgita.presenter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.CommanActivity;
import com.androidtv.bhagavadgita.DarshanActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.DarshanInnerAdapter;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ActionModel;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.bumptech.glide.Glide;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class DarshanTodayPresenter extends AbstractBasePresenter<BaseCardView> {
    private MasterActivity mContext;
    private List<DarshanModel> mDarshanList;
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;

    public DarshanTodayPresenter(MasterActivity context, List<DarshanModel> darshanList) {
        super(context);
        mContext = context;
        mDarshanList = darshanList;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        mDefaultBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorTransparent);
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
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_darshan_nxt_item, null));
        updateCardBackgroundColor(cardView, false);

        return cardView;
    }

    private void updateCardBackgroundColor(BaseCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;
        view.setBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {
        if (!(object instanceof DarshanModel)) {
            return;
        }

        DarshanModel darshanModel = (DarshanModel) object;
        ImageView imageView = cardView.findViewById(R.id.imageview);
        if (imageView != null) {
            Glide.with(getContext())
                    .load(mContext.getImagePath(darshanModel, true))
                    .into(imageView);
        }

        ImageView imageOverlay = cardView.findViewById(R.id.imageOverlay);
        if (imageOverlay != null) {
            // 1. Resolve whether the drawable is set as background or src
            Drawable drawable = imageOverlay.getBackground();
            boolean isBackground = true;

            if (!(drawable instanceof LayerDrawable)) {
                drawable = imageOverlay.getDrawable();
                isBackground = false;
            }

            if (drawable instanceof LayerDrawable) {
                LayerDrawable layerDrawable = (LayerDrawable) drawable.mutate();

                // 2. Safe color resolution with fallback
                String colorHex = SharePreferenceManager.getString("KEY_THEME_COLOR");
                int dynamicStartColor = Color.parseColor(colorHex.equals("") ? "#00000000" : colorHex);

                int[] colors = new int[]{dynamicStartColor, Color.TRANSPARENT, Color.TRANSPARENT};

                // 3. Recreate the layers to ensure the new colors take effect
                // Top-to-Bottom (angle 270)
                GradientDrawable topLayer = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                topLayer.setGradientCenter(0.10f, 0.5f);

                // Left-to-Right (angle 0)
                GradientDrawable leftLayer = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                leftLayer.setGradientCenter(0.15f, 0.5f);

                // Right to Left (angle 180)
                GradientDrawable rightLayer = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
                rightLayer.setGradientCenter(0.85f, 0.5f);

                // 4. Update the layer IDs
                layerDrawable.setDrawableByLayerId(R.id.topLayer, topLayer);
                layerDrawable.setDrawableByLayerId(R.id.leftLayer, leftLayer);
                layerDrawable.setDrawableByLayerId(R.id.rightLayer, rightLayer);

                // 5. Re-apply and force redraw
                if (isBackground) {
                    imageOverlay.setBackground(layerDrawable);
                } else {
                    imageOverlay.setImageDrawable(layerDrawable);
                }
                imageOverlay.invalidate();
            }
        }

        imageOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(DarshanActivity.createIntent(getActivity(), (DarshanModel) item));
                ActionModel action = new ActionModel(9, "Darshan", false, 0);
                mContext.startActivity(CommanActivity.createIntent(mContext, action));
            }
        });

        RecyclerView recyclerView = cardView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(new DarshanInnerAdapter(mContext, mDarshanList, darshanModel));
    }

    @Override
    public void onUnbindViewHolder(BaseCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }
}
