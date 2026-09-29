package com.androidtv.bhagavadgita;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;

import com.androidtv.bhagavadgita.comman.BackgroundImageUtils;
import com.androidtv.bhagavadgita.comman.GlideHelper;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.fragment.DarshanFragment;
import com.androidtv.bhagavadgita.fragment.DashboardFragment;
import com.androidtv.bhagavadgita.fragment.HistoryFragment;
import com.androidtv.bhagavadgita.fragment.HomeFragment;
import com.androidtv.bhagavadgita.fragment.MusicFragment;
import com.androidtv.bhagavadgita.fragment.PushtimargFragment;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class DashboardActivity extends MasterActivity  implements HomeFragment.OnBrowseRowListener{

    private BrowseSupportFragment mBrowseFragment;

    private static final int CONTENT_IMAGE_CROSS_FADE_DURATION = 1000;
    private ImageView mContentImage;
    private View mMainFrame;
    private Drawable mBackgroundWithPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mContentImage = findViewById(R.id.content_image);

        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
        }
    }

    public void switchFragment(BrowseSupportFragment fragment) {
        mBrowseFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mBrowseFragment)
                .commit();
    }

    @Override
    public void onItemSelected(Object object, long index) {
        if (object == null) return;

        // VersesModel on Continue Watching / Verses of the Day does NOT show preview window
        boolean showPreview = !(object instanceof DarshanModel);

        String color;
        if (object instanceof DarshanModel) {
            color = ((DarshanModel) object).getBackground();
        } else {
            color = SharePreferenceManager.getString("KEY_THEME_COLOR");
        }

        if (mMainFrame == null) {
            mMainFrame = findViewById(R.id.main_frame);
        }

        // Pass false to clear the top-right cutout shadow
        Bitmap background = getBG(color, showPreview);
        if (background != null) {
            mBackgroundWithPreview = new BitmapDrawable(getResources(), background);
            mMainFrame.setBackground(mBackgroundWithPreview);
        }

        if (!showPreview) {
            // Clear or hide content image
            if (mContentImage != null) {
                Glide.with(this).clear(mContentImage);
                mContentImage.setVisibility(View.GONE);
            }
            return;
        }

        if (mContentImage != null) {
            mContentImage.setVisibility(View.VISIBLE);
        }

        String imagePath = getImagePath(object, true);
        if (imagePath == null || imagePath.isEmpty()) {
            LogTag.e("Image path is null or empty");
            return;
        }

        if (object instanceof PushtimargModel) {
            Glide.with(this)
                    .load(getImage(((PushtimargModel) object).getImage()))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mContentImage);
        } else {
            GlideHelper.loadWithCinematicZoom(
                    this,
                    mContentImage,
                    imagePath,
                    CONTENT_IMAGE_CROSS_FADE_DURATION,
                    R.drawable.ic_action_noimage_land
            );
        }
    }

    public Bitmap getBG(String colorHex, boolean hasPreviewWindow) {
        int parsedColor;
        try {
            if (colorHex == null || colorHex.trim().isEmpty()) {
                parsedColor = Color.parseColor("#00000000"); // or your default theme color
            } else {
                String formattedHex = colorHex.trim();
                if (!formattedHex.startsWith("#")) {
                    formattedHex = "#" + formattedHex;
                }
                parsedColor = Color.parseColor(formattedHex);
            }
        } catch (IllegalArgumentException e) {
            parsedColor = Color.parseColor("#00000000");
        }

        // Modern Screen Dimensions Calculation
        int screenWidth;
        int screenHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            Rect bounds = windowMetrics.getBounds();
            screenWidth = bounds.width();
            screenHeight = bounds.height();
        } else {
            Display display = getWindowManager().getDefaultDisplay();
            Point windowSize = new Point();
            display.getSize(windowSize);
            screenWidth = windowSize.x;
            screenHeight = windowSize.y;
        }

        // IF NO PREVIEW NEEDED: Return flat background bitmap without cutout
        if (!hasPreviewWindow) {
            Bitmap flatBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
            flatBitmap.eraseColor(parsedColor);
            return flatBitmap;
        }

        // WITH PREVIEW: Draw the cutout for rows that show header & preview image
        int imageWidth = (int) getResources().getDimension(R.dimen.content_image_width);
        int imageHeight = (int) getResources().getDimension(R.dimen.content_image_height);
        int gradientSize = (int) getResources().getDimension(R.dimen.content_image_gradient_size);

        return BackgroundImageUtils.createBackgroundWithPreviewWindow(
                screenWidth, screenHeight, imageWidth, imageHeight, gradientSize, parsedColor);
    }
}