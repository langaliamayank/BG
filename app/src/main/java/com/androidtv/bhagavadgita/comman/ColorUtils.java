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

    public static int whiten(int baseColor, float ratio) {
        int a = Color.alpha(baseColor);
        int r = Color.red(baseColor);
        int g = Color.green(baseColor);
        int b = Color.blue(baseColor);

        // Interpolate each channel towards 255 (white)
        r = Math.round(r + (255 - r) * ratio);
        g = Math.round(g + (255 - g) * ratio);
        b = Math.round(b + (255 - b) * ratio);

        return Color.argb(a, Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }

    public static int darken(@ColorInt int baseColor, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= (1f - factor);
        return Color.HSVToColor(hsv);
    }
}