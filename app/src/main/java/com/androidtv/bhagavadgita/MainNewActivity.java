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
import android.graphics.drawable.ColorDrawable;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.comman.BackgroundImageUtils;
import com.androidtv.bhagavadgita.comman.GlideHelper;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.MyApplication;
import com.androidtv.bhagavadgita.comman.NavigableFragment;
import com.androidtv.bhagavadgita.comman.NavigationDrawerItem;
import com.androidtv.bhagavadgita.fragment.DarshanFragment;
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

public class MainNewActivity extends MasterActivity implements
        HomeNewFragment.OnBrowseRowListener,
        DarshanFragment.OnBrowseRowListener,
        GitaFragment.OnBrowseRowListener,
        MusicFragment.OnBrowseRowListener {

    private static int selectedIndex = 0;
    public static VerticalGridView verticalGridView;
    private BrowseSupportFragment mBrowseFragment;
    private LinearLayout relativeLayout;
    private static final int CONTENT_IMAGE_CROSS_FADE_DURATION = 1000;
    private ImageView mContentImage;
    private NavigationAdapter navigationAdapter;
    private View mMainFrame;
    private Drawable mBackgroundWithPreview;
    Bitmap background;
    int themeColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        relativeLayout = findViewById(R.id.drawerContainer);
        verticalGridView = findViewById(R.id.verticalGridView);

        // 1. Initialize Adapter FIRST before any fragment switches or drawer toggles
        navigationAdapter = new NavigationAdapter(MainNewActivity.this, NavigationDrawerItem.getData());
        verticalGridView.setAdapter(navigationAdapter);

        // 2. Setup GridView properties
        verticalGridView.setItemAlignmentOffset(0);
        verticalGridView.setWindowAlignmentOffsetPercent(25f);
        verticalGridView.setSelectedPosition(selectedIndex);
        verticalGridView.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_HIGH_EDGE);
        verticalGridView.setSaveChildrenPolicy(BaseGridView.SAVE_ALL_CHILD);

        verticalGridView.post(() -> {
            View view1 = verticalGridView.getChildAt(selectedIndex);
            if (view1 != null) {
                view1.performClick();
            }
        });

        // 3. Load initial fragment and metadata safely now that adapter is ready
        if (savedInstanceState == null) {
            switchFragment(new HomeNewFragment());
        }

//        applyDarshanTheme();
        showMetadata();
        toggleDrawer(false);


    }

//    private void applyDarshanTheme() {
//        DarshanModel active = MyApplication.getDarshanTheme();
//        LogTag.e("active " + active.toString());
//
//        if (active != null && active.getBackground() != null) {
//            try {
//                themeColor = Color.parseColor(active.getBackground());
//            } catch (IllegalArgumentException e) {
//                themeColor = Color.WHITE;
//            }
//            LogTag.e("color " + themeColor + " ==> " + active.getBackground());
//
//            // UI Views ko update karein (e.g. background set karein)
////            findViewById(R.id.root_layout).setBackgroundColor(themeColor);
//        } else {
//            LogTag.e("darshanList not ready yet - using default theme color");
//        }
//    }

public Bitmap getBG(String colorHex) {
    // 1. Safe Color Parsing
    int parsedColor;
    try {
        if (colorHex == null || colorHex.trim().isEmpty()) {
            parsedColor = Color.WHITE;
        } else {
            String formattedHex = colorHex.trim();
            if (!formattedHex.startsWith("#")) {
                formattedHex = "#" + formattedHex;
            }
            parsedColor = Color.parseColor(formattedHex);
        }
    } catch (IllegalArgumentException e) {
        LogTag.e("Invalid color format: " + colorHex + ", fallback to WHITE");
        parsedColor = Color.WHITE;
    }

    // 2. Modern Screen Dimensions Calculation
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

    int imageWidth = (int) getResources().getDimension(R.dimen.content_image_width);
    int imageHeight = (int) getResources().getDimension(R.dimen.content_image_height);
    int gradientSize = (int) getResources().getDimension(R.dimen.content_image_gradient_size);

    LogTag.e("getBG " + colorHex);
    return BackgroundImageUtils.createBackgroundWithPreviewWindow(
            screenWidth, screenHeight, imageWidth, imageHeight, gradientSize, parsedColor);
}

    private void showMetadata() {
        mContentImage = findViewById(R.id.content_image);

//        Display display = getWindowManager().getDefaultDisplay();
//        Point windowSize = new Point();
//        display.getSize(windowSize);
//        int imageWidth = (int) getResources().getDimension(R.dimen.content_image_width);
//        int imageHeight = (int) getResources().getDimension(R.dimen.content_image_height);
//        int gradientSize = (int) getResources().getDimension(R.dimen.content_image_gradient_size);

//        Bitmap newBackground = BackgroundImageUtils.createBackgroundWithPreviewWindow(
//                windowSize.x, windowSize.y, imageWidth, imageHeight, gradientSize,
//                themeColor);
//
//        if (newBackground != null) {
//            background = newBackground;
//            mBackgroundWithPreview = new BitmapDrawable(getResources(), background);
//            mMainFrame = findViewById(R.id.main_frame);
//            mMainFrame.setBackground(mBackgroundWithPreview);
//        }
    }

    private String lastLoadedColor = null;

    @Override
    public void onItemSelected(Object object, long index) {
        if (object == null) return;

        String imagePath = getImagePath(object, true);

        if (object instanceof DarshanModel) {
            DarshanModel darshan = (DarshanModel) object;
            String currentColor = darshan.getBackground();

            // Agar color same hai, to redraw mat karo (Blinking ruk jayegi)
            if (currentColor != null && currentColor.equals(lastLoadedColor)) {
                return;
            }

            lastLoadedColor = currentColor;
            Bitmap bmp = getBG(currentColor);

            if (bmp != null) {
                if (mMainFrame == null) {
                    mMainFrame = findViewById(R.id.main_frame);
                }
                mBackgroundWithPreview = new BitmapDrawable(getResources(), bmp);
                if (mMainFrame != null) {
                    mMainFrame.setBackground(mBackgroundWithPreview);
                }
            }
        }

        if (imagePath != null) {
            GlideHelper.loadWithCinematicZoom(
                    this, mContentImage, imagePath,
                    CONTENT_IMAGE_CROSS_FADE_DURATION,
                    R.drawable.ic_action_noimage_land
            );

            mMainFrame.setBackground(mBackgroundWithPreview);

        } else if (object instanceof PushtimargModel) {
            PushtimargModel model = (PushtimargModel) object;

            Bitmap bmp = getBG("#000000");
            if (bmp != null) {
                mBackgroundWithPreview = new BitmapDrawable(getResources(), bmp);
                mMainFrame = findViewById(R.id.main_frame);
                mMainFrame.setBackground(mBackgroundWithPreview);
            }

            Glide.with(this)
                    .load(getImage(model.getImage()))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mContentImage);
            mMainFrame.setBackground(mBackgroundWithPreview);

        } else if (object instanceof FestivalModel) {
            Bitmap bmp = getBG("#000000");
            if (bmp != null) {
                mBackgroundWithPreview = new BitmapDrawable(getResources(), bmp);
                mMainFrame = findViewById(R.id.main_frame);
                mMainFrame.setBackground(mBackgroundWithPreview);
            }

            Glide.with(this)
                    .load(R.drawable.utsav)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mContentImage);
            mMainFrame.setBackground(mBackgroundWithPreview);

        } else if (object instanceof ActionModel) {
            Bitmap bmp = getBG("#000000");
            if (bmp != null) {
                mBackgroundWithPreview = new BitmapDrawable(getResources(), bmp);
                mMainFrame = findViewById(R.id.main_frame);
                mMainFrame.setBackground(mBackgroundWithPreview);
            }

            Glide.with(this)
                    .load(R.drawable.utsav)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mContentImage);
            mMainFrame.setBackground(mBackgroundWithPreview);

        }  else {
            mMainFrame.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationViewHolder> {

        private List<NavigationDrawerItem> mList;
        private MainNewActivity mContext;
        private boolean menuExpanded = false;

        public NavigationAdapter(MainNewActivity context, List<NavigationDrawerItem> listItems) {
            mContext = context;
            mList = listItems;
        }

        @Override
        public NavigationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.card_item_nav, viewGroup, false);
            return new NavigationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NavigationViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
            NavigationDrawerItem item = mList.get(position);
            viewHolder.textView.setText(item.getTitle());
            viewHolder.imageView.setImageResource(item.getImageId());

            boolean isValidItem = !(item.getTitle().equals("") && item.getImageId() == 0);
            viewHolder.itemView.setFocusable(isValidItem);
            viewHolder.itemView.setFocusableInTouchMode(isValidItem);

            if (position == selectedIndex) {
                viewHolder.selector.setVisibility(View.VISIBLE);
//                viewHolder.glow.setBackgroundResource(R.drawable.glow_background);
            } else {
                viewHolder.selector.setVisibility(View.INVISIBLE);
//                viewHolder.glow.setBackgroundResource(0);
            }

            viewHolder.textView.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorTextSecondary)));
            viewHolder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                int colorRes = hasFocus ? R.color.colorTextPrimary : R.color.colorTextSecondary;
                viewHolder.textView.setTextColor(ColorStateList.valueOf(getResources().getColor(colorRes)));
                viewHolder.textView.setTypeface(hasFocus ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                viewHolder.imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, colorRes)));
            });

            viewHolder.itemView.setOnClickListener(view -> {
                if (position == 0) {
                    switchFragment(new HomeNewFragment());
                } else if (position == 1) {
//                    switchFragment(new DarshanFragment());

                    Intent intent = new Intent(MainNewActivity.this, CalendarActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } else if (position == 2) {
                    switchFragment(new GitaFragment());
                } else if (position == 3) {
                    switchFragment(new MusicFragment());
                } else if (position == 4) {
                    Toast.makeText(mContext, "Notification", Toast.LENGTH_SHORT).show();
                } else if (position == 5) {
                    Intent intent = new Intent(MainNewActivity.this, MySettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

                if (position != 5 && relativeLayout != null) {
                    relativeLayout.getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.navigation_drawer_collapsed_width);
                    relativeLayout.requestLayout();
                    relativeLayout.clearFocus();
                }

                selectedIndex = position;
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        class NavigationViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;
            private ImageView imageView;
            private ImageView selector;
            private RelativeLayout glow;

            public NavigationViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.nav_title);
                imageView = itemView.findViewById(R.id.nav_icon);
                selector = itemView.findViewById(R.id.nav_selector);
                glow = itemView.findViewById(R.id.nav_glow);
            }
        }

        public void setMenuExpanded(boolean menuExpanded) {
            LogTag.e("setMenuExpanded: " + menuExpanded);
            this.menuExpanded = menuExpanded;
        }
    }

    public void switchFragment(BrowseSupportFragment fragment) {
        if (mContentImage != null) {
            GlideHelper.clearImage(mContentImage);
        }

        if (relativeLayout != null) {
            toggleDrawer(false);
        }

        mBrowseFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mBrowseFragment)
                .commit();
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
                                toggleDrawer(true);
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
                    toggleDrawer(false);
                    return true;
                }
                break;
        }

        return super.dispatchKeyEvent(event);
    }

    private void toggleDrawer(boolean expand) {
        if (navigationAdapter != null) {
            navigationAdapter.setMenuExpanded(expand);
            navigationAdapter.notifyDataSetChanged();
        }

        int targetWidth = expand ?
                getResources().getDimensionPixelSize(R.dimen.navigation_drawer_expanded_width) :
                getResources().getDimensionPixelSize(R.dimen.navigation_drawer_collapsed_width);

        if (relativeLayout != null) {
            relativeLayout.getLayoutParams().width = targetWidth;
            relativeLayout.requestLayout();

            if (expand) {
                relativeLayout.post(() -> verticalGridView.requestFocus());
            } else {
                relativeLayout.clearFocus();
                if (mBrowseFragment instanceof NavigableFragment) {
                    View firstPoster = ((NavigableFragment) mBrowseFragment).getmFirstPosterView();
                    if (firstPoster != null) firstPoster.requestFocus();
                }
            }
        }
    }

    @Override
    public void handleOnBack() {
        boolean hasBack = false;
        try {
            if (findViewById(R.id.container).isShown()) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                if (fragment instanceof HomeNewFragment) {
                    HomeNewFragment mBrowseFragment = (HomeNewFragment) fragment;
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
                                mBrowseFragment.setFocusOnFirstPoster();
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
}