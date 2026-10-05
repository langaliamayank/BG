package com.androidtv.bhagavadgita.comman;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import com.androidtv.bhagavadgita.R;

public class DepthPageTransformer implements ViewPager2.PageTransformer {

    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();
        View childLayout = page.findViewById(R.id.childLayout);

        if (position < -1) { // [-Infinity, -1)
            // Page is completely off-screen to the left
            page.setAlpha(0f);

        } else if (position <= 0) { // [-1, 0]
            // Use default slide transition for the page moving left
            page.setAlpha(1f);
            page.setTranslationX(0f);
            page.setScaleX(1f);
            page.setScaleY(1f);

            // Parallax translation for the inner child view
            if (childLayout != null) {
                childLayout.setTranslationX(0f);
            }

        } else if (position <= 1) { // (0, 1]
            // Fade the page out
            page.setAlpha(1f - position);

            // Counteract default slide transition
            page.setTranslationX(pageWidth * -position);

            // Scale the page down (depth effect)
            float scaleFactor = MIN_SCALE + (1f - MIN_SCALE) * (1f - Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            // Optional: Parallax shift on the child element
            if (childLayout != null) {
                childLayout.setTranslationX(pageWidth * 0.3f * -position);
            }

        } else { // (1, +Infinity]
            // Page is completely off-screen to the right
            page.setAlpha(0f);
        }
    }
}
