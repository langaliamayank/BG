package com.androidtv.bhagavadgita.comman;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;

public class BackgroundImageUtils {

    public static final String TAG = BackgroundImageUtils.class.getSimpleName();

    public static Bitmap createBackgroundWithPreviewWindow(int backgroundWidth,
                                                           int backgroundHeight,
                                                           int previewWindowWidth,
                                                           int previewWindowHeight,
                                                           int gradientSize,
                                                           int color) {

        // Draw the background color
        Bitmap result = Bitmap.createBitmap(backgroundWidth, backgroundHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(color);

        // Create the preview window mask
        Bitmap mask = createPreviewWindowMask(previewWindowWidth, previewWindowHeight, gradientSize);
        if (mask == null) {
            Log.d(TAG, "Background created without a preview window");
            return result;
        }

        // Draw the gradient mask on top of the background
        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(mask, backgroundWidth - previewWindowWidth, 0, maskPaint);

        return result;
    }

    /**
     * Create a Bitmap that will be used as a mask to create the preview window in the background.
     * The Bitmap will be the given width and height. The Bitmap will be transparent except for a
     * gradient on the left and bottom sides.
     *
     * @param width Width of the mask.
     * @param height Height of the mask.
     * @param gradientSize Size of the gradient.
     * @return Bitmap mask that will be used to create a preview window.
     */
    private static Bitmap createPreviewWindowMask(int width, int height, int gradientSize) {

        // Do nothing if the size is invalid
        if (width <= 0 || height <= 0) {
            return null;
        }

        // Initialize the mask
        Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mask);
        canvas.drawColor(Color.TRANSPARENT);

        // If the gradient size is zero, don't draw the gradients
        if (gradientSize <= 0) {
            return mask;
        }

        // Calculate gradient rects
        Rect leftGradientRect = new Rect(
                0, 0, gradientSize, height - gradientSize
        );
        Rect bottomGradientRect = new Rect(
                leftGradientRect.right, height - gradientSize, width, height
        );
        Rect cornerGradientRect = new Rect(
                leftGradientRect.left,
                leftGradientRect.bottom,
                bottomGradientRect.left,
                bottomGradientRect.bottom
        );

        // Create left gradient
        Paint leftGradientPaint = new Paint();
        leftGradientPaint.setDither(true);
        leftGradientPaint.setShader(new LinearGradient(leftGradientRect.left, 0,
                leftGradientRect.right, 0, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP));

        // Create right gradient
        Paint bottomGradientPaint = new Paint();
        bottomGradientPaint.setDither(true);
        bottomGradientPaint.setShader(new LinearGradient(leftGradientRect.right,
                bottomGradientRect.bottom, leftGradientRect.right, bottomGradientRect.top,
                Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP));

        // Create corner gradient
        Paint cornerGradientPaint = new Paint();
        cornerGradientPaint.setDither(true);
        cornerGradientPaint.setShader(new RadialGradient(cornerGradientRect.right,
                cornerGradientRect.top, gradientSize, Color.TRANSPARENT, Color.BLACK,
                Shader.TileMode.CLAMP));

        // Draw the gradients
        canvas.drawRect(leftGradientRect, leftGradientPaint);
        canvas.drawRect(bottomGradientRect, bottomGradientPaint);
        canvas.drawRect(cornerGradientRect, cornerGradientPaint);

        return mask;
    }
}
