package com.androidtv.bhagavadgita;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.leanback.app.BackgroundManager;

import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.bumptech.glide.Glide;

public class VersesDetailActivity extends MasterActivity {

    private ChapterModel mChapterModel;

    public static Intent createIntent(Context context, ChapterModel chapterModel) {
        Intent intent = new Intent(context, VersesDetailActivity.class);
        intent.putExtra("DATA", chapterModel);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verses_detail);

        mChapterModel = (ChapterModel) getIntent().getSerializableExtra("DATA");
        if (mChapterModel != null) {

            ((TextView) findViewById(R.id.textChapterTitle)).setText("Chapter " + mChapterModel.getChapterNumber() + ": " + mChapterModel.getNameTranslation());
            ((TextView) findViewById(R.id.textChapterInfo)).setText(mChapterModel.getChapterSummary());
            ((TextView) findViewById(R.id.textOther)).setText(mChapterModel.getVersesCount() + " Verses");

            Glide.with(VersesDetailActivity.this)
                    .load(getImagePath(mChapterModel, false))
                    .into(((ImageView) findViewById(R.id.imageChapter)));

//            String selectedType = SharePreferenceManager.getString("LANGUAGE");
//            if (selectedType.isEmpty() || selectedType == null || selectedType.equalsIgnoreCase("1")) {
//                ((TextView) findViewById(R.id.textChapterNumber)).setText("Chapter " + mChapterModel.getChapterNumber() + " • " + mChapterModel.getVersesCount() + " Verses");
//                ((TextView) findViewById(R.id.textChapterTitle)).setText(mChapterModel.getNameTransliterated());
//                ((TextView) findViewById(R.id.textChapterInfo)).setText(mChapterModel.getChapterSummary());
//
//                Glide.with(VersesDetailActivity.this)
//                        .load(getImagePath(mChapterModel, false))
//                        .into(((ImageView) findViewById(R.id.imageChapter)));

//            } else {
//                ((TextView) findViewById(R.id.textChapterNumber)).setText("Chapter " + mChapterModel.getChapterNumber() + " • " + mChapterModel.getVersesCount() + " Verses");
//                ((TextView) findViewById(R.id.textChapterTitle)).setText(mChapterModel.getName());
//                ((TextView) findViewById(R.id.textChapterInfo)).setText(mChapterModel.getChapterSummaryHindi());
//
//                Glide.with(VersesDetailActivity.this)
//                        .load(getImagePath(mChapterModel, false))
//                        .into(((ImageView) findViewById(R.id.imageChapter)));
//            }
        }
    }
}

