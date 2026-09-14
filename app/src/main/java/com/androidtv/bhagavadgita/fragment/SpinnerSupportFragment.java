package com.androidtv.bhagavadgita.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.androidtv.bhagavadgita.comman.GradientProgressBar;

public class SpinnerSupportFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        GradientProgressBar progressBar = new GradientProgressBar(container.getContext());

        if (container instanceof FrameLayout) {
            Resources res = getResources();

            int size = res.getDimensionPixelSize(com.intuit.sdp.R.dimen._40sdp);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size, Gravity.CENTER);
            progressBar.setLayoutParams(layoutParams);
        }

        progressBar.startAnimation();

        return progressBar;
    }
}

