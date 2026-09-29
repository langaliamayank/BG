package com.androidtv.bhagavadgita.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.VerticalGridView;

import com.androidtv.bhagavadgita.R;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MasterBrowseFragment extends BrowseSupportFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            prepareEntranceTransition();

        setupUIElements();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int resId = getResources().getIdentifier("scale_frame", "id", view.getContext().getPackageName());
        View rowsContainer = view.findViewById(resId);
        if (rowsContainer != null) {
            int initialPadding = getResources().getDimensionPixelSize(R.dimen.low_padding);
            rowsContainer.setPadding(0, initialPadding, 0, 0);
        }

        view.post(() -> {
            if (!isAdded()) return;
            if (getRowsSupportFragment() != null) {
                VerticalGridView vgv = getRowsSupportFragment().getVerticalGridView();
                if (vgv != null) {

                    vgv.setItemAnimator(null);
                    vgv.setClipChildren(false);
                    vgv.setClipToPadding(false);
                    vgv.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                    vgv.setFocusScrollStrategy(BaseGridView.FOCUS_SCROLL_ALIGNED);
                    vgv.setWindowAlignment(BaseGridView.WINDOW_ALIGN_NO_EDGE);
                    vgv.setWindowAlignmentOffsetPercent(BaseGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED);
//                    vgv.setWindowAlignmentOffset(getResources().getDimensionPixelSize(R.dimen.low_padding));
                    vgv.setItemAlignmentOffset(0);
                    vgv.setItemAlignmentOffsetPercent(0f);
                    vgv.setPruneChild(false);

                    disableClipping(vgv);
                }
            }
        });
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

    private void setupUIElements() {
        setHeadersState(HEADERS_DISABLED);
        setHeadersTransitionOnBackEnabled(false);
    }
}
