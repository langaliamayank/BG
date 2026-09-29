package com.androidtv.bhagavadgita.pagination;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.leanback.widget.BaseCardView;

import com.androidtv.bhagavadgita.R;

public class LoadingCardView extends BaseCardView {

    private ProgressBar mProgressBar;
    private RelativeLayout mProgressContainer;

    public LoadingCardView(Context context, int styleResId) {
        super(new ContextThemeWrapper(context, styleResId), null, 0);
        buildLoadingCardView(styleResId);
    }

    public LoadingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getStyledContext(context, attrs, defStyleAttr), attrs, defStyleAttr);
        buildLoadingCardView(getImageCardViewStyle(context, attrs, defStyleAttr));
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    private void buildLoadingCardView(int styleResId) {
        setFocusable(false);
        setFocusableInTouchMode(false);
        setCardType(CARD_TYPE_MAIN_ONLY);
        setBackgroundResource(R.color.colorTransparent);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_loading_card, this);
        TypedArray cardAttrs =
                getContext().obtainStyledAttributes(
                        styleResId, androidx.leanback.R.styleable.lbImageCardView);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_indicator);
        mProgressContainer = (RelativeLayout) findViewById(R.id.progressContainer);

//        if (getScreenWidth() == 1280 && getScreenHeight() == 720) {
//            mProgressContainer.getLayoutParams().width = 238;
//            mProgressContainer.getLayoutParams().height = 238;
//
//        } else {
            mProgressContainer.getLayoutParams().width = 200;
            mProgressContainer.getLayoutParams().height = 200;
//        }

        cardAttrs.recycle();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public void isLoading(boolean isLoading) {
        mProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private static Context getStyledContext(Context context, AttributeSet attrs, int defStyleAttr) {
        int style = getImageCardViewStyle(context, attrs, defStyleAttr);
        return new ContextThemeWrapper(context, style);
    }

    private static int getImageCardViewStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        int style = null == attrs ? 0 : attrs.getStyleAttribute();
        if (0 == style) {
            TypedArray styledAttrs =
                    context.obtainStyledAttributes(
                            androidx.leanback.R.styleable.LeanbackTheme);
            style = styledAttrs.getResourceId(
                    androidx.leanback.R.styleable.LeanbackTheme_imageCardViewStyle, 0);
            styledAttrs.recycle();
        }
        return style;
    }

    public LoadingCardView(Context context) {
        this(context, null);
    }

    public LoadingCardView(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.leanback.R.attr.imageCardViewStyle);
    }

}