package com.androidtv.bhagavadgita;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.fragment.ChapterVersesFragment;
import com.androidtv.bhagavadgita.fragment.HomeNewFragment;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.bumptech.glide.Glide;

public class ChapterVersesActivity extends MasterActivity implements
        ChapterVersesFragment.OnBrowseRowListener {

    private OnBackPressedListener onBackPressedListener;

    public static Intent createIntent(Context context, ChapterModel chapterModel) {
        Intent intent = new Intent(context, ChapterVersesActivity.class);
        intent.putExtra("DATA", chapterModel);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verses);

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (onBackPressedListener != null) {
                            onBackPressedListener.doBack();
                        } else {
                            finish();
                        }
                    }
                });
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onItemSelected(Object item, long index) {

    }
}
