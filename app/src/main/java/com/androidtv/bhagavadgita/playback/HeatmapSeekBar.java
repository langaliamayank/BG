package com.androidtv.bhagavadgita.playback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.SeekBar;

@SuppressLint("RestrictedApi")
public class HeatmapSeekBar extends SeekBar {
    private float[] mHeatData;

    private final Path mWavePath = new Path();
    private final Path mFillPath = new Path();

    private final Paint mWaveLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Height ratio of the curve vs the total view height (e.g. 70% wave, 30% baseline/track)
    private static final float WAVE_HEIGHT_RATIO = 0.75f;
    private int mLastWidth = 0;
    private int mLastHeight = 0;

    public HeatmapSeekBar(Context context) {
        super(context);
        init();
    }

    public HeatmapSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeatmapSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Outline of the peak line (YouTube uses clean white or off-white)
        mWaveLinePaint.setStyle(Paint.Style.STROKE);
        mWaveLinePaint.setStrokeWidth(3f);
        mWaveLinePaint.setColor(Color.argb(200, 255, 255, 255));
        mWaveLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mWaveLinePaint.setStrokeJoin(Paint.Join.ROUND);

        // Fill below the line (gradient applied in updatePaths)
        mFillPaint.setStyle(Paint.Style.FILL);
    }

    public void setHeatData(float[] data) {
        this.mHeatData = data;
        rebuildPaths();
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            rebuildPaths();
        }
    }

    private void rebuildPaths() {
        mWavePath.reset();
        mFillPath.reset();

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0 || mHeatData == null || mHeatData.length < 2) {
            return;
        }

        float availableWidth = width - getPaddingLeft() - getPaddingRight();
        float baselineY = height - getPaddingBottom();
        float maxWaveHeight = height * WAVE_HEIGHT_RATIO;

        int n = mHeatData.length;
        float stepX = availableWidth / (n - 1);

        // Calculate (x, y) coordinates for all points
        float[] ptsX = new float[n];
        float[] ptsY = new float[n];

        for (int i = 0; i < n; i++) {
            ptsX[i] = getPaddingLeft() + (i * stepX);
            // Invert scale: 1.0 is at peak (top), 0.0 is at baseline (bottom)
            float normalized = Math.max(0.0f, Math.min(1.0f, mHeatData[i]));
            ptsY[i] = baselineY - (normalized * maxWaveHeight);
        }

        // Start drawing the smooth spline path
        mWavePath.moveTo(ptsX[0], ptsY[0]);
        mFillPath.moveTo(ptsX[0], baselineY);
        mFillPath.lineTo(ptsX[0], ptsY[0]);

        for (int i = 0; i < n - 1; i++) {
            float p0x = (i > 0) ? ptsX[i - 1] : ptsX[i];
            float p0y = (i > 0) ? ptsY[i - 1] : ptsY[i];

            float p1x = ptsX[i];
            float p1y = ptsY[i];

            float p2x = ptsX[i + 1];
            float p2y = ptsY[i + 1];

            float p3x = (i + 2 < n) ? ptsX[i + 2] : p2x;
            float p3y = (i + 2 < n) ? ptsY[i + 2] : p2y;

            // Catmull-Rom spline tension converted to Cubic Bezier control points
            float ctrl1X = p1x + (p2x - p0x) / 6.0f;
            float ctrl1Y = p1y + (p2y - p0y) / 6.0f;
            float ctrl2X = p2x - (p3x - p1x) / 6.0f;
            float ctrl2Y = p2y - (p3y - p1y) / 6.0f;

            mWavePath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, p2x, p2y);
            mFillPath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, p2x, p2y);
        }

        // Close fill path down to the baseline
        mFillPath.lineTo(ptsX[n - 1], baselineY);
        mFillPath.close();

        // YouTube-like vertical gradient: opaque near the peak, fading toward the bottom
        mFillPaint.setShader(new LinearGradient(
                0, baselineY - maxWaveHeight,
                0, baselineY,
                Color.argb(120, 255, 255, 255), // Top alpha
                Color.argb(10, 255, 255, 255),  // Baseline alpha
                Shader.TileMode.CLAMP
        ));
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (mHeatData != null && mHeatData.length > 1) {
            // Draw filled gradient under the curve
            canvas.drawPath(mFillPath, mFillPaint);
            // Draw top peak curve line
            canvas.drawPath(mWavePath, mWaveLinePaint);
        }

        // Draw standard progress line & thumb over the waveform
        super.onDraw(canvas);
    }
}