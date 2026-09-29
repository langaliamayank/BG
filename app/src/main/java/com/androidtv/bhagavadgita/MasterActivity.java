package com.androidtv.bhagavadgita;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.BackgroundManager;

import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.MyApplication;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.model.HistoryModel;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.model.VallabhacharyaModel;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.conscrypt.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class MasterActivity extends FragmentActivity {
    public static int selectedPosition = 0;
    private BackgroundManager backgroundManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable backgroundRunnable;
    private static final int BACKGROUND_UPDATE_DELAY_MS = 200;

    public static String getUserAgent() {
        String userAgent = "{app_name}/{app_version} ({os_name} {os_version}; {device_brand} {device_model})";
        Context context = MyApplication.getInstance().getApplicationContext();
        userAgent = userAgent.replace("{device_brand}", Build.BRAND)
                .replace("{device_model}", Build.MODEL)
                .replace("{app_name}", context.getString(R.string.app_name));
        return userAgent;
    }


    public String getImagePath(Object object, boolean isLandscape) {
        if (object instanceof HistoryModel) {
            return APIInterface.OTHER_IMAGE_BASE_PATH + ((HistoryModel) object).getImage() + ".jpeg?raw=true";
        }
        if (object instanceof VersesModel) {
            return APIInterface.CHAPTER_IMAGE_NUMBER_BASE_PATH + ((VersesModel) object).getChapterNumber() + ".jpg?raw=true";
        }
        if (object instanceof ChapterModel) {
            return APIInterface.CHAPTER_IMAGE_BASE_PATH + ((ChapterModel) object).getImageName() + ".jpg?raw=true";
        }
        if (object instanceof DarshanModel) {
            if (isLandscape)
                return APIInterface.DARSHAN_IMAGE_LAND_BASE_PATH + ((DarshanModel) object).getImage() + ".jpeg?raw=true";
            else
                return APIInterface.DARSHAN_IMAGE_BASE_PATH + ((DarshanModel) object).getImage() + ".png?raw=true";
        }

        if (object instanceof VallabhacharyaModel) {
            return APIInterface.OTHER_IMAGE_BASE_PATH + ((VallabhacharyaModel) object).getImage() + ".jpeg?raw=true";
        }

        if (object instanceof PushtimargModel){
            return ((PushtimargModel) object).getImage();
        }

        return null;
    }

    public Drawable getImage(String path) {
        int resId = getResources().getIdentifier(path, "drawable", getPackageName());
        return ContextCompat.getDrawable(MasterActivity.this, resId);
    }

    public static String loadJSONFromAsset(Context context, String path) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Step 1: Initialize BackgroundManager using 'this' activity context
        backgroundManager = BackgroundManager.getInstance(this);
        if (!backgroundManager.isAttached()) {
            backgroundManager.attach(getWindow());
        }

        // Set a fallback background
        updateBackgroundColorDelayed(SharePreferenceManager.getString("KEY_THEME_COLOR"));

        View rootView = getWindow().getDecorView().getRootView();
        rootView.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                String oldName = (oldFocus != null) ? oldFocus.getClass().getSimpleName() + " (Id: " + oldFocus.getId() + ")" : "NULL";
                String newName = (newFocus != null) ? newFocus.getClass().getSimpleName() + " (Id: " + newFocus.getId() + ")" : "NULL";
//                LogTag.e("GlobalFocusDebug " + "Focus shifted FROM: " + oldName + " --> TO: " + newName);
            }
        });
    }

    public void updateBackgroundColorDelayed(final String hexColor) {
        if (backgroundRunnable != null) {
            handler.removeCallbacks(backgroundRunnable);
        }

        backgroundRunnable = new Runnable() {
            @Override
            public void run() {
                if (hexColor != null && !hexColor.isEmpty()) {
                    try {
                        int color = Color.parseColor(hexColor);
                        getWindow().setBackgroundDrawable(new ColorDrawable(color));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        };

        handler.postDelayed(backgroundRunnable, BACKGROUND_UPDATE_DELAY_MS);
    }

    // Step 3: Prevent memory leaks when the Activity finishes
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundRunnable != null) {
            handler.removeCallbacks(backgroundRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        PlayerManager playerManager = PlayerManager.getInstance(this);
//        if (playerManager != null && playerManager.getPlayer() != null) {
//            playerManager.getPlayer().pause();
//            playerManager.getPlayer().setPlayWhenReady(false);
//        }
    }
}
