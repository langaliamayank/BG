package com.androidtv.bhagavadgita.comman;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.core.content.ContextCompat;

import com.androidtv.bhagavadgita.R;

public class GradientProgressBar extends View {

    /*For Circle*/
    private Paint paint;
    private RectF rect;
    private float rotationAngle = 0;
    private ValueAnimator animator;

    public GradientProgressBar(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20f); // Adjust thickness here
        paint.setStrokeCap(Paint.Cap.ROUND); // Smooth rounded ends
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float margin = paint.getStrokeWidth() / 2;
        rect = new RectF(margin, margin, w - margin, h - margin);

        int green = ContextCompat.getColor(getContext(), R.color.colorAccent);
        int orange = ContextCompat.getColor(getContext(), R.color.colorTextPrimary);

        SweepGradient shader = new SweepGradient(w / 2f, h / 2f,
                new int[]{green, orange, green}, null);
        paint.setShader(shader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        // Rotate the entire canvas based on the animator value
        canvas.rotate(rotationAngle, getWidth() / 2f, getHeight() / 2f);
        canvas.drawArc(rect, 0, 360, false, paint);
        canvas.restore();
    }

    public void startAnimation() {
        if (animator != null && animator.isRunning()) return;

        animator = ValueAnimator.ofFloat(0, 360);
        animator.setDuration(1000); // Speed of rotation
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            rotationAngle = (float) animation.getAnimatedValue();
            invalidate(); // Redraw
        });
        animator.start();
    }

    /*For Square*/
//    private Paint paint;
//    private RectF rect;
//    private Matrix matrix; // To rotate the gradient
//    private float rotationAngle = 0;
//    private ValueAnimator animator;
//    private SweepGradient shader;
//
//    public GradientProgressBar(Context context) {
//        super(context);
//        init();
//    }
//
//    private void init() {
//        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(20f);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//        matrix = new Matrix();
//    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        float margin = paint.getStrokeWidth() / 2;
//
//        // Center the square
//        float size = Math.min(w, h) - (margin * 2);
//        float left = (w - size) / 2;
//        float top = (h - size) / 2;
//        rect = new RectF(left, top, left + size, top + size);
//
//        int green = ContextCompat.getColor(getContext(), R.color.colorAccent);
//        int orange = ContextCompat.getColor(getContext(), R.color.colorAccent);
//
//        // Create the shader once
//        shader = new SweepGradient(w / 2f, h / 2f, new int[]{green, orange, green}, null);
//        paint.setShader(shader);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        // 1. Rotate only the shader, NOT the canvas
//        matrix.setRotate(rotationAngle, getWidth() / 2f, getHeight() / 2f);
//        shader.setLocalMatrix(matrix);
//
//        // 2. Draw the fixed square (0f corner radius for sharp square)
//        canvas.drawRect(rect, paint);
//    }
//
//    public void startAnimation() {
//        if (animator != null && animator.isRunning()) return;
//
//        animator = ValueAnimator.ofFloat(0, 360);
//        animator.setDuration(1000);
//        animator.setInterpolator(new LinearInterpolator());
//        animator.setRepeatCount(ValueAnimator.INFINITE);
//        animator.addUpdateListener(animation -> {
//            rotationAngle = (float) animation.getAnimatedValue();
//            invalidate(); // Redraw with the updated matrix
//        });
//        animator.start();
//    }
}