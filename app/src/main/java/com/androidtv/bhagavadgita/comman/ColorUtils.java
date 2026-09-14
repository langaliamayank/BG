package com.androidtv.bhagavadgita.comman;

import android.graphics.Color;

import androidx.annotation.ColorInt;

public class ColorUtils {

    public static int lighten(@ColorInt int baseColor, float ratio) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] = hsv[2] + (1.0f - hsv[2]) * ratio;
        hsv[2] = Math.min(1.0f, Math.max(0.0f, hsv[2]));
        return Color.HSVToColor(Color.alpha(baseColor), hsv);
    }

    public static int darken(@ColorInt int baseColor, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= (1f - factor);
        return Color.HSVToColor(hsv);
    }
}