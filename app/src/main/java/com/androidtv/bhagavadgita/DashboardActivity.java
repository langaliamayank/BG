package com.androidtv.bhagavadgita;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.comman.BackgroundImageUtils;
import com.androidtv.bhagavadgita.comman.GlideHelper;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.NavigableFragment;
import com.androidtv.bhagavadgita.comman.NavigationDrawerItem;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.fragment.DarshanFragment;
import com.androidtv.bhagavadgita.fragment.DashboardFragment;
import com.androidtv.bhagavadgita.fragment.GitaFragment;
import com.androidtv.bhagavadgita.fragment.HomeNewFragment;
import com.androidtv.bhagavadgita.fragment.MusicFragment;
import com.androidtv.bhagavadgita.model.ActionModel;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.presenter.MyListRowPresenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class DashboardActivity extends MasterActivity {

    private BrowseSupportFragment mBrowseFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (savedInstanceState == null) {
            switchFragment(new DashboardFragment());
        }
    }

    public void switchFragment(BrowseSupportFragment fragment) {
        mBrowseFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mBrowseFragment)
                .commit();
    }

    @Override
    public void handleOnBack() {
        boolean hasBack = false;
        try {
            if (findViewById(R.id.container).isShown()) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                if (fragment instanceof DashboardFragment) {
                    DashboardFragment mBrowseFragment = (DashboardFragment) fragment;
                    int selectedRowPosition = mBrowseFragment.getRowsSupportFragment().getSelectedPosition();

                    MyListRowPresenter.ViewHolder selectedRow = (MyListRowPresenter.ViewHolder) mBrowseFragment.getRowsSupportFragment().getRowViewHolder(selectedRowPosition);
                    int selectedItemPosition = selectedRow.getSelectedPosition();

                    if (selectedItemPosition == 0) {
                        hasBack = false;
                    } else {
                        hasBack = true;
                        mBrowseFragment.getRowsSupportFragment().setSelectedPosition(selectedRowPosition, true,
                                new MyListRowPresenter.SelectItemViewHolderTask(0));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                mBrowseFragment.setFocusOnFirstPoster();
                            }
                        }, 200);
                    }
                }
            }

        } catch (Exception e) {
            LogTag.e(e + "");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
            }
        }, 500);

        if (!hasBack) {

        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return super.dispatchKeyEvent(event);

        int keyCode = event.getKeyCode();
        View currentFocus = getCurrentFocus();

        boolean isFocusInNav = (currentFocus != null &&
                (currentFocus.getId() == R.id.navigationItem ||
                        currentFocus.getId() == R.id.verticalGridView));

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (isFocusInNav) {
                    return true;
                }

                // Check if we are currently inside HomeNewFragment
                if (mBrowseFragment instanceof BrowseSupportFragment) {
                    try {
                        BrowseSupportFragment fragment = (BrowseSupportFragment) mBrowseFragment;
                        if (fragment.getRowsSupportFragment() != null) {
                            MyListRowPresenter.ViewHolder rowVh = (MyListRowPresenter.ViewHolder)
                                    fragment.getRowsSupportFragment()
                                            .getRowViewHolder(fragment.getRowsSupportFragment().getSelectedPosition());

                            if (rowVh != null && rowVh.getSelectedPosition() == 0) {
//                                toggleDrawer(true);
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        LogTag.e(e.toString());
                    }
                }

                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (isFocusInNav) {
//                    toggleDrawer(false);
                    return true;
                }
                break;
        }

        return super.dispatchKeyEvent(event);
    }
}