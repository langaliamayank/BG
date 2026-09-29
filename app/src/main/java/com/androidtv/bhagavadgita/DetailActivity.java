package com.androidtv.bhagavadgita;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.leanback.app.BackgroundManager;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager.widget.ViewPager;

import com.androidtv.bhagavadgita.comman.FixedSpeedScroller;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.PlayerController;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.fragment.DetailTabFragment;
import com.androidtv.bhagavadgita.model.VersesCache;
import com.androidtv.bhagavadgita.model.VersesModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends MasterActivity {

    public static ViewPager mViewPager;
    private VersesModel mVersesModel;
    private ArrayList<VersesModel> mVersesList;
    private int mSelectedIndex;
    private boolean mContinueWatching;
    public static TabFragmentAdapter mAdapter;
    private Interpolator mInterpolator;

    public static Intent createIntent(Context context, VersesModel versesModel,
                                      int index, boolean watching) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("DATA", versesModel);
        intent.putExtra("INDEX", index);
        intent.putExtra("WATCH", watching);
        return intent;
    }

    @UnstableApi
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mContinueWatching = getIntent().getBooleanExtra("WATCH", false);
        if (mContinueWatching) {
            mVersesList = VersesCache.getInstance().getActiveList();
        } else {
            mVersesList = VersesCache.getInstance().getVerses();
        }

        // Check if list is null or empty
        if (mVersesList == null || mVersesList.isEmpty()) {
            Toast.makeText(this, "No verses available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mVersesModel = (VersesModel) getIntent().getSerializableExtra("DATA");
        mSelectedIndex = getIntent().getIntExtra("INDEX", 0);

        // Clamp mSelectedIndex to valid range
        if (mSelectedIndex < 0 || mSelectedIndex >= mVersesList.size()) {
            mSelectedIndex = 0;
        }

        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mViewPager.setHorizontalScrollBarEnabled(false);
        mViewPager.setVerticalScrollBarEnabled(false);

        mAdapter = new TabFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        List<Fragment> fragmentList = new ArrayList<>();
        for (VersesModel versesModel : mVersesList) {
            fragmentList.add(DetailTabFragment.newInstance(versesModel, mVersesList, mSelectedIndex));
        }

        mAdapter.replaceFragmentList(fragmentList);

        updateNavigationButtons(mSelectedIndex, mAdapter.getCount());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int currentPage = -1;

            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                updateNavigationButtons(position, mAdapter.getCount());

                for (int i = 0; i < mAdapter.getCount(); i++) {
                    Fragment fragment = mAdapter.getItem(i);
                    if (fragment instanceof PlayerController) {
                        if (i == position) {
                            ((PlayerController) fragment).playMedia();
                        } else {
                            ((PlayerController) fragment).pauseMedia();
                        }
                    }
                }

                currentPage = position;
                saveLastWatched(position, mVersesList);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mViewPager.setCurrentItem(mSelectedIndex, true);
        mViewPager.setOffscreenPageLimit(3);

        // Manually trigger first-page playback since onPageSelected
        // won't fire for the initial position on some ViewPager versions
        mViewPager.post(() -> {
            if (mAdapter != null && mSelectedIndex >= 0 && mSelectedIndex < mAdapter.getCount()) {
                Fragment first = mAdapter.getItem(mSelectedIndex);
                if (first instanceof PlayerController) {
                    ((PlayerController) first).playMedia();
                }
            }
        });

        saveLastWatched(mViewPager.getCurrentItem(), mVersesList);

        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mViewPager.getContext(), mInterpolator);
            scroller.setFriction(100);
            mScroller.set(mViewPager, scroller);
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }

    private void updateNavigationButtons(int position, int totalCount) {
        if (totalCount <= 1) {
            // Only 1 item (e.g., Verse of the Day): hide both
            findViewById(R.id.imagePrev).setVisibility(View.GONE);
            findViewById(R.id.imageNext).setVisibility(View.GONE);
            return;
        }

        // Previous Button: hide at 0, show otherwise
        if (position == 0) {
            findViewById(R.id.imagePrev).setVisibility(View.GONE);
        } else {
            findViewById(R.id.imagePrev).setVisibility(View.VISIBLE);
        }

        // Next Button: hide at last index, show otherwise
        if (position == totalCount - 1) {
            findViewById(R.id.imageNext).setVisibility(View.GONE);
        } else {
            findViewById(R.id.imageNext).setVisibility(View.VISIBLE);
        }
    }

    public class TabFragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();

        public TabFragmentAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        public void replaceFragmentList(List<Fragment> newFragments) {
            fragmentList.clear();
            fragmentList.addAll(newFragments);
            notifyDataSetChanged();
        }
    }

    private void saveLastWatched(int position, ArrayList<VersesModel> list) {
        if (mContinueWatching == true) {
            SharePreferenceManager.save("KEY_LAST_VERSE_LIST", list);
            SharePreferenceManager.save("KEY_LAST_VERSE_POSITION", position);
            LogTag.e("Saved Continue Watching at index: " + position);
        }
    }
}
