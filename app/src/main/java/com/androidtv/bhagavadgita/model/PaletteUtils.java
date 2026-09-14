package com.androidtv.bhagavadgita.model;

import android.graphics.Color;

import androidx.palette.graphics.Palette;

public class PaletteUtils {

    public static PaletteColors getPaletteColors(Palette palette) {
        PaletteColors colors = new PaletteColors();

        //figuring out toolbar palette color in order of preference
        if (palette.getDarkVibrantSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getDarkVibrantSwatch().getRgb());
            colors.setTextColor(palette.getDarkVibrantSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getDarkVibrantSwatch().getTitleTextColor());

        } else if (palette.getLightVibrantSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getLightVibrantSwatch().getRgb());
            colors.setTextColor(palette.getLightVibrantSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getLightVibrantSwatch().getTitleTextColor());

        } else if (palette.getDarkMutedSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getDarkMutedSwatch().getRgb());
            colors.setTextColor(palette.getDarkMutedSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getDarkMutedSwatch().getTitleTextColor());

        } else if (palette.getLightMutedSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getLightMutedSwatch().getRgb());
            colors.setTextColor(palette.getLightMutedSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getLightMutedSwatch().getTitleTextColor());

        } else if (palette.getVibrantSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getVibrantSwatch().getRgb());
            colors.setTextColor(palette.getVibrantSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getVibrantSwatch().getTitleTextColor());

        } else if (palette.getMutedSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getMutedSwatch().getRgb());
            colors.setTextColor(palette.getMutedSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getMutedSwatch().getTitleTextColor());
        }

        //set the status bar color to be a darker version of the toolbar background Color;
        if (colors.getToolbarBackgroundColor() != 0) {
            float[] hsv = new float[3];
            int color = colors.getToolbarBackgroundColor();
            Color.colorToHSV(color, hsv);
            hsv[2] *= 0.8f; // value component
            colors.setStatusBarColor(Color.HSVToColor(hsv));
        }

        return colors;
    }
}