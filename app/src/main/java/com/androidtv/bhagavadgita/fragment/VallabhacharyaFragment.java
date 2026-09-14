package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.VerticalGridView;

import com.androidtv.bhagavadgita.CommanActivity;
import com.androidtv.bhagavadgita.HomeDetailActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.HeaderView;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.comman.RowHeaderItem;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.model.VallabhacharyaModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.androidtv.bhagavadgita.presenter.MyListRowPresenter;
import com.androidtv.bhagavadgita.presenter.PushtimargPresenter;
import com.androidtv.bhagavadgita.presenter.VallabhacharyaPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class VallabhacharyaFragment extends BrowseSupportFragment implements OnBackPressedListener {

    private ArrayObjectAdapter mRowsAdapter;
    private OnBrowseRowListener mCallback;
    private HeaderView headerView;

    private static final String SPINNER_TAG = "LoadingOverlay";
    private SpinnerSupportFragment mSpinnerFragment;

//    /*For Background*/
//    private BackgroundManager backgroundManager;
//    private final Handler handler = new Handler(Looper.getMainLooper());
//    private Runnable backgroundRunnable;
//    private static final int BACKGROUND_UPDATE_DELAY_MS = 300;

    @Override
    public void doBack() {
        try {
            int selectedRowPosition = getRowsSupportFragment().getSelectedPosition();

            MyListRowPresenter.ViewHolder selectedRow = (MyListRowPresenter.ViewHolder) getRowsSupportFragment().getRowViewHolder(selectedRowPosition);
            int selectedItemPosition = selectedRow.getSelectedPosition();

            if (selectedItemPosition == 0) {
                ((CommanActivity) getActivity()).switchFragment(new DashboardFragment());

            } else {
                getRowsSupportFragment().setSelectedPosition(selectedRowPosition, true,
                        new MyListRowPresenter.SelectItemViewHolderTask(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnBrowseRowListener {
        void onItemSelected(Object item, long index);
    }

    public void disableClipping(View view) {
        if (view == null) return;
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
        }
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            disableClipping((View) parent);
        }
    }

    @Override
    public View onInflateTitleView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        headerView = new HeaderView(inflater.getContext(), false, getActivity().getWindowManager());
        return headerView;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        // Step 1: Initialize BackgroundManager
//        backgroundManager = BackgroundManager.getInstance(getActivity());
//        backgroundManager.attach(getActivity().getWindow());
//
//        // Set a fallback background
//        updateBackgroundColorDelayed(SharePreferenceManager.getString("KEY_THEME_COLOR"));

        int resId = view.getContext().getResources().getIdentifier("scale_frame", "id", view.getContext().getPackageName());
        View rowsContainer = view.findViewById(resId);
        if (rowsContainer != null) {
            int padding = getResources().getDimensionPixelSize(R.dimen.content_image_height);
            rowsContainer.setPadding(0, padding, 0, 0);
        }

        prepareEntranceTransition();
        new Handler(Looper.getMainLooper()).postDelayed(this::startEntranceTransition, Constants.INTERVAL);

        view.post(() -> {
            if (getRowsSupportFragment() != null) {
                VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
                if (vgv != null) {
                    vgv.setItemAnimator(null);
                    vgv.setClipChildren(false);
                    vgv.setClipToPadding(false);
                    vgv.setFocusScrollStrategy(BaseGridView.FOCUS_SCROLL_ALIGNED);
                    vgv.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_NO_EDGE);
                    vgv.setWindowAlignmentOffsetPercent(0f);
                    disableClipping(vgv);
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupUIElements();
        setupRowAdapter();
        setupEventListeners();
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        if (backgroundRunnable != null) {
//            handler.removeCallbacks(backgroundRunnable);
//        }
//    }
//
//    private void updateBackgroundColorDelayed(final String hexColor) {
//        if (backgroundRunnable != null) {
//            handler.removeCallbacks(backgroundRunnable);
//        }
//
//        backgroundRunnable = () -> {
//            if (getActivity() == null || hexColor == null || hexColor.isEmpty()) return;
//
//            try {
//                int color = Color.parseColor(hexColor);
//
//                // 1. setColor handles solid colors reliably in Leanback
//                if (backgroundManager != null && backgroundManager.isAttached()) {
//                    backgroundManager.setColor(color);
//                }
//
//                // 2. Set directly on the window to prevent Leanback from blanking it out
//                getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(color));
//
//            } catch (IllegalArgumentException ignored) {
//            }
//        };
//
//        handler.postDelayed(backgroundRunnable, BACKGROUND_UPDATE_DELAY_MS);
//    }

    private void showLoader() {
        if (getParentFragmentManager().findFragmentByTag(SPINNER_TAG) != null) return;
        mSpinnerFragment = new SpinnerSupportFragment();
        getParentFragmentManager().beginTransaction()
                .add(android.R.id.content, mSpinnerFragment, SPINNER_TAG)
                .commitAllowingStateLoss();
    }

    private void hideLoader() {
        if (!isAdded()) return;
        Fragment fragment = getParentFragmentManager().findFragmentByTag(SPINNER_TAG);
        if (fragment != null) {
            getParentFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
            mSpinnerFragment = null;
        }
    }

    private void updateHeaderVisibility(boolean show) {
        if (headerView == null) return;

        float targetAlpha = show ? 1f : 0f;
        headerView.animate()
                .alpha(targetAlpha)
                .setDuration(100)
                .start();

        if (!isAdded()) return;

        Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        View contentImage = activity.findViewById(R.id.content_image);
        if (contentImage != null) {
            contentImage.animate()
                    .alpha(targetAlpha)
                    .setDuration(100)
                    .start();
        }

        ImageView mainActivityImageView = getActivity().findViewById(R.id.content_image);
        View rowsContainer = getView().findViewById(androidx.leanback.R.id.scale_frame);
        if (rowsContainer != null) {
            int padding_high = getResources().getDimensionPixelSize(R.dimen.content_image_height);
            int padding_low = getResources().getDimensionPixelSize(R.dimen.low_padding);
            rowsContainer.setPadding(0, show ? padding_high : padding_low, 0, 0);
            mainActivityImageView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (mCallback != null && row != null) {
                mCallback.onItemSelected(item, row.getHeaderItem().getId());
            }

            if (row instanceof ListRow) {
                ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                        .getAdapter());
                int selectedItemPosition = adapter.indexOf(item);

                if (itemViewHolder != null) itemViewHolder.view.setTag(selectedItemPosition);
            }

            if (item instanceof Object && row != null) {
                Object data = (Object) item;

                // 1. Update the Data
                if (headerView != null) {
                    HeaderItem header = row.getHeaderItem();
                    if (header instanceof RowHeaderItem) {
                        List<Object> childList = ((RowHeaderItem) header).getList();
                        headerView.setData(data, childList, ((RowHeaderItem) header));

                        headerView.post(() -> {
                            updateHeaderVisibility(true);
                        });
                    }
                }
            }
        });

        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (!(item instanceof Object) || !(row instanceof ListRow)) return;

            if (item instanceof VallabhacharyaModel) {
                VallabhacharyaModel vallabhacharyaModel = (VallabhacharyaModel) item;
                startActivity(HomeDetailActivity.createIntent(getActivity(), vallabhacharyaModel, 2));
            }
        });
    }

    private void setupRowAdapter() {
        showLoader();

        MyListRowPresenter selector = new MyListRowPresenter((MasterActivity) getActivity(), 0);
        mRowsAdapter = new ArrayObjectAdapter(selector);
        setAdapter(mRowsAdapter);

        new Thread(() -> {
            try {
                Thread.sleep(Constants.INTERVAL);

                createVallabhacharya();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createVallabhacharya() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getVallabhacharya();
        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                hideLoader();

                VallabhacharyaPresenter vallabhacharyaPresenter = new VallabhacharyaPresenter((MasterActivity) getActivity());
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(vallabhacharyaPresenter);

                try {
                    List<VallabhacharyaModel> vallabhacharyaModelList = new Gson().fromJson(response, new TypeToken<List<VallabhacharyaModel>>() {
                    }.getType());
                    for (VallabhacharyaModel vallabhacharyaModel : vallabhacharyaModelList) {
                        listRowAdapter.add(vallabhacharyaModel);
                    }

                    RowHeaderItem cardPresenterHeader = new RowHeaderItem(0, "Shrinathji", Collections.singletonList(vallabhacharyaModelList));
                    cardPresenterHeader.setDescription("Vallabhacharya");
                    mRowsAdapter.add(new ListRow(cardPresenterHeader, listRowAdapter));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private void setupUIElements() {
        setHeadersState(HEADERS_DISABLED);
        setHeadersTransitionOnBackEnabled(false);
        if (getActivity() instanceof OnBrowseRowListener) {
            mCallback = (OnBrowseRowListener) getActivity();
        } else {
            throw new ClassCastException(getActivity().toString() + " must implement OnBrowseRowListener");
        }
    }
}