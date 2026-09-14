package com.androidtv.bhagavadgita.comman;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.androidtv.bhagavadgita.R;

public class DepthPageTransformer implements ViewPager.PageTransformer {

    private final float imageVelocityFactor = 1.2f;
    private final float metaVelocityFactor = 1.5f;

    @Override
    public void transformPage(View page, float position) {
        int width = page.getWidth();

        // Find your inner views similar to SonyLIV's ViewHolder setup
//        View imageView = page.findViewById(R.id.childLayout); // or spotlight_main
//        View metadataView = page.findViewById(R.id.advertisementMetadata);

        if (position < -1.0f || position > 1.0f) {
            // Page is way offscreen
            page.setAlpha(1.0f);
        } else {
            float pos = -position;
            float wid = width;

            // 1. Parallax shift for the background image
//            if (imageView != null) {
//                float imageTranslation = (wid / imageVelocityFactor) * pos;
//                imageView.setTranslationX(imageTranslation);
//            }

            // 2. Parallax shift for text/metadata
//            if (metadataView != null) {
//                float metaTranslation = (wid / metaVelocityFactor) * pos;
//                metadataView.setTranslationX(metaTranslation);
//
//                // Fade out metadata as it moves away from center
//                float alphaFactor = 1.0f - Math.abs(position);
//                metadataView.setAlpha(alphaFactor);
//            }
        }
    }
}
