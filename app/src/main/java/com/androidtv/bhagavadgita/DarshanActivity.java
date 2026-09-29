package com.androidtv.bhagavadgita;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.bumptech.glide.Glide;

public class DarshanActivity extends MasterActivity {

    private DarshanModel mDarshanModel;

    public static Intent createIntent(Context context, DarshanModel darshanModel) {
        Intent intent = new Intent(context, DarshanActivity.class);
        intent.putExtra("DATA", darshanModel);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_darshan);

        mDarshanModel = (DarshanModel) getIntent().getSerializableExtra("DATA");
        if (mDarshanModel != null) {

            ((TextView) findViewById(R.id.textDarshanInfo)).setText(mDarshanModel.getDescription());
            ((TextView) findViewById(R.id.textDarshanStatus)).setText(mDarshanModel.getStatus());
            ((TextView) findViewById(R.id.textDarshanTime)).setText(mDarshanModel.getStartTime().equalsIgnoreCase("") ? "-" : mDarshanModel.getStartTime() + " to " + mDarshanModel.getEndTime());

            ((TextView) findViewById(R.id.textDarshanName)).setText(mDarshanModel.getTitle() + "/" + mDarshanModel.getTitleHi());
            ((TextView) findViewById(R.id.textDarshanMessage)).setText(mDarshanModel.getMessage() + "\n\n" + mDarshanModel.getMessageHi());

            Glide.with(DarshanActivity.this)
                    .load(getImagePath(mDarshanModel, false))
                    .into(((ImageView) findViewById(R.id.imageChapter)));
        }
    }
}
