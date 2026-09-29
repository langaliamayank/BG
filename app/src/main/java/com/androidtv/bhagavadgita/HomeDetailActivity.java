package com.androidtv.bhagavadgita;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;

import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.HistoryModel;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.model.VallabhacharyaModel;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.bumptech.glide.Glide;

public class HomeDetailActivity extends MasterActivity {

    private HistoryModel mHistoryModel;
    private PushtimargModel mPushtimargModel;
    private VallabhacharyaModel mVallabhacharyaModel;
    private int mCome, finalHeight, finalWidth;

    public static Intent createIntent(Context context, HistoryModel historyModel, int mode) {
        Intent intent = new Intent(context, HomeDetailActivity.class);
        intent.putExtra("DATA", historyModel);
        intent.putExtra("MODE", mode);
        return intent;
    }

    public static Intent createIntent(Context context, PushtimargModel pushtimargModel, int mode) {
        Intent intent = new Intent(context, HomeDetailActivity.class);
        intent.putExtra("DATA", pushtimargModel);
        intent.putExtra("MODE", mode);
        return intent;
    }

    public static Intent createIntent(Context context, VallabhacharyaModel vallabhacharyaModel, int mode) {
        Intent intent = new Intent(context, HomeDetailActivity.class);
        intent.putExtra("DATA", vallabhacharyaModel);
        intent.putExtra("MODE", mode);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_detail);

        mCome = getIntent().getIntExtra("MODE", 0);
        if (mCome == 0) {
            mHistoryModel = (HistoryModel) getIntent().getSerializableExtra("DATA");
            if (mHistoryModel != null) {
                ((TextView) findViewById(R.id.textName)).setText(mHistoryModel.getTitleHi());

                String mImagePath = APIInterface.OTHER_IMAGE_BASE_PATH + mHistoryModel.getImage() + ".jpeg?raw=true";
                Glide.with(HomeDetailActivity.this)
                        .load(mImagePath)
                        .into(((ImageView) findViewById(R.id.imageData)));

                final ViewTreeObserver vto = ((ImageView) findViewById(R.id.imageData)).getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ((ImageView) findViewById(R.id.imageData)).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        finalHeight = ((ImageView) findViewById(R.id.imageData)).getMeasuredHeight();
                        finalWidth = ((ImageView) findViewById(R.id.imageData)).getMeasuredWidth();
                        makeSpan(mHistoryModel.getDescriptionHi());
                    }
                });

            }
        }

        if (mCome == 1) {
            mPushtimargModel = (PushtimargModel) getIntent().getSerializableExtra("DATA");
            if (mPushtimargModel != null) {
                ((TextView) findViewById(R.id.textName)).setText(mPushtimargModel.getTitle());

                Glide.with(HomeDetailActivity.this)
                        .load(getImage(mPushtimargModel.getImage()))
                        .into(((ImageView) findViewById(R.id.imageData)));

                final ViewTreeObserver vto = ((ImageView) findViewById(R.id.imageData)).getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ((ImageView) findViewById(R.id.imageData)).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        finalHeight = ((ImageView) findViewById(R.id.imageData)).getMeasuredHeight();
                        finalWidth = ((ImageView) findViewById(R.id.imageData)).getMeasuredWidth();
                        makeSpan(mPushtimargModel.getDescription());
                    }
                });

            }
        }

        if (mCome == 2) {
            mVallabhacharyaModel = (VallabhacharyaModel) getIntent().getSerializableExtra("DATA");
            if (mVallabhacharyaModel != null) {
                ((TextView) findViewById(R.id.textName)).setText(mVallabhacharyaModel.getTitle());

                String mImagePath = APIInterface.OTHER_IMAGE_BASE_PATH + mVallabhacharyaModel.getImage() + ".jpeg?raw=true";
                Glide.with(HomeDetailActivity.this)
                        .load(mImagePath)
                        .into(((ImageView) findViewById(R.id.imageData)));

                final ViewTreeObserver vto = ((ImageView) findViewById(R.id.imageData)).getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ((ImageView) findViewById(R.id.imageData)).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        finalHeight = ((ImageView) findViewById(R.id.imageData)).getMeasuredHeight();
                        finalWidth = ((ImageView) findViewById(R.id.imageData)).getMeasuredWidth();
                        makeSpan(mVallabhacharyaModel.getDescription());
                    }
                });

            }
        }

    }

    private void makeSpan(String message) {
        String[] paragraphs = message.split("\n\n", 2); // Split into two paragraphs

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        TextView textView = findViewById(R.id.textInfo);

        float fontSpacing = textView.getPaint().getFontSpacing();
        int lines = (int) (finalHeight / fontSpacing);

        // First paragraph with image margin
        if (paragraphs.length > 0) {
            int start = ssb.length();
            ssb.append(paragraphs[0]);
            ssb.append("\n\n");
            int end = ssb.length();

            FlowTextHelper span = new FlowTextHelper(lines, finalWidth + 10);
            ssb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Second paragraph starts normally (no span)
        if (paragraphs.length > 1) {
            ssb.append(paragraphs[1]);
        }

        textView.setText(ssb);
    }

    class FlowTextHelper implements LeadingMarginSpan.LeadingMarginSpan2 {

        private int margin;
        private int lines;

        FlowTextHelper(int lines, int margin) {
            this.margin = margin;
            this.lines = lines;
        }

        @Override
        public int getLeadingMargin(boolean first) {
            if (first) {
                return margin;
            } else {
                return 0;
            }
        }

        @Override
        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                      int top, int baseline, int bottom, CharSequence text,
                                      int start, int end, boolean first, Layout layout) {
        }

        @Override
        public int getLeadingMarginLineCount() {
            return lines;
        }
    }
}
